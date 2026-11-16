package com.hoan.qlpt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hoan.qlpt.R;
import com.hoan.qlpt.model.ApprovalItem;

import java.util.List;

public class ApprovalAdapter extends RecyclerView.Adapter<ApprovalAdapter.VH> {

    public interface Listener {
        void onApprove(ApprovalItem item);
        void onReject(ApprovalItem item);
    }

    private final List<ApprovalItem> items;
    private final Listener listener;

    public ApprovalAdapter(List<ApprovalItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_approval, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ApprovalItem it = items.get(position);
        h.tvTitle.setText(it.title);
        h.tvLine1.setText(it.line1);
        h.tvLine2.setText(it.line2);
        h.btnApprove.setOnClickListener(v -> listener.onApprove(it));
        h.btnReject.setOnClickListener(v -> listener.onReject(it));
        h.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onClick(it); });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public interface OnItemClick { void onClick(ApprovalItem item); }

    private boolean showActions = true;
    private OnItemClick onItemClick;

    public void setShowActions(boolean show) { showActions = show; }
    public void setOnItemClick(OnItemClick l) { onItemClick = l; }


    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLine1, tvLine2;
        View btnApprove, btnReject;
        View layoutActions;

        VH(View v) {
            super(v);

            layoutActions = v.findViewById(R.id.layoutActions);

            tvTitle = v.findViewById(R.id.tvTitle);
            tvLine1 = v.findViewById(R.id.tvLine1);
            tvLine2 = v.findViewById(R.id.tvLine2);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
        }
    }

}