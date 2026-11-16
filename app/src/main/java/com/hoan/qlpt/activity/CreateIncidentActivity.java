package com.hoan.qlpt.activity;

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
import com.hoan.qlpt.model.Incident;
import com.hoan.qlpt.util.ImageUploader;
import com.hoan.qlpt.util.Notifier;

import java.util.ArrayList;
import java.util.List;

public class CreateIncidentActivity extends AppCompatActivity {

    private static final int MAX_IMAGES = 4;
    private static final String[] CATEGORIES = {"Điện", "Nước", "Thiết bị", "Khác"};

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<Uri> selectedUris = new ArrayList<>();
    private ImagePreviewAdapter previewAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> picker;
    private DocumentSnapshot contract;
    private Spinner spCategory;
    private TextInputEditText edtTitle, edtDescription;
    private TextView tvImageCount;
    private Button btnSubmit;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_incident);

        spCategory = findViewById(R.id.spCategory);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        tvImageCount = findViewById(R.id.tvImageCount);
        btnSubmit = findViewById(R.id.btnSubmit);
        progress = findViewById(R.id.progress);
        spCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORIES));

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

        btnSubmit.setEnabled(false);
        loadContract();
        btnSubmit.setOnClickListener(v -> submit());
    }

    // Chỉ báo được khi có hợp đồng đang hiệu lực
    private void loadContract() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }
        db.collection("contracts")
                .whereEqualTo("tenantId", uid)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        toast("Bạn chưa có hợp đồng đang hiệu lực nên chưa báo sự cố được");
                        finish();
                        return;
                    }
                    contract = snap.getDocuments().get(0);
                    ((TextView) findViewById(R.id.tvRoom)).setText(
                            "Phòng: " + contract.getString("roomTitle"));
                    btnSubmit.setEnabled(true);
                })
                .addOnFailureListener(e -> { toast("Lỗi: " + e.getMessage()); finish(); });
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void submit() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || contract == null) return;
        String title = text(edtTitle), desc = text(edtDescription);
        if (title.length() < 3) { toast("Nhập tiêu đề (ít nhất 3 ký tự)"); return; }
        if (desc.length() < 10) { toast("Mô tả cần ít nhất 10 ký tự"); return; }

        setLoading(true);
        ImageUploader.uploadAll(this, new ArrayList<>(selectedUris),
                (d, t) -> btnSubmit.setText("Đang tải ảnh " + d + "/" + t),
                urls -> {
                    DocumentReference ref = db.collection("incidents").document();
                    Incident i = new Incident();
                    i.setId(ref.getId());
                    i.setContractId(contract.getId());
                    i.setRoomId(contract.getString("roomId"));
                    i.setRoomTitle(contract.getString("roomTitle"));
                    i.setLandlordId(contract.getString("landlordId"));
                    i.setTenantId(uid);
                    i.setTenantName(contract.getString("tenantName"));
                    i.setCategory(CATEGORIES[spCategory.getSelectedItemPosition()]);
                    i.setTitle(title);
                    i.setDescription(desc);
                    i.setNote("");
                    i.setImageUrls(urls);
                    i.setCreatedAt(System.currentTimeMillis());
                    i.setUpdatedAt(i.getCreatedAt());

                    ref.set(i)
                            .addOnSuccessListener(v -> {
                                Notifier.send(i.getLandlordId(), "Sự cố mới",
                                        i.getTenantName() + " báo: " + title + " (" + i.getRoomTitle() + ")");
                                toast("Đã gửi báo cáo sự cố");
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                btnSubmit.setText("Gửi báo cáo");
                                toast("Lỗi: " + e.getMessage());
                            });
                },
                msg -> {
                    setLoading(false);
                    btnSubmit.setText("Gửi báo cáo");
                    toast("Lỗi tải ảnh: " + msg);
                });
    }

    private interface UrlsCallback { void onDone(List<String> urls); }

    private void uploadAll(List<Uri> pending, List<String> done, UrlsCallback cb) {
        if (pending.isEmpty()) { cb.onDone(done); return; }
        Uri next = pending.remove(0);
        ImageUploader.upload(next, new ImageUploader.Callback() {
            @Override public void onSuccess(String url) { done.add(url); uploadAll(pending, done, cb); }
            @Override public void onError(String message) { setLoading(false); toast("Lỗi tải ảnh: " + message); }
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

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_LONG).show(); }
}