package com.hoan.qlpt.activity.landlord;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Room;
import com.hoan.qlpt.util.MoneyTextWatcher;
import com.hoan.qlpt.util.RoomDetailsForm;

import java.util.Map;

public class EditRoomActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String roomId;
    private RoomDetailsForm form;
    private TextInputEditText edtPrice, edtArea, edtDescription;
    private Button btnSave;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room);

        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null) {
            finish();
            return;
        }

        edtPrice = findViewById(R.id.edtPrice);
        edtArea = findViewById(R.id.edtArea);
        edtDescription = findViewById(R.id.edtDescription);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        MoneyTextWatcher.attach(edtPrice);
        form = new RoomDetailsForm(findViewById(R.id.roomDetails));

        // Điền sẵn dữ liệu hiện tại
        db.collection("rooms").document(roomId).get()
                .addOnSuccessListener(d -> {
                    Room r = d.toObject(Room.class);
                    if (r == null) {
                        finish();
                        return;
                    }
                    ((TextView) findViewById(R.id.tvRoomTitle)).setText(r.getTitle());
                    edtPrice.setText(String.valueOf(r.getPrice()));
                    edtArea.setText(String.valueOf(r.getArea()));
                    edtDescription.setText(r.getDescription());
                    form.fill(r);
                })
                .addOnFailureListener(e -> {
                    toast("Lỗi tải: " + e.getMessage());
                    finish();
                });

        btnSave.setOnClickListener(v -> save());
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void save() {
        long price = MoneyTextWatcher.parse(edtPrice.getText());
        double area;
        try {
            area = Double.parseDouble(text(edtArea));
        } catch (NumberFormatException e) {
            toast("Diện tích không hợp lệ");
            return;
        }
        if (price <= 0 || area <= 0) {
            toast("Giá và diện tích phải lớn hơn 0");
            return;
        }
        String err = form.validate();
        if (err != null) {
            toast(err);
            return;
        }

        Map<String, Object> m = form.toMap();
        m.put("price", price);
        m.put("area", area);
        m.put("description", text(edtDescription));

        setLoading(true);
        db.collection("rooms").document(roomId).update(m)
                .addOnSuccessListener(v -> {
                    toast("Đã lưu thay đổi");
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