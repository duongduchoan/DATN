package com.hoan.qlpt.activity.landlord;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.House;

public class AddHouseActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtDistrict, edtAddress, edtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_house);
        edtName = findViewById(R.id.edtName);
        edtDistrict = findViewById(R.id.edtDistrict);
        edtAddress = findViewById(R.id.edtAddress);
        edtDescription = findViewById(R.id.edtDescription);
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void save() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        if (text(edtName).isEmpty() || text(edtDistrict).isEmpty() || text(edtAddress).isEmpty()) {
            Toast.makeText(this, "Nhập đủ tên, quận/huyện, địa chỉ", Toast.LENGTH_LONG).show();
            return;
        }
        DocumentReference ref = FirebaseFirestore.getInstance().collection("houses").document();
        House h = new House();
        h.setId(ref.getId());
        h.setLandlordId(uid);
        h.setName(text(edtName));
        h.setDistrict(text(edtDistrict));
        h.setAddress(text(edtAddress));
        h.setDescription(text(edtDescription));
        h.setCreatedAt(System.currentTimeMillis());
        ref.set(h)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đã lưu nhà trọ", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
