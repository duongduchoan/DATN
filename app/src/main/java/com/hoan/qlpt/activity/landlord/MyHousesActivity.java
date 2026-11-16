package com.hoan.qlpt.activity.landlord;
import com.hoan.qlpt.util.EmptyState;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.ApprovalAdapter;
import com.hoan.qlpt.model.ApprovalItem;
import com.hoan.qlpt.model.House;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyHousesActivity extends AppCompatActivity {

    private final List<ApprovalItem> items = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ApprovalAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_houses);

        tvEmpty = findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "🏠", "Bạn chưa có nhà trọ nào", "Thêm nhà trọ ở màn hình Thêm phòng");
        RecyclerView rv = findViewById(R.id.rvPending);
        adapter = new ApprovalAdapter(items, new ApprovalAdapter.Listener() {
            @Override public void onApprove(ApprovalItem it) {}
            @Override public void onReject(ApprovalItem it) {}
        });
        adapter.setShowActions(false);
        adapter.setOnItemClick(this::onItemClick);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }
        db.collection("houses").whereEqualTo("landlordId", uid).get()
                .addOnSuccessListener(snap -> {
                    List<House> houses = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        House h = d.toObject(House.class);
                        if (h != null) { h.setId(d.getId()); houses.add(h); }
                    }
                    Collections.sort(houses, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                    items.clear();
                    for (House h : houses) {
                        String st;
                        switch (h.getStatus() == null ? "approved" : h.getStatus()) {
                            case "pending": st = "Chờ admin duyệt"; break;
                            case "rejected":
                                st = "Bị từ chối"
                                        + (h.getRejectReason() == null || h.getRejectReason().isEmpty()
                                        ? "" : ": " + h.getRejectReason())
                                        + "\n(bấm để gửi lại)";
                                break;
                            default: st = "Đã duyệt";
                        }
                        items.add(new ApprovalItem(h.getId(), h.getName(),
                                h.getAddress() + ", " + h.getDistrict(), st, h));
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void onItemClick(ApprovalItem it) {
        House h = (House) it.data;
        if (!"rejected".equals(h.getStatus())) {
            Toast.makeText(this, it.line2, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Gửi lại để duyệt")
                .setMessage("Gửi lại \"" + h.getName() + "\" cho admin xem xét?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi lại", (d, w) ->
                        db.collection("houses").document(h.getId())
                                .update("status", "pending", "rejectReason", "")
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(this, "Đã gửi lại, chờ admin duyệt", Toast.LENGTH_LONG).show();
                                    load();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()))
                .show();
    }
}