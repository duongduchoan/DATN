
package com.hoan.qlpt.fragment;
import com.hoan.qlpt.util.EmptyState;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.tenant.InvoiceDetailActivity;
import com.hoan.qlpt.model.Contract;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.hoan.qlpt.util.Notifier;

public class TenantHomeFragment extends Fragment {

    private View root;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(
                R.layout.fragment_tenant_home,
                container,
                false
        );
        EmptyState.set((TextView) root.findViewById(R.id.tvEmpty), "🔑", "Bạn chưa có hợp đồng thuê nào", "Vào tab Tìm phòng và gửi yêu cầu thuê");
        root.findViewById(R.id.btnMyRequests).setOnClickListener(v -> startActivity(
                new Intent(requireContext(), com.hoan.qlpt.activity.tenant.MyRentalRequestsActivity.class)));
        root.findViewById(R.id.btnSaved).setOnClickListener(v -> startActivity(
                new Intent(requireContext(), com.hoan.qlpt.activity.tenant.SavedRoomsActivity.class)));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Tải hợp đồng đang hoạt động của người thuê
        db.collection("contracts")
                .whereEqualTo("tenantId", uid)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || root == null) return;

                    View card = root.findViewById(R.id.cardContract);
                    View empty = root.findViewById(R.id.tvEmpty);

                    if (snap.isEmpty()) {
                        card.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);

                        // Không có hợp đồng thì ẩn thông tin hóa đơn
                        View cardDue = root.findViewById(R.id.cardDue);
                        if (cardDue != null) {
                            cardDue.setVisibility(View.GONE);
                        }
                        return;
                    }

                    DocumentSnapshot cd = snap.getDocuments().get(0);
                    Contract c = cd.toObject(Contract.class);

                    if (c == null) return;

                    empty.setVisibility(View.GONE);
                    card.setVisibility(View.VISIBLE);
                    bindReturn(cd.getId(), c);
                    root.findViewById(R.id.btnIncidents).setOnClickListener(v -> startActivity(
                            new Intent(requireContext(), com.hoan.qlpt.activity.IncidentsActivity.class)
                                    .putExtra("mode", "tenant")));

                    NumberFormat nf =
                            NumberFormat.getInstance(new Locale("vi", "VN"));

                    SimpleDateFormat fmt =
                            new SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                            );

                    ((TextView) root.findViewById(R.id.tvRoom))
                            .setText(c.getRoomTitle());

                    ((TextView) root.findViewById(R.id.tvPrice))
                            .setText(
                                    "Giá thuê: "
                                            + nf.format(c.getPrice())
                                            + "đ/tháng"
                            );

                    ((TextView) root.findViewById(R.id.tvDeposit))
                            .setText(
                                    "Tiền cọc: "
                                            + nf.format(c.getDeposit())
                                            + "đ"
                            );

                    ((TextView) root.findViewById(R.id.tvDates))
                            .setText(
                                    "Thời hạn: "
                                            + fmt.format(new Date(c.getStartDate()))
                                            + " - "
                                            + fmt.format(new Date(c.getEndDate()))
                            );

                    db.collection("profiles").document(c.getLandlordId()).get().addOnSuccessListener(p -> {
                        if (!isAdded()) return;
                        String name = p.getString("fullName");
                        String phone = p.getString("phone");
                        ((TextView) root.findViewById(R.id.tvLandlord)).setText(
                                "Chủ trọ: " + (name == null ? "(chưa có tên)" : name)
                                + (phone == null || phone.isEmpty() ? "" : "\n" + phone));

                        View call = root.findViewById(R.id.btnCallLandlord);
                        if (phone != null && !phone.isEmpty()) {
                            call.setVisibility(View.VISIBLE);
                            call.setOnClickListener(v -> {
                                try {
                                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
                                } catch (Exception e) {
                                    Toast.makeText(requireContext(), "Máy không có ứng dụng gọi điện",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            call.setVisibility(View.GONE);
                        }
                    });

                    // Tải hóa đơn gần nhất của người thuê
                    loadDue(uid);
                });
    }

    private void loadDue(String uid) {
        FirebaseFirestore.getInstance()
                .collection("invoices")
                .whereEqualTo("tenantId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded() || root == null) return;

                    DocumentSnapshot best = null;

                    // Chọn hóa đơn chưa thanh toán có hạn gần nhất
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        if ("paid".equals(d.getString("status"))) {
                            continue;
                        }

                        Long due = d.getLong("dueDate");
                        if (due == null) continue;

                        if (best == null
                                || due < best.getLong("dueDate")) {
                            best = d;
                        }
                    }

                    View card = root.findViewById(R.id.cardDue);

                    if (best == null) {
                        if (card != null) {
                            card.setVisibility(View.GONE);
                        }
                        return;
                    }

                    if (card == null) return;

                    final DocumentSnapshot pick = best;

                    long due = pick.getLong("dueDate");
                    Long totalValue = pick.getLong("total");
                    long total = totalValue == null ? 0 : totalValue;

                    long days = (long) Math.ceil(
                            (due - System.currentTimeMillis())
                                    / 86400000.0
                    );

                    NumberFormat nf =
                            NumberFormat.getInstance(new Locale("vi", "VN"));

                    SimpleDateFormat fmt =
                            new SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                            );

                    ((TextView) root.findViewById(R.id.tvDueTitle))
                            .setText(
                                    "Hóa đơn tháng "
                                            + pick.getLong("month")
                                            + "/"
                                            + pick.getLong("year")
                            );

                    ((TextView) root.findViewById(R.id.tvDueAmount))
                            .setText(nf.format(total) + "đ");

                    ((TextView) root.findViewById(R.id.tvDueInfo))
                            .setText(
                                    "Hạn đóng: "
                                            + fmt.format(new Date(due))
                                            + " • "
                                            + (days >= 0
                                            ? "Còn " + days + " ngày"
                                            : "Quá hạn " + (-days) + " ngày")
                            );

                    ((TextView) root.findViewById(R.id.tvDueStatus))
                            .setText(
                                    "pending".equals(pick.getString("status"))
                                            ? "Chờ chủ trọ xác nhận"
                                            : "Chưa thanh toán"
                            );
                    ((TextView) root.findViewById(R.id.tvDueStatus))
                            .setTextColor(days < 0 ? 0xFFC62828 : 0xFFE65100);

                    card.setVisibility(View.VISIBLE);

                    card.setOnClickListener(v -> {
                        Intent intent = new Intent(
                                requireContext(),
                                InvoiceDetailActivity.class
                        );

                        intent.putExtra("invoiceId", pick.getId());
                        startActivity(intent);
                    });
                });
    }
    private void bindReturn(String contractId, Contract c) {
        Button btn = root.findViewById(R.id.btnReturn);
        TextView info = root.findViewById(R.id.tvReturnInfo);

        if (c.isEndRequested()) {
            btn.setVisibility(View.GONE);
            info.setVisibility(View.VISIBLE);
            info.setText("Bạn đã gửi yêu cầu trả phòng. Chờ chủ trọ xử lý.");
            return;
        }
        info.setVisibility(View.GONE);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Yêu cầu trả phòng")
                .setMessage("Gửi yêu cầu trả phòng cho chủ trọ? Chủ trọ sẽ kiểm tra hóa đơn "
                        + "và tiền cọc rồi kết thúc hợp đồng.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi", (d, w) ->
                        FirebaseFirestore.getInstance().collection("contracts").document(contractId)
                                .update("endRequested", true, "endRequestedAt", System.currentTimeMillis())
                                .addOnSuccessListener(x -> {
                                    Notifier.send(c.getLandlordId(), "Yêu cầu trả phòng",
                                            c.getTenantName() + " xin trả " + c.getRoomTitle());
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Đã gửi yêu cầu trả phòng",
                                                Toast.LENGTH_LONG).show();
                                        load();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isAdded()) Toast.makeText(requireContext(),
                                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }))
                .show());
    }
}
