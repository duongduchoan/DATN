package com.hoan.qlpt.activity.landlord;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;

import java.util.HashMap;
import java.util.Map;

public class BankInfoActivity extends AppCompatActivity {

    // Tên ngân hàng và mã BIN dùng cho VietQR
    public static final String[] NAMES = {"Vietcombank", "Techcombank", "MB Bank", "ACB",
            "BIDV", "VietinBank", "TPBank", "VPBank", "Sacombank", "Agribank"};
    public static final String[] BINS = {"970436", "970407", "970422", "970416",
            "970418", "970415", "970423", "970432", "970403", "970405"};

    private Spinner spBank;
    private TextInputEditText edtAccount, edtHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_info);
        spBank = findViewById(R.id.spBank);
        edtAccount = findViewById(R.id.edtAccount);
        edtHolder = findViewById(R.id.edtHolder);
        spBank.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, NAMES));

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }

        FirebaseFirestore.getInstance().collection("payoutInfo").document(uid).get()
                .addOnSuccessListener(d -> {
                    if (!d.exists()) return;
                    String bin = d.getString("bankBin");
                    for (int i = 0; i < BINS.length; i++) {
                        if (BINS[i].equals(bin)) spBank.setSelection(i);
                    }
                    edtAccount.setText(d.getString("bankAccount"));
                    edtHolder.setText(d.getString("bankHolder"));
                });

        findViewById(R.id.btnSave).setOnClickListener(v -> save(uid));
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void save(String uid) {
        String account = text(edtAccount);
        String holder = text(edtHolder);
        if (!account.matches("\\d{6,20}")) {
            Toast.makeText(this, "Số tài khoản gồm 6-20 chữ số", Toast.LENGTH_LONG).show();
            return;
        }
        if (holder.isEmpty()) {
            Toast.makeText(this, "Nhập tên chủ tài khoản", Toast.LENGTH_LONG).show();
            return;
        }
        int p = spBank.getSelectedItemPosition();
        Map<String, Object> m = new HashMap<>();
        m.put("bankBin", BINS[p]);
        m.put("bankName", NAMES[p]);
        m.put("bankAccount", account);
        m.put("bankHolder", holder);
        FirebaseFirestore.getInstance().collection("payoutInfo").document(uid).set(m)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đã lưu thông tin nhận tiền", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}