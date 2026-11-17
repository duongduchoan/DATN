package com.hoan.qlpt.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.landlord.RequestDetailActivity;
import com.hoan.qlpt.activity.tenant.MyRentalRequestsActivity;
import com.hoan.qlpt.adapter.NotificationAdapter;
import com.hoan.qlpt.model.AppNotification;
import com.hoan.qlpt.util.EmptyState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private final List<AppNotification> items = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private NotificationAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rvNotifications);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "🔔", "Chưa có thông báo nào", "Thông báo mới sẽ hiện ở đây");
        adapter = new NotificationAdapter(items, this::open);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        view.findViewById(R.id.btnReadAll).setOnClickListener(v -> markAllRead());
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        db.collection("notifications").whereEqualTo("userId", uid).get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    items.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        AppNotification n = d.toObject(AppNotification.class);
                        if (n != null) {
                            n.setId(d.getId());
                            items.add(n);
                        }
                    }
                    Collections.sort(items, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void markRead(AppNotification n) {
        if (n.isRead()) return;
        db.collection("notifications").document(n.getId()).update("read", true)
                .addOnSuccessListener(v -> {
                    n.setRead(true);
                    if (isAdded()) adapter.notifyDataSetChanged();
                });
    }

    private void markAllRead() {
        WriteBatch batch = db.batch();
        int count = 0;
        for (AppNotification n : items) {
            if (!n.isRead()) {
                batch.update(db.collection("notifications").document(n.getId()), "read", true);
                count++;
            }
        }
        if (count == 0) return;
        batch.commit().addOnSuccessListener(v -> {
            for (AppNotification n : items) n.setRead(true);
            if (isAdded()) adapter.notifyDataSetChanged();
        });
    }

    // Bấm vào thông báo: đánh dấu đã đọc rồi mở màn hình liên quan
    private void open(AppNotification n) {
        markRead(n);
        String type = n.getType() == null ? "" : n.getType();
        if ("visit_new".equals(type) && n.getRefId() != null && !n.getRefId().isEmpty()) {
            Intent i = new Intent(requireContext(), RequestDetailActivity.class);
            i.putExtra("requestId", n.getRefId());
            startActivity(i);
        } else if ("visit_result".equals(type)) {
            startActivity(new Intent(requireContext(), MyRentalRequestsActivity.class));
        }
    }
}