package com.hoan.qlpt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Room;
import com.hoan.qlpt.util.Amenities;
import com.hoan.qlpt.util.ImageUrl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.VH> {

    public interface OnRoomClick {
        void onClick(Room room);
    }

    private final List<Room> rooms;
    private final boolean guestMode; // true: người thuê xem (hiện địa chỉ); false: chủ trọ (hiện trạng thái duyệt)
    private OnRoomClick onRoomClick;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    public RoomAdapter(List<Room> rooms) {
        this(rooms, false);
    }

    public RoomAdapter(List<Room> rooms, boolean guestMode) {
        this.rooms = rooms;
        this.guestMode = guestMode;
    }

    public void setOnRoomClick(OnRoomClick l) {
        this.onRoomClick = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Room r = rooms.get(position);
        h.tvPrice.setText(nf.format(r.getPrice()) + "đ/tháng");
        h.tvTitle.setText(r.getTitle());
        h.tvInfo.setText(areaText(r.getArea()) + " m² • " + r.getDistrict());

        h.tvVerified.setVisibility(guestMode ? View.VISIBLE : View.GONE);

        List<String> am = r.getAmenities();
        if (am == null || am.isEmpty()) {
            h.tvTags.setVisibility(View.GONE);
        } else {
            List<String> shown = new ArrayList<>();
            for (String a : am) {
                String c = Amenities.canon(a);
                if (!shown.contains(c)) shown.add(c);
                if (shown.size() == 3) break;
            }
            h.tvTags.setVisibility(View.VISIBLE);
            h.tvTags.setText(String.join(" • ", shown));
        }

        if (guestMode) {
            h.tvStatus.setText(r.getAddress());
            h.tvStatus.setTextColor(0xFF666666);
        } else {
            h.tvStatus.setText(statusText(r));
            h.tvStatus.setTextColor(0xFF009688);
        }

        if (r.getImageUrls() != null && !r.getImageUrls().isEmpty()) {
            Glide.with(h.img).load(ImageUrl.thumb(r.getImageUrls().get(0), 800))
                    .centerCrop().into(h.img);
        } else {
            Glide.with(h.img).clear(h.img);
            h.img.setImageDrawable(null);
        }

        h.itemView.setOnClickListener(v -> {
            if (onRoomClick != null) onRoomClick.onClick(r);
        });
    }

    private String areaText(double a) {
        return a == Math.rint(a) ? String.valueOf((long) a) : String.valueOf(a);
    }

    private String statusText(Room r) {
        String s;
        switch (r.getStatus()) {
            case "approved":
                s = "Đã duyệt";
                break;
            case "rejected":
                s = "Bị từ chối";
                break;
            default:
                s = "Chờ duyệt";
        }
        String text = s + " • " + ("rented".equals(r.getRentStatus()) ? "Đã thuê" : "Còn trống")
                + (r.isHidden() ? " • Đang ẩn" : "");
        if ("rejected".equals(r.getStatus())
                && r.getRejectReason() != null && !r.getRejectReason().isEmpty()) {
            text += "\nLý do: " + r.getRejectReason();
        }
        return text;
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle, tvPrice, tvInfo, tvTags, tvStatus, tvVerified;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgRoom);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvInfo = v.findViewById(R.id.tvInfo);
            tvTags = v.findViewById(R.id.tvTags);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvVerified = v.findViewById(R.id.tvVerified);
        }
    }
}