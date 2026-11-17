package com.hoan.qlpt.activity.landlord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.RentalRequest;
import com.hoan.qlpt.util.Notifier;
import com.hoan.qlpt.util.ReasonDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RequestDetailActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat day = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat time = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private String requestId;
    private RentalRequest req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        requestId = getIntent().getStringExtra("requestId");
        if (requestId == null) {
            finish();
            return;
        }

        findViewById(R.id.btnApprove).setOnClickListener(v -> approve());
        findViewById(R.id.btnReject).setOnClickListener(v -> reject());
        findViewById(R.id.btnContract).setOnClickListener(v -> openContract());
    }

    // Quay lại từ màn tạo hợp đồng thì trạng thái cập nhật
    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        db.collection("rentalRequests").document(requestId).get()
                .addOnSuccessListener(d -> {
                    req = d.toObject(RentalRequest.class);
                    String me = FirebaseAuth.getInstance().getUid();
                    if (req == null || me == null || !me.equals(req.getLandlordId())) {
                        toast("Không tìm thấy lịch hẹn này");
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
    }

    private void bind() {
        ((TextView) findViewById(R.id.tvRoom)).setText(req.getRoomTitle());

        TextView st = findViewById(R.id.tvStatus);
        String s = req.getStatus() == null ? "pending" : req.getStatus();
        switch (s) {
            case "approved":
                st.setText("Đã xác nhận lịch xem");
                st.setTextColor(0xFF2E7D32);
                break;
            case "contracted":
                st.setText("Đã tạo hợp đồng");
                st.setTextColor(0xFF009688);
                break;
            case "rejected":
                st.setText("Đã từ chối");
                st.setTextColor(0xFFE53935);
                break;
            case "cancelled":
                st.setText("Người thuê đã hủy");
                st.setTextColor(0xFF9E9E9E);
                break;
            default:
                st.setText("Đang chờ bạn xác nhận");
                st.setTextColor(0xFFFF9800);
        }

        String visit = req.getVisitDate() > 0 ? day.format(new Date(req.getVisitDate())) : "Không ghi";
        String note = req.getMessage() == null || req.getMessage().isEmpty() ? "(Không có)" : req.getMessage();
        ((TextView) findViewById(R.id.tvInfo)).setText(
                "Người đặt: " + req.getTenantName()
                        + "\nSố điện thoại: " + req.getTenantPhone()
                        + "\n\nNgày xem phòng: " + visit
                        + "\nSố người ở: " + req.getPeopleCount()
                        + "\nSố lượng xe: " + req.getVehicleCount()
                        + "\nThú cưng: " + (req.getPets() == null || req.getPets().isEmpty() ? "Không" : req.getPets())
                        + "\nDự kiến vào ở: " + (req.getExpectedMoveIn() == null || req.getExpectedMoveIn().isEmpty()
                        ? "Không ghi" : req.getExpectedMoveIn())
                        + "\n\nGhi chú: " + note
                        + "\n\nGửi lúc: " + time.format(new Date(req.getCreatedAt())));

        findViewById(R.id.layoutActions).setVisibility("pending".equals(s) ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnContract).setVisibility("approved".equals(s) ? View.VISIBLE : View.GONE);
    }

    private void approve() {
        db.collection("rentalRequests").document(requestId).update("status", "approved")
                .addOnSuccessListener(v -> {
                    String when = req.getVisitDate() > 0
                            ? "\nNgày xem phòng: " + day.format(new Date(req.getVisitDate())) : "";
                    Notifier.send(req.getTenantId(), "Chủ trọ đã xác nhận lịch xem phòng",
                            req.getRoomTitle() + when + "\nHãy liên hệ chủ trọ để thống nhất giờ xem.",
                            "visit_result", requestId);
                    toast("Đã xác nhận lịch, người thuê sẽ nhận được thông báo");
                    load();
                })
                .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage()));
    }

    private void reject() {
        ReasonDialog.show(this, "Từ chối lịch xem phòng", reason ->
                db.collection("rentalRequests").document(requestId).update("status", "rejected")
                        .addOnSuccessListener(v -> {
                            Notifier.send(req.getTenantId(), "Lịch xem phòng bị từ chối",
                                    req.getRoomTitle() + "\nLý do: " + reason,
                                    "visit_result", requestId);
                            toast("Đã từ chối lịch hẹn");
                            load();
                        })
                        .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage())));
    }

    private void openContract() {
        Intent i = new Intent(this, CreateContractActivity.class);
        i.putExtra("requestId", req.getId());
        i.putExtra("roomId", req.getRoomId());
        i.putExtra("roomTitle", req.getRoomTitle());
        i.putExtra("tenantId", req.getTenantId());
        i.putExtra("tenantName", req.getTenantName());
        startActivity(i);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}