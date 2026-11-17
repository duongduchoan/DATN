package com.hoan.qlpt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Room;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PendingRoomAdapter extends RecyclerView.Adapter<PendingRoomAdapter.VH> {

    public interface Listener {
        void onApprove(Room room);

        void onReject(Room room);
    }

    private final List<Room> rooms;
    private final Listener listener;

    public PendingRoomAdapter(List<Room> rooms, Listener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_room, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Room r = rooms.get(position);
        h.tvTitle.setText(r.getTitle());
        h.tvPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(r.getPrice()) + "đ/tháng");
        h.tvInfo.setText(r.getArea() + " m² • " + r.getDistrict() + "\n" + r.getAddress());

        if (r.getImageUrls() != null && !r.getImageUrls().isEmpty()) {
            Glide.with(h.img).load(com.hoan.qlpt.util.ImageUrl.thumb(r.getImageUrls().get(0), 300)).centerCrop().into(h.img);
        } else {
            h.img.setImageDrawable(null);
        }
        h.btnApprove.setOnClickListener(v -> listener.onApprove(r));
        h.btnReject.setOnClickListener(v -> listener.onReject(r));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle, tvPrice, tvInfo;
        Button btnApprove, btnReject;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgRoom);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvInfo = v.findViewById(R.id.tvInfo);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
        }
    }
}