package com.hoan.qlpt.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.util.ImageSourceHelper;
import com.hoan.qlpt.util.ImageUploader;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid;
    private String currentAvatar = "";
    private Uri newAvatar;
    private ImageView imgAvatar;
    private TextInputEditText edtName, edtPhone;
    private Button btnSave;
    private ProgressBar progress;
    private ImageSourceHelper imageSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }

        imgAvatar = findViewById(R.id.imgAvatar);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        ((TextInputEditText) findViewById(R.id.edtEmail)).setText(
                FirebaseAuth.getInstance().getCurrentUser().getEmail());

        db.collection("profiles").document(uid).get().addOnSuccessListener(d -> {
            edtName.setText(d.getString("fullName"));
            edtPhone.setText(d.getString("phone"));
            String url = d.getString("avatarUrl");
            if (url != null && !url.isEmpty()) {
                currentAvatar = url;
                Glide.with(this).load(com.hoan.qlpt.util.ImageUrl.thumb(url, 200)).circleCrop().into(imgAvatar);
            }
        });

        imageSource = new ImageSourceHelper(this, 1, uris -> {
            if (uris.isEmpty()) return;
            newAvatar = uris.get(0);
            Glide.with(this).load(newAvatar).circleCrop().into(imgAvatar);
        });
        View.OnClickListener pick = v -> imageSource.show();
        findViewById(R.id.btnPickAvatar).setOnClickListener(pick);
        imgAvatar.setOnClickListener(pick);

        btnSave.setOnClickListener(v -> save());
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void save() {
        String name = text(edtName), phone = text(edtPhone);
        if (name.isEmpty()) {
            toast("Nhập họ và tên");
            return;
        }
        if (!phone.matches("0\\d{9,10}")) {
            toast("Số điện thoại không hợp lệ");
            return;
        }

        setLoading(true);
        if (newAvatar == null) {
            write(name, phone, currentAvatar);
        } else {
            ImageUploader.uploadAll(this, java.util.Collections.singletonList(newAvatar), null,
                    urls -> write(name, phone, urls.get(0)),
                    msg -> {
                        setLoading(false);
                        toast("Lỗi tải ảnh: " + msg);
                    });
        }
    }

    private void write(String name, String phone, String avatarUrl) {
        Map<String, Object> m = new HashMap<>();
        m.put("fullName", name);
        m.put("phone", phone);
        m.put("avatarUrl", avatarUrl);
        db.collection("profiles").document(uid).update(m)
                .addOnSuccessListener(v -> {
                    toast("Đã lưu hồ sơ");
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Lỗi: " + e.getMessage());
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}