package com.hoan.qlpt.activity.tenant;
import com.hoan.qlpt.util.EmptyState;

import android.content.Intent;
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
import com.hoan.qlpt.activity.guest.RoomDetailActivity;
import com.hoan.qlpt.adapter.ApprovalAdapter;
import com.hoan.qlpt.model.ApprovalItem;
import com.hoan.qlpt.model.RentalRequest;
import com.hoan.qlpt.util.Notifier;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyRentalRequestsActivity extends AppCompatActivity {

    private final List<ApprovalItem> items = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private ApprovalAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);

        ((TextView) findViewById(R.id.tvScreenTitle)).setText("Yêu cầu thuê của tôi");
        findViewById(R.id.fabAdd).setVisibility(View.GONE);
        tvEmpty = findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "📨", "Bạn chưa gửi yêu cầu thuê nào", "Vào tab Tìm phòng để bắt đầu");

        adapter = new ApprovalAdapter(items, new ApprovalAdapter.Listener() {
            @Override public void onApprove(ApprovalItem it) {}
            @Override public void onReject(ApprovalItem it) {}
        });
        adapter.setShowActions(false);
        adapter.setOnItemClick(this::onItemClick);

        RecyclerView rv = findViewById(R.id.rvPending);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private String statusText(String s) {
        if (s == null) return "";
        switch (s) {
            case "approved": return "Chủ trọ đã xác nhận lịch xem phòng";
            case "contracted": return "Đã có hợp đồng";
            case "rejected": return "Bị từ chối";
            case "cancelled": return "Đã hủy";
            default: return "Đang chờ chủ trọ xác nhận lịch";
        }
    }

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }
        db.collection("rentalRequests").whereEqualTo("tenantId", uid).get()
                .addOnSuccessListener(snap -> {
                    List<RentalRequest> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        RentalRequest r = d.toObject(RentalRequest.class);
                        if (r != null) { r.setId(d.getId()); list.add(r); }
                    }
                    Collections.sort(list, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                    items.clear();
                    for (RentalRequest r : list) {
                        String msg = r.getMessage() == null || r.getMessage().isEmpty()
                                ? "" : "\nLời nhắn: " + r.getMessage();
                        String visit = r.getVisitDate() > 0
                                ? "Lịch xem: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(new Date(r.getVisitDate())) + "\n" : "";
                        items.add(new ApprovalItem(r.getId(), r.getRoomTitle(), statusText(r.getStatus()),
                                visit + "Gửi lúc " + fmt.format(new Date(r.getCreatedAt())) + msg, r));
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> toast("Lỗi tải: " + e.getMessage()));
    }

    private void onItemClick(ApprovalItem it) {
        RentalRequest r = (RentalRequest) it.data;
        if ("pending".equals(r.getStatus())) {
            new AlertDialog.Builder(this)
                    .setTitle(r.getRoomTitle())
                    .setItems(new String[]{"Xem phòng", "Hủy yêu cầu"}, (d, which) -> {
                        if (which == 0) openRoom(r); else confirmCancel(r);
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        } else {
            openRoom(r);
        }
    }

    private void openRoom(RentalRequest r) {
        Intent i = new Intent(this, RoomDetailActivity.class);
        i.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, r.getRoomId());
        startActivity(i);
    }

    private void confirmCancel(RentalRequest r) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy yêu cầu thuê")
                .setMessage("Bạn muốn hủy yêu cầu thuê " + r.getRoomTitle() + "?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy yêu cầu", (d, w) ->
                        db.collection("rentalRequests").document(r.getId()).update("status", "cancelled")
                                .addOnSuccessListener(v -> {
                                    Notifier.send(r.getLandlordId(), "Người thuê hủy yêu cầu",
                                            r.getTenantName() + " đã hủy yêu cầu thuê " + r.getRoomTitle());
                                    toast("Đã hủy yêu cầu");
                                    load();
                                })
                                .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage())))
                .show();
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_LONG).show(); }
}