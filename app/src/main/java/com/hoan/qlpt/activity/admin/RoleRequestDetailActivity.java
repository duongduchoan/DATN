package com.hoan.qlpt.activity.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.ImageSliderAdapter;
import com.hoan.qlpt.model.RoleRequest;
import com.hoan.qlpt.util.Notifier;
import com.hoan.qlpt.util.Role;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoleRequestDetailActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RoleRequest req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_request_detail);

        String id = getIntent().getStringExtra("requestId");
        if (id == null) {
            finish();
            return;
        }

        db.collection("roleRequests").document(id).get()
                .addOnSuccessListener(d -> {
                    req = d.toObject(RoleRequest.class);
                    if (req == null) {
                        finish();
                        return;
                    }
                    req.setId(d.getId());
                    bind();
                })
                .addOnFailureListener(e -> {
                    toast("Lỗi tải: " + e.getMessage());
                    finish();
                });

        findViewById(R.id.btnApprove).setOnClickListener(v -> confirmApprove());
        findViewById(R.id.btnReject).setOnClickListener(v -> askReason());
    }

    private void bind() {
        TextView st = findViewById(R.id.tvStatus);
        switch (req.getStatus()) {
            case "approved":
                st.setText("Đã duyệt");
                st.setTextColor(0xFF2E7D32);
                break;
            case "rejected":
                st.setText("Đã từ chối");
                st.setTextColor(0xFFE53935);
                break;
            default:
                st.setText("Chờ duyệt");
                st.setTextColor(0xFFFF9800);
        }

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(req.getCreatedAt()));
        ((TextView) findViewById(R.id.tvInfo)).setText(
                "Họ tên: " + req.getFullName()
                        + "\nSĐT: " + req.getPhone()
                        + "\nEmail: " + req.getEmail()
                        + "\nCCCD: " + req.getCccd()
                        + "\nSố nhà trọ: " + req.getHouseCount() + " • Tổng số phòng: " + req.getRoomCount()
                        + "\nĐịa chỉ nhà trọ chính: " + req.getHouseAddress()
                        + "\nGiới thiệu: " + req.getDescription()
                        + "\nGửi lúc: " + date
                        + ("rejected".equals(req.getStatus()) && req.getRejectReason() != null
                        ? "\nLý do từ chối: " + req.getRejectReason() : ""));

        List<String> urls = req.getImageUrls() == null ? new ArrayList<>() : req.getImageUrls();
        ((ViewPager2) findViewById(R.id.vpImages)).setAdapter(new ImageSliderAdapter(urls));

        // Chỉ xử lý được khi còn chờ duyệt
        findViewById(R.id.layoutActions).setVisibility(
                "pending".equals(req.getStatus()) ? View.VISIBLE : View.GONE);
    }

    private void confirmApprove() {
        new AlertDialog.Builder(this)
                .setTitle("Duyệt yêu cầu")
                .setMessage("Cấp quyền chủ trọ cho " + req.getFullName() + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Duyệt", (d, w) -> {
                    WriteBatch b = db.batch();
                    b.update(db.collection("roleRequests").document(req.getId()), "status", "approved");
                    b.update(db.collection("users").document(req.getUid()), "roleId", Role.LANDLORD);
                    commit(b, "Đã cấp quyền chủ trọ (người dùng cần đăng nhập lại)",
                            "Yêu cầu làm chủ trọ được duyệt",
                            "Bạn đã được cấp quyền chủ trọ. Hãy đăng xuất và đăng nhập lại");
                })
                .show();
    }

    private void askReason() {
        EditText edt = new EditText(this);
        edt.setHint("Lý do từ chối (bắt buộc)");
        edt.setPadding(48, 32, 48, 32);
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Từ chối yêu cầu")
                .setView(edt)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Từ chối", null)
                .create();
        dlg.show();
        // Ghi đè nút để không tự đóng khi chưa nhập lý do
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String reason = edt.getText().toString().trim();
            if (reason.length() < 5) {
                toast("Hãy nhập lý do (ít nhất 5 ký tự)");
                return;
            }
            WriteBatch b = db.batch();
            b.update(db.collection("roleRequests").document(req.getId()),
                    "status", "rejected", "rejectReason", reason);
            commit(b, "Đã từ chối yêu cầu", "Yêu cầu làm chủ trọ bị từ chối", "Lý do: " + reason);
            dlg.dismiss();
        });
    }

    private void commit(WriteBatch b, String okMsg, String notiTitle, String notiBody) {
        b.commit()
                .addOnSuccessListener(v -> {
                    Notifier.send(req.getUid(), notiTitle, notiBody);
                    toast(okMsg);
                    finish();
                })
                .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage()));
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}