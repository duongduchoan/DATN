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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.ApprovalAdapter;
import com.hoan.qlpt.model.ApprovalItem;
import com.hoan.qlpt.model.House;
import com.hoan.qlpt.util.Notifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hoan.qlpt.util.ReasonDialog;

public class ApproveHousesFragment extends Fragment {

    private final List<ApprovalItem> items = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        EmptyState.set(tvEmpty, "✅", "Không có nhà trọ nào chờ duyệt", "Mọi nhà trọ đã được xử lý");

        adapter = new ApprovalAdapter(items, new ApprovalAdapter.Listener() {
            @Override public void onApprove(ApprovalItem it) { update(it, "approved", ""); }
            @Override public void onReject(ApprovalItem it) {
                ReasonDialog.show(requireContext(), "Từ chối nhà trọ: " + it.title,
                        reason -> update(it, "rejected", reason));
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        load();
    }

    private void load() {
        db.collection("profiles").get().addOnSuccessListener(ps -> {
            Map<String, String> names = new HashMap<>();
            for (DocumentSnapshot d : ps.getDocuments()) names.put(d.getId(), d.getString("fullName"));

            db.collection("houses").whereEqualTo("status", "pending").get()
                    .addOnSuccessListener(hs -> {
                        if (!isAdded()) return;
                        items.clear();
                        for (DocumentSnapshot d : hs.getDocuments()) {
                            House h = d.toObject(House.class);
                            if (h == null) continue;
                            h.setId(d.getId());
                            String owner = names.get(h.getLandlordId());
                            items.add(new ApprovalItem(h.getId(), h.getName(),
                                    h.getAddress() + ", " + h.getDistrict(),
                                    "Chủ trọ: " + (owner == null ? "(chưa có tên)" : owner), h));
                        }
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    });
        }).addOnFailureListener(e -> {
            if (isAdded()) Toast.makeText(requireContext(),
                    "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void update(ApprovalItem it, String status, String reason) {
        boolean approved = "approved".equals(status);
        House h = (House) it.data;
        db.collection("houses").document(it.id)
                .update("status", status, "rejectReason", reason)
                .addOnSuccessListener(v -> {
                    Notifier.send(h.getLandlordId(),
                            approved ? "Nhà trọ đã được duyệt" : "Nhà trọ bị từ chối",
                            approved ? h.getName() : h.getName() + "\nLý do: " + reason);
                    if (!isAdded()) return;
                    items.remove(it);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(requireContext(),
                            approved ? "Đã duyệt nhà trọ" : "Đã từ chối nhà trọ", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}