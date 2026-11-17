package com.hoan.qlpt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hoan.qlpt.R;
import com.hoan.qlpt.model.RentalRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.VH> {

    public interface Listener {
        void onApprove(RentalRequest r);

        void onReject(RentalRequest r);

        void onCreateContract(RentalRequest r);
    }

    public interface OnItemClick {
        void onClick(RentalRequest r);
    }

    private OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick l) {
        onItemClick = l;
    }

    private final List<RentalRequest> items;
    private final Listener listener;
    private final SimpleDateFormat fmtTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat fmtDay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public RequestAdapter(List<RentalRequest> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RentalRequest r = items.get(position);
        h.tvRoom.setText(r.getRoomTitle());
        h.tvTenant.setText(r.getTenantName() + " • " + r.getTenantPhone());

        if (r.getVisitDate() > 0) {
            h.tvVisit.setVisibility(View.VISIBLE);
            h.tvVisit.setText("Lịch xem phòng: " + fmtDay.format(new Date(r.getVisitDate()))
                    + "\n" + r.getPeopleCount() + " người • " + r.getVehicleCount() + " xe • Thú cưng: "
                    + (r.getPets() == null || r.getPets().isEmpty() ? "Không" : r.getPets())
                    + "\nDự kiến vào ở: " + r.getExpectedMoveIn());
        } else {
            h.tvVisit.setVisibility(View.GONE); // yêu cầu cũ chưa có thông tin lịch
        }

        h.tvMessage.setText(r.getMessage() == null || r.getMessage().isEmpty()
                ? "(Không có ghi chú)" : "Ghi chú: " + r.getMessage());
        h.tvDate.setText("Gửi lúc " + fmtTime.format(new Date(r.getCreatedAt())));

        String st = r.getStatus();
        boolean pending = "pending".equals(st);
        h.layoutActions.setVisibility(pending ? View.VISIBLE : View.GONE);
        h.tvResult.setVisibility(pending ? View.GONE : View.VISIBLE);
        h.btnContract.setVisibility("approved".equals(st) ? View.VISIBLE : View.GONE);

        if ("approved".equals(st)) {
            h.tvResult.setText("Đã xác nhận lịch xem");
            h.tvResult.setTextColor(0xFF2E7D32);
        } else if ("contracted".equals(st)) {
            h.tvResult.setText("Đã tạo hợp đồng");
            h.tvResult.setTextColor(0xFF009688);
        } else if ("cancelled".equals(st)) {
            h.tvResult.setText("Người thuê đã hủy");
            h.tvResult.setTextColor(0xFF9E9E9E);
        } else {
            h.tvResult.setText("Đã từ chối");
            h.tvResult.setTextColor(0xFFE53935);
        }

        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(r);
        });
        h.btnApprove.setOnClickListener(v -> listener.onApprove(r));
        h.btnReject.setOnClickListener(v -> listener.onReject(r));
        h.btnContract.setOnClickListener(v -> listener.onCreateContract(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRoom, tvTenant, tvVisit, tvMessage, tvDate, tvResult;
        View layoutActions, btnApprove, btnReject, btnContract;

        VH(View v) {
            super(v);
            tvRoom = v.findViewById(R.id.tvRoom);
            tvTenant = v.findViewById(R.id.tvTenant);
            tvVisit = v.findViewById(R.id.tvVisit);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvDate = v.findViewById(R.id.tvDate);
            tvResult = v.findViewById(R.id.tvResult);
            layoutActions = v.findViewById(R.id.layoutActions);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
            btnContract = v.findViewById(R.id.btnContract);
        }
    }
}