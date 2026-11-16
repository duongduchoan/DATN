package com.hoan.qlpt.activity.tenant;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Invoice;
import com.hoan.qlpt.util.BarChartView;
import com.hoan.qlpt.util.StatCard;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {

    private static final int ELEC = 0, WATER = 1, TOTAL = 2;

    private final List<Invoice> all = new ArrayList<>();
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private int year;
    private int mode = TOTAL;
    private BarChartView chart;
    private TextView tvYear, tvNote;
    private View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        root = findViewById(R.id.rootStats);
        chart = findViewById(R.id.chart);
        tvYear = findViewById(R.id.tvYear);
        tvNote = findViewById(R.id.tvNote);
        year = Calendar.getInstance().get(Calendar.YEAR);

        StatCard.label(root, R.id.stAvg, "Trung bình mỗi tháng");
        StatCard.label(root, R.id.stMax, "Tháng cao nhất");
        StatCard.label(root, R.id.stMin, "Tháng thấp nhất");

        findViewById(R.id.btnPrevYear).setOnClickListener(v -> { year--; render(); });
        findViewById(R.id.btnNextYear).setOnClickListener(v -> { year++; render(); });

        ((ChipGroup) findViewById(R.id.chipGroupMode)).setOnCheckedStateChangeListener((g, ids) -> {
            int id = ids.isEmpty() ? R.id.chipTotal : ids.get(0);
            if (id == R.id.chipElec) mode = ELEC;
            else if (id == R.id.chipWater) mode = WATER;
            else mode = TOTAL;
            render();
        });

        load();
    }

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }
        FirebaseFirestore.getInstance().collection("invoices")
                .whereEqualTo("tenantId", uid).get()
                .addOnSuccessListener(snap -> {
                    all.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Invoice i = d.toObject(Invoice.class);
                        if (i != null) all.add(i);
                    }
                    render();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    render();
                });
    }

    private long valueOf(Invoice i) {
        switch (mode) {
            case ELEC: return Math.max(0, i.getElecNew() - i.getElecOld()) * i.getElecPrice();
            case WATER: return Math.max(0, i.getWaterNew() - i.getWaterOld()) * i.getWaterPrice();
            default: return i.getTotal();
        }
    }

    private void render() {
        tvYear.setText("Năm " + year);

        long[] vals = new long[12];
        for (Invoice i : all) {
            if (i.getYear() == year && i.getMonth() >= 1 && i.getMonth() <= 12) {
                vals[i.getMonth() - 1] += valueOf(i);
            }
        }

        Calendar now = Calendar.getInstance();
        int highlight = year == now.get(Calendar.YEAR) ? now.get(Calendar.MONTH) : -1;
        chart.setData(vals, highlight);

        long sum = 0;
        int count = 0, maxIdx = -1, minIdx = -1;
        for (int m = 0; m < 12; m++) {
            if (vals[m] <= 0) continue;
            sum += vals[m];
            count++;
            if (maxIdx < 0 || vals[m] > vals[maxIdx]) maxIdx = m;
            if (minIdx < 0 || vals[m] < vals[minIdx]) minIdx = m;
        }

        if (count == 0) {
            StatCard.value(root, R.id.stAvg, "—");
            StatCard.value(root, R.id.stMax, "—");
            StatCard.value(root, R.id.stMin, "—");
            tvNote.setText("Chưa có hóa đơn nào trong năm " + year);
            return;
        }
        StatCard.value(root, R.id.stAvg, nf.format(sum / count) + "đ");
        StatCard.value(root, R.id.stMax, "T" + (maxIdx + 1) + " • " + nf.format(vals[maxIdx]) + "đ");
        StatCard.value(root, R.id.stMin, "T" + (minIdx + 1) + " • " + nf.format(vals[minIdx]) + "đ");
        tvNote.setText("Dựa trên " + count + " hóa đơn có dữ liệu. Tháng chưa có hóa đơn không tính vào trung bình.");
    }
}