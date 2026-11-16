package com.hoan.qlpt.activity.landlord;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.ImagePreviewAdapter;
import com.hoan.qlpt.model.House;
import com.hoan.qlpt.model.Room;
import com.hoan.qlpt.util.ImageUploader;
import com.hoan.qlpt.util.MoneyTextWatcher;
import com.hoan.qlpt.util.RoomDetailsForm;

import java.util.ArrayList;
import java.util.List;

public class AddRoomActivity extends AppCompatActivity {

    private static final int MAX_IMAGES = 5;

    private final List<Uri> selectedUris = new ArrayList<>();
    private final List<House> houses = new ArrayList<>();
    private ImagePreviewAdapter previewAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> picker;
    private RoomDetailsForm form;
    private Spinner spHouse;
    private TextInputEditText edtRoomNumber, edtPrice, edtArea, edtDescription;
    private TextView tvImageCount;
    private Button btnSave;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        spHouse = findViewById(R.id.spHouse);
        edtRoomNumber = findViewById(R.id.edtRoomNumber);
        edtPrice = findViewById(R.id.edtPrice);
        edtArea = findViewById(R.id.edtArea);
        edtDescription = findViewById(R.id.edtDescription);
        tvImageCount = findViewById(R.id.tvImageCount);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        MoneyTextWatcher.attach(edtPrice);
        form = new RoomDetailsForm(findViewById(R.id.roomDetails));

        findViewById(R.id.btnAddHouse).setOnClickListener(v ->
                startActivity(new Intent(this, AddHouseActivity.class)));
        findViewById(R.id.btnMyHouses).setOnClickListener(v ->
                startActivity(new Intent(this, MyHousesActivity.class)));

        RecyclerView rv = findViewById(R.id.rvImages);
        previewAdapter = new ImagePreviewAdapter(selectedUris, pos -> {
            selectedUris.remove(pos);
            previewAdapter.notifyDataSetChanged();
            updateImageCount();
        });
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(previewAdapter);

        picker = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(MAX_IMAGES), uris -> {
                    boolean over = false;
                    for (Uri u : uris) {
                        if (selectedUris.contains(u)) continue;
                        if (selectedUris.size() >= MAX_IMAGES) { over = true; break; }
                        selectedUris.add(u);
                    }
                    previewAdapter.notifyDataSetChanged();
                    updateImageCount();
                    if (over) toast("Chỉ chọn tối đa " + MAX_IMAGES + " ảnh");
                });
        findViewById(R.id.btnPickImages).setOnClickListener(v -> {
            if (selectedUris.size() >= MAX_IMAGES) {
                toast("Đã đủ " + MAX_IMAGES + " ảnh. Hãy xóa bớt trước khi chọn thêm");
                return;
            }
            picker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnSave.setOnClickListener(v -> save());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHouses(); // quay lại từ màn thêm nhà trọ thì danh sách tự mới
    }

    private void updateImageCount() {
        tvImageCount.setText(selectedUris.isEmpty() ? "Chưa chọn ảnh"
                : "Đã chọn " + selectedUris.size() + "/" + MAX_IMAGES + " ảnh");
    }

    // Chỉ hiện các nhà trọ đã được admin duyệt
    private void loadHouses() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance().collection("houses")
                .whereEqualTo("landlordId", uid)
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(snap -> {
                    houses.clear();
                    List<String> labels = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        House h = d.toObject(House.class);
                        if (h == null) continue;
                        h.setId(d.getId());
                        houses.add(h);
                        labels.add(h.getName());
                    }
                    spHouse.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, labels));
                });
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void save() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        int hp = spHouse.getSelectedItemPosition();
        if (houses.isEmpty() || hp < 0) {
            toast("Chưa có nhà trọ nào được duyệt. Hãy thêm nhà trọ và chờ admin duyệt");
            return;
        }
        House house = houses.get(hp);

        String roomNumber = text(edtRoomNumber);
        long price = MoneyTextWatcher.parse(edtPrice.getText());
        double area;
        try {
            area = Double.parseDouble(text(edtArea));
        } catch (NumberFormatException e) {
            toast("Diện tích không hợp lệ");
            return;
        }
        if (roomNumber.isEmpty()) { toast("Nhập số phòng"); return; }
        if (price <= 0 || area <= 0) { toast("Giá và diện tích phải lớn hơn 0"); return; }
        String err = form.validate();
        if (err != null) { toast(err); return; }

        Room room = new Room();
        room.setLandlordId(uid);
        room.setHouseId(house.getId());
        room.setHouseName(house.getName());
        room.setRoomNumber(roomNumber);
        room.setTitle(house.getName() + " - Phòng " + roomNumber);
        room.setPrice(price);
        room.setArea(area);
        room.setDistrict(house.getDistrict());
        room.setAddress(house.getAddress());
        room.setDescription(text(edtDescription));
        room.setCreatedAt(System.currentTimeMillis());
        form.apply(room);

        setLoading(true);
        DocumentReference ref = FirebaseFirestore.getInstance().collection("rooms").document();
        room.setId(ref.getId());

        ImageUploader.uploadAll(this, new ArrayList<>(selectedUris),
                (d, t) -> btnSave.setText("Đang tải ảnh " + d + "/" + t),
                urls -> {
                    room.setImageUrls(urls);
                    ref.set(room)
                            .addOnSuccessListener(v -> {
                                toast("Đã lưu phòng, đang chờ admin duyệt");
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                btnSave.setText("Lưu phòng");
                                toast("Lỗi lưu phòng: " + e.getMessage());
                            });
                },
                msg -> {
                    setLoading(false);
                    btnSave.setText("Lưu phòng");
                    toast("Lỗi tải ảnh: " + msg);
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); }
}