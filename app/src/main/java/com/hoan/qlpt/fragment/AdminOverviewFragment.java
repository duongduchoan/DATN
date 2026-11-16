package com.hoan.qlpt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hoan.qlpt.R;
import com.hoan.qlpt.util.StatCard;

public class AdminOverviewFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_admin_overview, container, false);
        StatCard.label(root, R.id.stUsers, "Tổng người dùng");
        StatCard.label(root, R.id.stLandlords, "Chủ trọ");
        StatCard.label(root, R.id.stRooms, "Phòng đã duyệt");
        StatCard.label(root, R.id.stPendingHouses, "Nhà trọ chờ duyệt");
        StatCard.label(root, R.id.stPendingRooms, "Phòng chờ duyệt");
        StatCard.label(root, R.id.stPendingRoles, "Yêu cầu lên chủ trọ");
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        count(db.collection("users"), R.id.stUsers);
        count(db.collection("users").whereEqualTo("roleId", 2), R.id.stLandlords);
        count(db.collection("rooms").whereEqualTo("status", "approved"), R.id.stRooms);
        count(db.collection("houses").whereEqualTo("status", "pending"), R.id.stPendingHouses);
        count(db.collection("rooms").whereEqualTo("status", "pending"), R.id.stPendingRooms);
        count(db.collection("roleRequests").whereEqualTo("status", "pending"), R.id.stPendingRoles);
    }

    // Đếm trên server, không tải cả danh sách về máy
    private void count(Query q, int cardId) {
        q.count().get(AggregateSource.SERVER)
                .addOnSuccessListener(r -> {
                    if (isAdded()) StatCard.value(root, cardId, String.valueOf(r.getCount()));
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) StatCard.value(root, cardId, "?");
                });
    }
}