package com.hoan.qlpt.activity.landlord;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Contract;
import com.hoan.qlpt.util.MoneyTextWatcher;
import com.hoan.qlpt.util.Notifier;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateContractActivity extends AppCompatActivity {

    private String requestId, roomId, roomTitle, tenantId, tenantName;
    private long startDate = 0, endDate = 0;
    private TextInputEditText edtPrice, edtDeposit;
    private Button btnStart, btnEnd, btnSave;
    private ProgressBar progress;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contract);

        requestId = getIntent().getStringExtra("requestId");
        roomId = getIntent().getStringExtra("roomId");
        roomTitle = getIntent().getStringExtra("roomTitle");
        tenantId = getIntent().getStringExtra("tenantId");
        tenantName = getIntent().getStringExtra("tenantName");
        if (requestId == null || roomId == null || tenantId == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvRoom)).setText("Phòng: " + roomTitle);
        ((TextView) findViewById(R.id.tvTenant)).setText("Người thuê: " + tenantName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDeposit = findViewById(R.id.edtDeposit);
        MoneyTextWatcher.attach(edtPrice);
        MoneyTextWatcher.attach(edtDeposit);
        btnStart = findViewById(R.id.btnStart);
        btnEnd = findViewById(R.id.btnEnd);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        // Điền sẵn giá phòng, cọc mặc định = 1 tháng
        db.collection("rooms").document(roomId).get().addOnSuccessListener(d -> {
            Long price = d.getLong("price");
            if (price != null) {
                edtPrice.setText(String.valueOf(price));
                edtDeposit.setText(String.valueOf(price));
            }
        });

        btnStart.setOnClickListener(v -> pickDate(true));
        btnEnd.setOnClickListener(v -> pickDate(false));
        btnSave.setOnClickListener(v -> save());
    }

    private void pickDate(boolean isStart) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(y, m, d, 0, 0, 0);
            sel.set(Calendar.MILLISECOND, 0);
            if (isStart) {
                startDate = sel.getTimeInMillis();
                btnStart.setText("Bắt đầu: " + fmt.format(new Date(startDate)));
            } else {
                endDate = sel.getTimeInMillis();
                btnEnd.setText("Kết thúc: " + fmt.format(new Date(endDate)));
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void save() {
        String landlordId = FirebaseAuth.getInstance().getUid();
        if (landlordId == null) return;

        long price, deposit;
        try {
            price = MoneyTextWatcher.parse(text(edtPrice));
            deposit = MoneyTextWatcher.parse(text(edtDeposit));
        } catch (NumberFormatException e) {
            toast("Giá hoặc tiền cọc không hợp lệ");
            return;
        }
        if (price <= 0 || deposit < 0) {
            toast("Giá phải lớn hơn 0, cọc không âm");
            return;
        }
        if (startDate == 0 || endDate == 0) {
            toast("Vui lòng chọn ngày bắt đầu và kết thúc");
            return;
        }
        if (endDate <= startDate) {
            toast("Ngày kết thúc phải sau ngày bắt đầu");
            return;
        }

        setLoading(true);
        // Kiểm tra phòng chưa có người thuê rồi mới tạo
        db.collection("rooms").document(roomId).get().addOnSuccessListener(d -> {
            if ("rented".equals(d.getString("rentStatus"))) {
                setLoading(false);
                toast("Phòng này đã có người thuê");
                return;
            }
            DocumentReference cRef = db.collection("contracts").document();
            Contract c = new Contract();
            c.setId(cRef.getId());
            c.setRequestId(requestId);
            c.setRoomId(roomId);
            c.setRoomTitle(roomTitle);
            c.setLandlordId(landlordId);
            c.setTenantId(tenantId);
            c.setTenantName(tenantName);
            c.setPrice(price);
            c.setDeposit(deposit);
            c.setStartDate(startDate);
            c.setEndDate(endDate);
            c.setCreatedAt(System.currentTimeMillis());

            WriteBatch batch = db.batch();
            batch.set(cRef, c);
            batch.update(db.collection("rooms").document(roomId), "rentStatus", "rented");
            batch.update(db.collection("rentalRequests").document(requestId), "status", "contracted");
            batch.commit()
                    .addOnSuccessListener(v -> {
                        Notifier.send(tenantId, "Hợp đồng mới", "Chủ trọ đã tạo hợp đồng thuê " + roomTitle);
                        toast("Đã tạo hợp đồng");
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        toast("Lỗi: " + e.getMessage());
                    });
        }).addOnFailureListener(e -> {
            setLoading(false);
            toast("Lỗi: " + e.getMessage());
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}