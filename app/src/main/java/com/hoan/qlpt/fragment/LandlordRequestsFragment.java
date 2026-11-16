package com.hoan.qlpt.fragment;
import com.hoan.qlpt.util.EmptyState;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.RequestAdapter;
import com.hoan.qlpt.model.RentalRequest;
import com.hoan.qlpt.util.Notifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LandlordRequestsFragment extends Fragment {

    private final List<RentalRequest> items = new ArrayList<>();
    private RequestAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_approve_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rvPending);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "📭", "Chưa có yêu cầu thuê nào", "Khi có người quan tâm, yêu cầu sẽ hiện ở đây");

        adapter = new RequestAdapter(items, new RequestAdapter.Listener() {
            @Override public void onApprove(RentalRequest r) { update(r, "approved"); }
            @Override public void onReject(RentalRequest r) { update(r, "rejected"); }
            @Override public void onCreateContract(RentalRequest r) {
                android.content.Intent i = new android.content.Intent(requireContext(),
                        com.hoan.qlpt.activity.landlord.CreateContractActivity.class);
                i.putExtra("requestId", r.getId());
                i.putExtra("roomId", r.getRoomId());
                i.putExtra("roomTitle", r.getRoomTitle());
                i.putExtra("tenantId", r.getTenantId());
                i.putExtra("tenantName", r.getTenantName());
                startActivity(i);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        load();
    }

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance().collection("rentalRequests")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    items.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        RentalRequest r = d.toObject(RentalRequest.class);
                        if (r != null) { r.setId(d.getId()); items.add(r); }
                    }
                    // Mới nhất lên đầu (sắp xếp trên máy để khỏi cần index)
                    Collections.sort(items, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void update(RentalRequest r, String status) {
        FirebaseFirestore.getInstance().collection("rentalRequests").document(r.getId())
                .update("status", status)
                .addOnSuccessListener(v -> {
                    if (!isAdded()) return;
                    r.setStatus(status);
                    Notifier.send(r.getTenantId(),
                            "approved".equals(status) ? "Chủ trọ đã xác nhận lịch xem phòng" : "Lịch xem phòng bị từ chối",
                            r.getRoomTitle());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) load();
    }
}