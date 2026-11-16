package com.hoan.qlpt.activity.tenant;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Invoice;
import com.hoan.qlpt.util.Notifier;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceDetailActivity extends AppCompatActivity {

    private Invoice invoice;
    private Button btnPaid;
    private RadioButton rbTransfer, rbCash;
    private View layoutQr, layoutPay;
    private TextView tvCashNote;
    private boolean hasBank = false;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        btnPaid = findViewById(R.id.btnPaid);
        rbTransfer = findViewById(R.id.rbTransfer);
        rbCash = findViewById(R.id.rbCash);
        layoutQr = findViewById(R.id.layoutQr);
        layoutPay = findViewById(R.id.layoutPay);
        tvCashNote = findViewById(R.id.tvCashNote);

        String id = getIntent().getStringExtra("invoiceId");
        if (id == null) { finish(); return; }

        FirebaseFirestore.getInstance().collection("invoices").document(id).get()
                .addOnSuccessListener(d -> {
                    invoice = d.toObject(Invoice.class);
                    if (invoice == null) { finish(); return; }
                    invoice.setId(d.getId());
                    bind();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });

        ((RadioGroup) findViewById(R.id.rgMethod)).setOnCheckedChangeListener((g, c) -> updateMethodUi());
        btnPaid.setOnClickListener(v -> reportPaid());
    }

    private String methodName(String m) {
        if ("cash".equals(m)) return "Tiền mặt";
        if ("transfer".equals(m)) return "Chuyển khoản";
        return "";
    }

    private void setText(int id, String s) { ((TextView) findViewById(id)).setText(s); }

    private String money(long v) { return nf.format(v) + "đ"; }

    private void bind() {
        Invoice i = invoice;
        ((TextView) findViewById(R.id.tvTitle)).setText(
                i.getRoomTitle() + "\nHóa đơn tháng " + i.getMonth() + "/" + i.getYear());

        TextView st = findViewById(R.id.tvStatus);
        switch (i.getStatus()) {
            case "paid": st.setText("Đã thanh toán"); st.setTextColor(0xFF2E7D32); break;
            case "pending": st.setText("Chờ chủ trọ xác nhận"); st.setTextColor(0xFFFF9800); break;
            default: st.setText("Chưa thanh toán"); st.setTextColor(0xFFE53935);
        }

        TextView tvMethod = findViewById(R.id.tvMethod);
        String mn = methodName(i.getPayMethod());
        tvMethod.setVisibility(!"unpaid".equals(i.getStatus()) && !mn.isEmpty() ? View.VISIBLE : View.GONE);
        tvMethod.setText("Hình thức: " + mn);

        long elecUse = Math.max(0, i.getElecNew() - i.getElecOld());
        long waterUse = Math.max(0, i.getWaterNew() - i.getWaterOld());

        setText(R.id.tvRoomFeeAmount, money(i.getRoomFee()));
        setText(R.id.tvElecDetail, nf.format(i.getElecOld()) + " → " + nf.format(i.getElecNew())
                + " (" + elecUse + " số x " + nf.format(i.getElecPrice()) + "đ)");
        setText(R.id.tvElecAmount, money(elecUse * i.getElecPrice()));
        setText(R.id.tvWaterDetail, nf.format(i.getWaterOld()) + " → " + nf.format(i.getWaterNew())
                + " (" + waterUse + " khối x " + nf.format(i.getWaterPrice()) + "đ)");
        setText(R.id.tvWaterAmount, money(waterUse * i.getWaterPrice()));
        setText(R.id.tvOtherAmount, money(i.getOtherFee()));
        setText(R.id.tvTotal, money(i.getTotal()));
        setText(R.id.tvDue, "Hạn đóng: "
                + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(i.getDueDate())));

        // Phần chọn hình thức chỉ hiện khi chưa thanh toán
        boolean unpaid = "unpaid".equals(i.getStatus());
        layoutPay.setVisibility(unpaid ? View.VISIBLE : View.GONE);
        if (!unpaid) return;

        hasBank = i.getBankBin() != null && !i.getBankBin().isEmpty()
                && i.getBankAccount() != null && !i.getBankAccount().isEmpty();
        rbTransfer.setEnabled(hasBank);
        rbTransfer.setText(hasBank ? "Chuyển khoản (quét mã QR)"
                : "Chuyển khoản (chủ trọ chưa cập nhật tài khoản)");
        if (hasBank) { rbTransfer.setChecked(true); setupQr(); }
        else rbCash.setChecked(true);
        updateMethodUi();
    }

    private String transferContent() {
        return "HD" + invoice.getId().substring(0, 6).toUpperCase(Locale.ROOT)
                + "T" + invoice.getMonth();
    }

    private void setupQr() {
        String content = transferContent();
        String url = "https://img.vietqr.io/image/" + invoice.getBankBin() + "-"
                + invoice.getBankAccount() + "-compact2.png?amount=" + invoice.getTotal()
                + "&addInfo=" + Uri.encode(content)
                + "&accountName=" + Uri.encode(invoice.getBankHolder() == null ? "" : invoice.getBankHolder());
        Glide.with(this).load(url).into((ImageView) findViewById(R.id.imgQr));

        ((TextView) findViewById(R.id.tvBank)).setText(invoice.getBankName() + "\n"
                + invoice.getBankAccount() + " - " + invoice.getBankHolder()
                + "\nSố tiền: " + nf.format(invoice.getTotal()) + "đ"
                + "\nNội dung: " + content);

        findViewById(R.id.btnCopy).setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("noidung", content));
            Toast.makeText(this, "Đã sao chép: " + content, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateMethodUi() {
        boolean transfer = rbTransfer.isChecked();
        layoutQr.setVisibility(transfer ? View.VISIBLE : View.GONE);
        tvCashNote.setVisibility(transfer ? View.GONE : View.VISIBLE);
        btnPaid.setText(transfer ? "Tôi đã chuyển khoản" : "Tôi đã trả tiền mặt");
    }

    private void reportPaid() {
        if (invoice == null) return;
        String method = rbTransfer.isChecked() ? "transfer" : "cash";
        FirebaseFirestore.getInstance().collection("invoices").document(invoice.getId())
                .update("status", "pending", "payMethod", method)
                .addOnSuccessListener(x -> {
                    Notifier.send(invoice.getLandlordId(), "Người thuê đã báo thanh toán",
                            invoice.getRoomTitle() + " - hóa đơn tháng " + invoice.getMonth() + "/"
                                    + invoice.getYear() + " (" + methodName(method) + ") đang chờ bạn xác nhận");
                    invoice.setStatus("pending");
                    invoice.setPayMethod(method);
                    bind();
                    Toast.makeText(this, "Đã báo thanh toán, chờ chủ trọ xác nhận", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}