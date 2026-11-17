package com.hoan.qlpt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Invoice;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.VH> {

    public interface OnClick {
        void onClick(Invoice invoice);
    }

    private final List<Invoice> items;
    private final OnClick onClick;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public InvoiceAdapter(List<Invoice> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Invoice i = items.get(position);
        h.tvTitle.setText(i.getRoomTitle() + " - Tháng " + i.getMonth() + "/" + i.getYear());
        h.tvTotal.setText(nf.format(i.getTotal()) + "đ");
        h.tvDue.setText("Hạn đóng: " + fmt.format(new Date(i.getDueDate())));
        String m = i.getPayMethod() == null || i.getPayMethod().isEmpty() ? ""
                : " • " + ("cash".equals(i.getPayMethod()) ? "Tiền mặt" : "Chuyển khoản");
        switch (i.getStatus()) {
            case "paid":
                h.tvStatus.setText("Đã thanh toán" + m);
                h.tvStatus.setTextColor(0xFF2E7D32);
                break;
            case "pending":
                h.tvStatus.setText("Chờ xác nhận" + m);
                h.tvStatus.setTextColor(0xFFFF9800);
                break;
            default:
                h.tvStatus.setText("Chưa thanh toán");
                h.tvStatus.setTextColor(0xFFE53935);
        }
        h.itemView.setOnClickListener(v -> onClick.onClick(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTotal, tvDue, tvStatus;

        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvDue = v.findViewById(R.id.tvDue);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}