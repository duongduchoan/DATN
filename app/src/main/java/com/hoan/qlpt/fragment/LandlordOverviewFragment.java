package com.hoan.qlpt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.util.BarChartView;
import com.hoan.qlpt.util.StatCard;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class LandlordOverviewFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_landlord_overview, container, false);
        StatCard.label(root, R.id.stRooms, "Tổng số phòng");
        StatCard.label(root, R.id.stRented, "Đang cho thuê");
        StatCard.label(root, R.id.stAvailable, "Phòng trống");
        StatCard.label(root, R.id.stRevenue, "Doanh thu tháng này");
        StatCard.label(root, R.id.stRequests, "Yêu cầu thuê mới");
        StatCard.label(root, R.id.stInvoices, "Hóa đơn chờ xác nhận");
        StatCard.label(root, R.id.stOverdue, "Hóa đơn quá hạn");
        StatCard.label(root, R.id.stExpiring, "Hợp đồng sắp hết hạn (30 ngày)");
        StatCard.label(root, R.id.stReturn, "Yêu cầu trả phòng");
        StatCard.label(root, R.id.stIncidents, "Sự cố chưa xử lý");
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        loadRooms(uid);
        loadRequests(uid);
        loadInvoices(uid);
        loadContracts(uid);
        loadIncidents(uid);
    }

    private void set(int cardId, String value) {
        if (isAdded()) StatCard.value(root, cardId, value);
    }

    private void fail(int... cardIds) {
        for (int id : cardIds) set(id, "?");
    }

    private void loadRooms(String uid) {
        db.collection("rooms").whereEqualTo("landlordId", uid).get()
                .addOnSuccessListener(snap -> {
                    int total = 0, rented = 0;
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        if ("rejected".equals(d.getString("status"))) continue;
                        total++;
                        if ("rented".equals(d.getString("rentStatus"))) rented++;
                    }
                    set(R.id.stRooms, String.valueOf(total));
                    set(R.id.stRented, String.valueOf(rented));
                    set(R.id.stAvailable, String.valueOf(total - rented));
                })
                .addOnFailureListener(e -> fail(R.id.stRooms, R.id.stRented, R.id.stAvailable));
    }

    private void loadRequests(String uid) {
        db.collection("rentalRequests")
                .whereEqualTo("landlordId", uid)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snap -> set(R.id.stRequests, String.valueOf(snap.size())))
                .addOnFailureListener(e -> fail(R.id.stRequests));
    }

    private void loadInvoices(String uid) {
        db.collection("invoices").whereEqualTo("landlordId", uid).get()
                .addOnSuccessListener(snap -> {
                    Calendar now = Calendar.getInstance();
                    int month = now.get(Calendar.MONTH) + 1;
                    int year = now.get(Calendar.YEAR);
                    long nowMs = System.currentTimeMillis();

                    long[] byMonth = new long[12];
                    long thisMonth = 0;
                    int pending = 0, overdue = 0;

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String st = d.getString("status");
                        Long m = d.getLong("month"), y = d.getLong("year");
                        Long total = d.getLong("total"), due = d.getLong("dueDate");

                        if ("pending".equals(st)) pending++;
                        if (!"paid".equals(st) && due != null && due < nowMs) overdue++;

                        if ("paid".equals(st) && m != null && y != null && total != null
                                && y.intValue() == year && m >= 1 && m <= 12) {
                            byMonth[m.intValue() - 1] += total;
                            if (m.intValue() == month) thisMonth += total;
                        }
                    }
                    set(R.id.stRevenue, nf.format(thisMonth) + "đ");
                    set(R.id.stInvoices, String.valueOf(pending));
                    set(R.id.stOverdue, String.valueOf(overdue));
                    if (isAdded()) {
                        ((BarChartView) root.findViewById(R.id.chartRevenue))
                                .setData(byMonth, now.get(Calendar.MONTH));
                    }
                })
                .addOnFailureListener(e -> fail(R.id.stRevenue, R.id.stInvoices, R.id.stOverdue));
    }

    private void loadContracts(String uid) {
        db.collection("contracts")
                .whereEqualTo("landlordId", uid)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snap -> {
                    long in30 = System.currentTimeMillis() + 30L * 24 * 3600 * 1000;
                    int expiring = 0, returning = 0;
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Long end = d.getLong("endDate");
                        if (end != null && end <= in30) expiring++;
                        if (Boolean.TRUE.equals(d.getBoolean("endRequested"))) returning++;
                    }
                    set(R.id.stExpiring, String.valueOf(expiring));
                    set(R.id.stReturn, String.valueOf(returning));
                })
                .addOnFailureListener(e -> fail(R.id.stExpiring, R.id.stReturn));
    }

    private void loadIncidents(String uid) {
        db.collection("incidents").whereEqualTo("landlordId", uid).get()
                .addOnSuccessListener(snap -> {
                    int open = 0;
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        if (!"done".equals(d.getString("status"))) open++;
                    }
                    set(R.id.stIncidents, String.valueOf(open));
                })
                .addOnFailureListener(e -> fail(R.id.stIncidents));
    }
}