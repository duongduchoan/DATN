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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.landlord.CreateInvoiceActivity;
import com.hoan.qlpt.adapter.InvoiceAdapter;
import com.hoan.qlpt.model.Invoice;
import com.hoan.qlpt.util.EmptyState;
import com.hoan.qlpt.util.Notifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LandlordInvoicesFragment extends Fragment {

    private final List<Invoice> items = new ArrayList<>();
    private InvoiceAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rvRooms);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "🧾", "Chưa có hóa đơn nào", "Bấm nút + để tạo hóa đơn");
        adapter = new InvoiceAdapter(items, this::onInvoiceClick);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        view.findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateInvoiceActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance().collection("invoices")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    items.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Invoice i = d.toObject(Invoice.class);
                        if (i != null) {
                            i.setId(d.getId());
                            items.add(i);
                        }
                    }
                    Collections.sort(items, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void onInvoiceClick(Invoice i) {
        String when = "tháng " + i.getMonth() + "/" + i.getYear();

        if ("paid".equals(i.getStatus())) {
            String m = "cash".equals(i.getPayMethod()) ? "tiền mặt"
                    : "transfer".equals(i.getPayMethod()) ? "chuyển khoản" : "";
            Toast.makeText(requireContext(), "Hóa đơn đã thanh toán" + (m.isEmpty() ? "" : " (" + m + ")"),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if ("pending".equals(i.getStatus())) {
            boolean cash = "cash".equals(i.getPayMethod());
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận thanh toán")
                    .setMessage((cash ? "Người thuê báo đã trả TIỀN MẶT" : "Người thuê báo đã CHUYỂN KHOẢN")
                            + " hóa đơn " + when + ". Bạn đã nhận đủ tiền?"
                            + (cash ? "" : "\nHãy kiểm tra tài khoản ngân hàng trước khi xác nhận."))
                    .setNegativeButton("Chưa nhận", (d, w) -> setStatus(i, "unpaid", ""))
                    .setPositiveButton("Đã nhận tiền", (d, w) -> setStatus(i, "paid", i.getPayMethod()))
                    .show();
            return;
        }

        // Chưa thanh toán: cho phép đánh dấu đã thu tiền mặt
        new AlertDialog.Builder(requireContext())
                .setTitle("Hóa đơn " + when)
                .setMessage("Người thuê chưa báo thanh toán. Nếu họ đã đưa tiền mặt, bạn có thể đánh dấu đã thu.")
                .setNegativeButton("Đóng", null)
                .setPositiveButton("Đã thu tiền mặt", (d, w) -> setStatus(i, "paid", "cash"))
                .show();
    }

    private void setStatus(Invoice i, String status, String method) {
        String mt = method == null ? "" : method;
        FirebaseFirestore.getInstance().collection("invoices").document(i.getId())
                .update("status", status, "payMethod", mt)
                .addOnSuccessListener(v -> {
                    if ("paid".equals(status)) {
                        Notifier.send(i.getTenantId(), "Đã xác nhận thanh toán",
                                "Chủ trọ đã nhận tiền hóa đơn tháng " + i.getMonth() + "/" + i.getYear());
                    } else {
                        Notifier.send(i.getTenantId(), "Chủ trọ chưa nhận được tiền",
                                "Hóa đơn tháng " + i.getMonth() + "/" + i.getYear()
                                        + " chưa được xác nhận. Vui lòng kiểm tra lại");
                    }
                    if (!isAdded()) return;
                    i.setStatus(status);
                    i.setPayMethod(mt);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
