package com.hoan.qlpt.activity.landlord;
import com.hoan.qlpt.util.EmptyState;

import android.content.Intent;
import android.net.Uri;
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
import com.hoan.qlpt.model.Contract;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TenantsActivity extends AppCompatActivity {

    private final List<ApprovalItem> items = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private ApprovalAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);

        ((TextView) findViewById(R.id.tvScreenTitle)).setText("Người thuê đang ở");
        findViewById(R.id.fabAdd).setVisibility(View.GONE);
        tvEmpty = findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "👥", "Chưa có người thuê nào", "Người đang thuê phòng của bạn sẽ hiện ở đây");

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

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }
        db.collection("contracts")
                .whereEqualTo("landlordId", uid)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snap -> {
                    items.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Contract c = d.toObject(Contract.class);
                        if (c == null) continue;
                        c.setId(d.getId());
                        items.add(new ApprovalItem(c.getId(), c.getTenantName(), c.getRoomTitle(),
                                "Thời hạn: " + fmt.format(new Date(c.getStartDate())) + " - "
                                        + fmt.format(new Date(c.getEndDate()))
                                        + "\nTiền cọc: " + nf.format(c.getDeposit()) + "đ"
                                        + (c.isEndRequested() ? "\nĐã xin trả phòng" : ""), c));
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void onItemClick(ApprovalItem it) {
        Contract c = (Contract) it.data;
        db.collection("profiles").document(c.getTenantId()).get().addOnSuccessListener(p -> {
            String phone = p.getString("phone");
            AlertDialog.Builder b = new AlertDialog.Builder(this)
                    .setTitle(c.getTenantName())
                    .setMessage("Phòng: " + c.getRoomTitle()
                            + "\nSĐT: " + (phone == null ? "(chưa có)" : phone)
                            + "\nGiá thuê: " + nf.format(c.getPrice()) + "đ/tháng")
                    .setNegativeButton("Đóng", null);
            if (phone != null && !phone.isEmpty()) {
                b.setPositiveButton("Gọi điện", (d, w) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
                    } catch (Exception e) {
                        Toast.makeText(this, "Máy không có ứng dụng gọi điện", Toast.LENGTH_LONG).show();
                    }
                });
            }
            b.show();
        });
    }
}