package com.hoan.qlpt.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.AppNotification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnClick {
        void onClick(AppNotification n);
    }

    private final List<AppNotification> items;
    private final OnClick onClick;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    public NotificationAdapter(List<AppNotification> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AppNotification n = items.get(position);
        h.tvTitle.setText(n.getTitle());
        h.tvBody.setText(n.getBody());
        h.tvTime.setText(fmt.format(new Date(n.getCreatedAt())));
        h.tvDot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);
        h.tvTitle.setTypeface(null, n.isRead() ? Typeface.NORMAL : Typeface.BOLD);
        ((MaterialCardView) h.itemView).setCardBackgroundColor(
                n.isRead() ? 0xFFFFFFFF : 0xFFE0F2F1);
        h.itemView.setOnClickListener(v -> onClick.onClick(n));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDot, tvTitle, tvBody, tvTime;

        VH(View v) {
            super(v);
            tvDot = v.findViewById(R.id.tvDot);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvBody = v.findViewById(R.id.tvBody);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }
}