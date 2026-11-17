package com.hoan.qlpt.activity.landlord;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Contract;
import com.hoan.qlpt.model.Invoice;
import com.hoan.qlpt.util.MoneyTextWatcher;
import com.hoan.qlpt.util.Notifier;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateInvoiceActivity extends AppCompatActivity {

    private final List<Contract> contracts = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Spinner spContract;
    private TextInputEditText edtMonth, edtYear, edtElecOld, edtElecNew, edtElecPrice,
            edtWaterOld, edtWaterNew, edtWaterPrice, edtOther;
    private TextView tvTotal;
    private Button btnSave;
    private ProgressBar progress;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private com.google.firebase.firestore.DocumentSnapshot bank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice);

        spContract = findViewById(R.id.spContract);
        edtMonth = findViewById(R.id.edtMonth);
        edtYear = findViewById(R.id.edtYear);
        edtElecOld = findViewById(R.id.edtElecOld);
        edtElecNew = findViewById(R.id.edtElecNew);
        edtElecPrice = findViewById(R.id.edtElecPrice);
        edtWaterOld = findViewById(R.id.edtWaterOld);
        edtWaterNew = findViewById(R.id.edtWaterNew);
        edtWaterPrice = findViewById(R.id.edtWaterPrice);
        edtOther = findViewById(R.id.edtOther);
        tvTotal = findViewById(R.id.tvTotal);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        MoneyTextWatcher.attach(edtElecPrice);
        MoneyTextWatcher.attach(edtWaterPrice);
        MoneyTextWatcher.attach(edtOther);

        Calendar now = Calendar.getInstance();
        edtMonth.setText(String.valueOf(now.get(Calendar.MONTH) + 1));
        edtYear.setText(String.valueOf(now.get(Calendar.YEAR)));

        TextWatcher w = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                preview();
            }
        };
        for (TextInputEditText e : new TextInputEditText[]{edtElecOld, edtElecNew, edtElecPrice,
                edtWaterOld, edtWaterNew, edtWaterPrice, edtOther}) {
            e.addTextChangedListener(w);
        }

        spContract.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                fillOldReadings(contracts.get(pos));
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });

        btnSave.setOnClickListener(v -> save());
        loadContracts();
        String me = FirebaseAuth.getInstance().getUid();
        if (me != null) {
            db.collection("payoutInfo").document(me).get()
                    .addOnSuccessListener(d -> bank = d);
        }
    }

    private void loadContracts() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        db.collection("contracts")
                .whereEqualTo("landlordId", uid)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> labels = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Contract c = d.toObject(Contract.class);
                        if (c == null) continue;
                        c.setId(d.getId());
                        contracts.add(c);
                        labels.add(c.getRoomTitle() + " - " + c.getTenantName());
                    }
                    if (contracts.isEmpty()) {
                        toast("Chưa có hợp đồng nào đang hiệu lực");
                        finish();
                        return;
                    }
                    spContract.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, labels));
                })
                .addOnFailureListener(e -> toast("Lỗi tải: " + e.getMessage()));
    }

    // Số cũ = số mới của hóa đơn gần nhất cùng hợp đồng
    private void fillOldReadings(Contract c) {
        edtElecOld.setText("0");
        edtWaterOld.setText("0");
        db.collection("invoices")
                .whereEqualTo("landlordId", c.getLandlordId())
                .whereEqualTo("contractId", c.getId()).get()
                .addOnSuccessListener(snap -> {
                    Invoice latest = null;
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Invoice i = d.toObject(Invoice.class);
                        if (i != null && (latest == null || i.getCreatedAt() > latest.getCreatedAt()))
                            latest = i;
                    }
                    if (latest != null) {
                        edtElecOld.setText(String.valueOf(latest.getElecNew()));
                        edtWaterOld.setText(String.valueOf(latest.getWaterNew()));
                    }
                });
        db.collection("rooms").document(c.getRoomId()).get().addOnSuccessListener(d -> {
            Long e = d.getLong("elecPrice"), w = d.getLong("waterPrice"), s = d.getLong("serviceFee");
            if (e != null && e > 0) edtElecPrice.setText(String.valueOf(e));
            if (w != null && w > 0) edtWaterPrice.setText(String.valueOf(w));
            if (s != null && s > 0) edtOther.setText(String.valueOf(s));
        });
    }

    private long num(TextInputEditText e) {
        return MoneyTextWatcher.parse(e.getText());
    }

    private long calcTotal(long roomFee) {
        return roomFee
                + (num(edtElecNew) - num(edtElecOld)) * num(edtElecPrice)
                + (num(edtWaterNew) - num(edtWaterOld)) * num(edtWaterPrice)
                + num(edtOther);
    }

    private void preview() {
        int pos = spContract.getSelectedItemPosition();
        if (pos < 0 || pos >= contracts.size()) return;
        tvTotal.setText("Tổng cộng: " + nf.format(calcTotal(contracts.get(pos).getPrice())) + "đ");
    }

    private void save() {
        int pos = spContract.getSelectedItemPosition();
        if (pos < 0 || pos >= contracts.size()) return;
        Contract c = contracts.get(pos);

        int month = (int) num(edtMonth), year = (int) num(edtYear);
        if (month < 1 || month > 12 || year < 2000) {
            toast("Tháng hoặc năm không hợp lệ");
            return;
        }
        if (num(edtElecNew) < num(edtElecOld) || num(edtWaterNew) < num(edtWaterOld)) {
            toast("Số mới không được nhỏ hơn số cũ");
            return;
        }

        setLoading(true);
        // Chặn trùng tháng của cùng hợp đồng
        db.collection("invoices")
                .whereEqualTo("landlordId", c.getLandlordId())
                .whereEqualTo("contractId", c.getId()).get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Long m = d.getLong("month"), y = d.getLong("year");
                        if (m != null && y != null && m == month && y == year) {
                            setLoading(false);
                            toast("Hợp đồng này đã có hóa đơn tháng " + month + "/" + year);
                            return;
                        }
                    }
                    Calendar due = Calendar.getInstance();
                    due.clear();
                    due.set(year, month, 5); // ngày 5 tháng sau (Calendar tháng từ 0)

                    DocumentReference ref = db.collection("invoices").document();
                    Invoice i = new Invoice();
                    i.setId(ref.getId());
                    i.setContractId(c.getId());
                    i.setRoomId(c.getRoomId());
                    i.setRoomTitle(c.getRoomTitle());
                    i.setLandlordId(c.getLandlordId());
                    i.setTenantId(c.getTenantId());
                    i.setMonth(month);
                    i.setYear(year);
                    i.setRoomFee(c.getPrice());
                    i.setElecOld(num(edtElecOld));
                    i.setElecNew(num(edtElecNew));
                    i.setElecPrice(num(edtElecPrice));
                    i.setWaterOld(num(edtWaterOld));
                    i.setWaterNew(num(edtWaterNew));
                    i.setWaterPrice(num(edtWaterPrice));
                    i.setOtherFee(num(edtOther));
                    i.setTotal(calcTotal(c.getPrice()));
                    i.setDueDate(due.getTimeInMillis());
                    i.setCreatedAt(System.currentTimeMillis());

                    if (bank != null && bank.exists()) {
                        i.setBankBin(bank.getString("bankBin"));
                        i.setBankName(bank.getString("bankName"));
                        i.setBankAccount(bank.getString("bankAccount"));
                        i.setBankHolder(bank.getString("bankHolder"));
                    } else {
                        toast("Bạn chưa nhập Thông tin nhận tiền: hóa đơn này chỉ thanh toán được bằng tiền mặt");
                    }

                    ref.set(i)
                            .addOnSuccessListener(v -> {
                                Notifier.send(c.getTenantId(), "Hóa đơn mới",
                                        "Hóa đơn tháng " + month + "/" + year + " của " + c.getRoomTitle()
                                                + " đã được tạo. Tổng: " + nf.format(i.getTotal()) + "đ");
                                toast("Đã tạo hóa đơn");
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                toast("Lỗi: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
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