package com.hoan.qlpt.activity.guest;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.tenant.BookVisitActivity;
import com.hoan.qlpt.adapter.ImageSliderAdapter;
import com.hoan.qlpt.model.Room;
import com.hoan.qlpt.util.Amenities;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoomDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "roomId";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private Room room;
    private String landlordPhone;
    private boolean favorited = false;
    private TextView tvImageIndex, tvFav;
    private ImageView ivFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        tvImageIndex = findViewById(R.id.tvImageIndex);
        tvFav = findViewById(R.id.tvFav);
        ivFav = findViewById(R.id.ivFav);

        String roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        if (roomId == null) { finish(); return; }

        db.collection("rooms").document(roomId).get()
                .addOnSuccessListener(d -> {
                    room = d.toObject(Room.class);
                    if (room == null) { toast("Không tìm thấy phòng"); finish(); return; }
                    room.setId(d.getId());
                    bind();
                })
                .addOnFailureListener(e -> { toast("Lỗi tải: " + e.getMessage()); finish(); });

        findViewById(R.id.btnFavorite).setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.btnBook).setOnClickListener(v -> {
            if (room == null) return;
            Intent i = new Intent(this, BookVisitActivity.class);
            i.putExtra("roomId", room.getId());
            startActivity(i);
        });
        findViewById(R.id.btnCall).setOnClickListener(v -> callLandlord());
    }

    // ---------- Hiển thị ----------

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private boolean empty(String s) { return s == null || s.isEmpty(); }

    private String or(String s) { return empty(s) ? "Chưa cập nhật" : s; }

    // 3500 -> "3,5k"; 28000 -> "28k"
    private String shortMoney(long v) {
        if (v >= 1000 && v % 1000 == 0) return (v / 1000) + "k";
        if (v >= 1000) return String.valueOf(v / 1000.0).replace('.', ',') + "k";
        return String.valueOf(v);
    }

    private void bind() {
        ((TextView) findViewById(R.id.tvTitle)).setText(room.getTitle());
        ((TextView) findViewById(R.id.tvPrice)).setText(nf.format(room.getPrice()) + "đ/tháng");

        double a = room.getArea();
        String area = (a == Math.rint(a) ? String.valueOf((long) a) : String.valueOf(a)) + " m²";
        ((TextView) findViewById(R.id.tvArea)).setText(empty(room.getFloor()) ? area
                : room.getFloor() + " • " + area);
        ((TextView) findViewById(R.id.tvAddress)).setText(room.getAddress() + ", " + room.getDistrict());

        List<String> urls = room.getImageUrls() == null ? new ArrayList<>() : room.getImageUrls();
        ViewPager2 vp = findViewById(R.id.vpImages);
        vp.setAdapter(new ImageSliderAdapter(urls));
        tvImageIndex.setVisibility(urls.isEmpty() ? View.GONE : View.VISIBLE);
        tvImageIndex.setText("1/" + urls.size());
        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                tvImageIndex.setText((position + 1) + "/" + urls.size());
            }
        });

        // Lưới tiện nghi: ô sáng là có
        GridLayout gl = findViewById(R.id.glAmenities);
        gl.removeAllViews();
        for (String name : Amenities.ALL) {
            gl.addView(amenityTile(name, Amenities.has(room.getAmenities(), name)));
        }

        bindDetails();

        ((TextView) findViewById(R.id.tvDescription)).setText(
                empty(room.getDescription()) ? "Chưa có mô tả" : room.getDescription());

        loadLandlord();
        loadFavorite();
    }

    private TextView amenityTile(String name, boolean on) {
        TextView t = new TextView(this);
        t.setText(name);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(11);
        t.setPadding(dp(2), dp(14), dp(2), dp(14));
        t.setBackgroundResource(on ? R.drawable.bg_amenity_on : R.drawable.bg_amenity_off);
        t.setTextColor(on ? 0xFF00695C : 0xFF9E9E9E);
        if (on) t.setTypeface(null, Typeface.BOLD);
        t.setLayoutParams(cellParams(dp(3)));
        return t;
    }

    private View cell(String label, String value) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(4), dp(6), dp(4), dp(6));

        TextView k = new TextView(this);
        k.setText(label);
        k.setTextSize(12);
        k.setTextColor(0xFF9E9E9E);
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(14);
        v.setTextColor(0xFF212121);
        v.setTypeface(null, Typeface.BOLD);
        l.addView(k);
        l.addView(v);
        l.setLayoutParams(cellParams(0));
        return l;
    }

    private GridLayout.LayoutParams cellParams(int margin) {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        lp.setMargins(margin, margin, margin, margin);
        return lp;
    }

    private void bindDetails() {
        boolean hasDetails = !empty(room.getToilet());
        findViewById(R.id.layoutDetails).setVisibility(hasDetails ? View.VISIBLE : View.GONE);
        findViewById(R.id.tvNoDetails).setVisibility(hasDetails ? View.GONE : View.VISIBLE);
        if (!hasDetails) return; // phòng cũ chưa có chi phí và điều kiện

        GridLayout costs = findViewById(R.id.glCosts);
        costs.removeAllViews();
        costs.addView(cell("Điện", shortMoney(room.getElecPrice()) + "/kWh"));
        costs.addView(cell("Nước", shortMoney(room.getWaterPrice()) + "/m³"));
        costs.addView(cell("Dịch vụ", room.getServiceFee() == 0 ? "Miễn phí"
                : shortMoney(room.getServiceFee()) + "/phòng"));
        costs.addView(cell("Giữ xe", room.getParkingFee() == 0 ? "Miễn phí"
                : shortMoney(room.getParkingFee()) + "/xe"));
        costs.addView(cell("Tối đa", room.getMaxPeople() + " người"));
        costs.addView(cell("Tối đa", room.getMaxVehicles() + " xe"));

        GridLayout info = findViewById(R.id.glInfo);
        info.removeAllViews();
        info.addView(cell("Toilet", or(room.getToilet())));
        info.addView(cell("Giờ giấc", or(room.getCurfew())));
        info.addView(cell("Máy giặt", or(room.getWasher())));
        info.addView(cell("Cửa sổ", or(room.getWindow())));
        info.addView(cell("Ban công", or(room.getBalcony())));
        info.addView(cell("Thú cưng", or(room.getPets())));
        info.addView(cell("Để xe", or(room.getParking())));
        info.addView(cell("Xe điện", or(room.getEvCharge())));
    }

    private void loadLandlord() {
        TextView tv = findViewById(R.id.tvLandlord);
        db.collection("profiles").document(room.getLandlordId()).get()
                .addOnSuccessListener(p -> {
                    String name = p.getString("fullName");
                    landlordPhone = p.getString("phone");
                    tv.setText((name == null ? "Chủ trọ" : name)
                            + (empty(landlordPhone) ? "" : " • " + landlordPhone));
                });
    }

    private void callLandlord() {
        if (empty(landlordPhone)) { toast("Chủ trọ chưa có số điện thoại"); return; }
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + landlordPhone)));
        } catch (Exception e) {
            toast("Máy không có ứng dụng gọi điện");
        }
    }

    // ---------- Yêu thích ----------

    private DocumentReference favRef() {
        return db.collection("favorites")
                .document(FirebaseAuth.getInstance().getUid() + "_" + room.getId());
    }

    private void setFav(boolean f) {
        favorited = f;
        ivFav.setImageResource(f ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        ivFav.setImageTintList(ColorStateList.valueOf(f ? 0xFFE53935 : 0xFF009688));
        tvFav.setText(f ? "Đã lưu" : "Lưu phòng");
    }

    private void loadFavorite() {
        if (FirebaseAuth.getInstance().getUid() == null) return;
        favRef().get().addOnSuccessListener(d -> setFav(d.exists()));
    }

    private void toggleFavorite() {
        if (room == null || FirebaseAuth.getInstance().getUid() == null) return;
        if (favorited) {
            favRef().delete()
                    .addOnSuccessListener(v -> setFav(false))
                    .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage()));
        } else {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", FirebaseAuth.getInstance().getUid());
            m.put("roomId", room.getId());
            m.put("createdAt", System.currentTimeMillis());
            favRef().set(m)
                    .addOnSuccessListener(v -> setFav(true))
                    .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage()));
        }
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_LONG).show(); }
}