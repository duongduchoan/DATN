package com.hoan.qlpt.activity.tenant;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.ImagePreviewAdapter;
import com.hoan.qlpt.model.RoleRequest;
import com.hoan.qlpt.util.ImageUploader;

import java.util.ArrayList;
import java.util.List;

public class RequestLandlordActivity extends AppCompatActivity {

    private static final int MAX_IMAGES = 3;
    private final List<Uri> selectedUris = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImagePreviewAdapter previewAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> picker;
    private TextInputEditText edtName, edtPhone, edtCccd, edtHouseCount, edtRoomCount,
            edtHouseAddress, edtDescription;
    private TextView tvImageCount;
    private CheckBox cbConfirm;
    private Button btnSubmit;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_landlord);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtCccd = findViewById(R.id.edtCccd);
        edtHouseCount = findViewById(R.id.edtHouseCount);
        edtRoomCount = findViewById(R.id.edtRoomCount);
        edtHouseAddress = findViewById(R.id.edtHouseAddress);
        edtDescription = findViewById(R.id.edtDescription);
        tvImageCount = findViewById(R.id.tvImageCount);
        cbConfirm = findViewById(R.id.cbConfirm);
        btnSubmit = findViewById(R.id.btnSubmit);
        progress = findViewById(R.id.progress);

        RecyclerView rv = findViewById(R.id.rvImages);
        previewAdapter = new ImagePreviewAdapter(selectedUris, pos -> {
            selectedUris.remove(pos);
            previewAdapter.notifyDataSetChanged();
            updateImageCount();
        });
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(previewAdapter);
        updateImageCount();

        picker = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(MAX_IMAGES), uris -> {
                    boolean over = false;
                    for (Uri u : uris) {
                        if (selectedUris.contains(u)) continue; // bỏ ảnh trùng
                        if (selectedUris.size() >= MAX_IMAGES) {
                            over = true;
                            break;
                        }
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

        // Điền sẵn từ hồ sơ
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }
        db.collection("profiles").document(uid).get().addOnSuccessListener(p -> {
            edtName.setText(p.getString("fullName"));
            edtPhone.setText(p.getString("phone"));
        });

        btnSubmit.setOnClickListener(v -> submit());
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void submit() {
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu == null) return;

        String name = text(edtName), phone = text(edtPhone), cccd = text(edtCccd);
        String address = text(edtHouseAddress), desc = text(edtDescription);

        if (name.isEmpty()) {
            toast("Nhập họ tên");
            return;
        }
        if (!phone.matches("0\\d{9,10}")) {
            toast("Số điện thoại không hợp lệ");
            return;
        }
        if (!cccd.matches("\\d{9}|\\d{12}")) {
            toast("CCCD phải gồm 9 hoặc 12 số");
            return;
        }

        int houses, rooms;
        try {
            houses = Integer.parseInt(text(edtHouseCount));
            rooms = Integer.parseInt(text(edtRoomCount));
        } catch (NumberFormatException e) {
            toast("Nhập số nhà trọ và tổng số phòng");
            return;
        }
        if (houses < 1 || rooms < 1) {
            toast("Số nhà trọ và số phòng phải từ 1 trở lên");
            return;
        }
        if (address.isEmpty()) {
            toast("Nhập địa chỉ nhà trọ chính");
            return;
        }
        if (desc.length() < 20) {
            toast("Phần giới thiệu cần ít nhất 20 ký tự");
            return;
        }
        if (selectedUris.isEmpty()) {
            toast("Chọn ít nhất 1 ảnh");
            return;
        }
        if (!cbConfirm.isChecked()) {
            toast("Bạn cần tích vào ô cam kết");
            return;
        }

        setLoading(true);
        // Chặn gửi trùng khi đang có yêu cầu chờ duyệt
        db.collection("roleRequests")
                .whereEqualTo("uid", fu.getUid())
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        setLoading(false);
                        toast("Bạn đã có một yêu cầu đang chờ duyệt");
                        return;
                    }
                    ImageUploader.uploadAll(this, new ArrayList<>(selectedUris),
                            (d, t) -> btnSubmit.setText("Đang tải ảnh " + d + "/" + t),
                            urls -> {
                                DocumentReference ref = db.collection("roleRequests").document();
                                RoleRequest r = new RoleRequest();
                                r.setId(ref.getId());
                                r.setUid(fu.getUid());
                                r.setFullName(name);
                                r.setPhone(phone);
                                r.setEmail(fu.getEmail());
                                r.setCccd(cccd);
                                r.setHouseCount(houses);
                                r.setRoomCount(rooms);
                                r.setHouseAddress(address);
                                r.setDescription(desc);
                                r.setImageUrls(urls);
                                r.setCreatedAt(System.currentTimeMillis());
                                ref.set(r)
                                        .addOnSuccessListener(v -> {
                                            toast("Đã gửi yêu cầu, chờ admin xem xét");
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            setLoading(false);
                                            btnSubmit.setText("Gửi yêu cầu");
                                            toast("Lỗi: " + e.getMessage());
                                        });
                            },
                            msg -> {
                                setLoading(false);
                                btnSubmit.setText("Gửi yêu cầu");
                                toast("Lỗi tải ảnh: " + msg);
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Lỗi: " + e.getMessage());
                });
    }

    private void updateImageCount() {
        tvImageCount.setText(selectedUris.isEmpty() ? "Chưa chọn ảnh"
                : "Đã chọn " + selectedUris.size() + "/" + MAX_IMAGES + " ảnh");
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}