package com.hoan.qlpt.fragment;

import android.content.Intent;
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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.admin.RoleRequestDetailActivity;
import com.hoan.qlpt.adapter.ApprovalAdapter;
import com.hoan.qlpt.model.ApprovalItem;
import com.hoan.qlpt.model.RoleRequest;
import com.hoan.qlpt.util.EmptyState;

import java.util.ArrayList;
import java.util.List;

public class RoleRequestsFragment extends Fragment {

    private final List<ApprovalItem> items = new ArrayList<>();
    private ApprovalAdapter adapter;
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
        EmptyState.set(tvEmpty, "✅", "Không có yêu cầu nào chờ duyệt", "");

        adapter = new ApprovalAdapter(items, new ApprovalAdapter.Listener() {
            @Override
            public void onApprove(ApprovalItem it) {
            }

            @Override
            public void onReject(ApprovalItem it) {
            }
        });
        adapter.setShowActions(false); // phải mở chi tiết xem rồi mới duyệt
        adapter.setOnItemClick(it -> {
            Intent i = new Intent(requireContext(), RoleRequestDetailActivity.class);
            i.putExtra("requestId", it.id);
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        FirebaseFirestore.getInstance().collection("roleRequests")
                .whereEqualTo("status", "pending").get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    items.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        RoleRequest r = d.toObject(RoleRequest.class);
                        if (r == null) continue;
                        r.setId(d.getId());
                        items.add(new ApprovalItem(r.getId(), r.getFullName(),
                                r.getPhone() + " • " + r.getEmail(),
                                r.getHouseCount() + " nhà trọ • " + r.getRoomCount()
                                        + " phòng • bấm để xem chi tiết", r));
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}