package com.hoan.qlpt.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.ImageSliderAdapter;
import com.hoan.qlpt.model.Incident;
import com.hoan.qlpt.util.Notifier;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IncidentDetailActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Incident incident;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_detail);

        String id = getIntent().getStringExtra("incidentId");
        if (id == null) {
            finish();
            return;
        }

        db.collection("incidents").document(id).get()
                .addOnSuccessListener(d -> {
                    incident = d.toObject(Incident.class);
                    if (incident == null) {
                        finish();
                        return;
                    }
                    incident.setId(d.getId());
                    bind();
                })
                .addOnFailureListener(e -> {
                    toast("Lỗi tải: " + e.getMessage());
                    finish();
                });

        findViewById(R.id.btnProcessing).setOnClickListener(v -> update("processing"));
        findViewById(R.id.btnDone).setOnClickListener(v -> update("done"));
    }

    private void bind() {
        ((TextView) findViewById(R.id.tvTitle)).setText(incident.getTitle());

        TextView st = findViewById(R.id.tvStatus);
        st.setText(Incident.statusText(incident.getStatus()));
        switch (incident.getStatus()) {
            case "done":
                st.setTextColor(0xFF2E7D32);
                break;
            case "processing":
                st.setTextColor(0xFFFF9800);
                break;
            default:
                st.setTextColor(0xFFE53935);
        }

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(incident.getCreatedAt()));
        boolean hasNote = incident.getNote() != null && !incident.getNote().isEmpty();
        ((TextView) findViewById(R.id.tvInfo)).setText(
                "Phòng: " + incident.getRoomTitle()
                        + "\nLoại: " + incident.getCategory()
                        + "\nNgười báo: " + incident.getTenantName()
                        + "\nGửi lúc: " + date
                        + "\n\n" + incident.getDescription()
                        + (hasNote ? "\n\nGhi chú của chủ trọ: " + incident.getNote() : ""));

        ViewPager2 vp = findViewById(R.id.vpImages);
        if (incident.getImageUrls() != null && !incident.getImageUrls().isEmpty()) {
            vp.setVisibility(View.VISIBLE);
            vp.setAdapter(new ImageSliderAdapter(incident.getImageUrls()));
        } else {
            vp.setVisibility(View.GONE);
        }

        // Chỉ chủ trọ của phòng này, và chỉ khi chưa xong, mới thấy phần xử lý
        String me = FirebaseAuth.getInstance().getUid();
        boolean isLandlord = me != null && me.equals(incident.getLandlordId());
        findViewById(R.id.layoutLandlord).setVisibility(
                isLandlord && !"done".equals(incident.getStatus()) ? View.VISIBLE : View.GONE);
    }

    private void update(String status) {
        String note = ((TextInputEditText) findViewById(R.id.edtNote)).getText() == null ? ""
                : ((TextInputEditText) findViewById(R.id.edtNote)).getText().toString().trim();
        if (note.isEmpty() && incident.getNote() != null) note = incident.getNote();
        final String finalNote = note;

        db.collection("incidents").document(incident.getId())
                .update("status", status, "note", finalNote, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(v -> {
                    Notifier.send(incident.getTenantId(),
                            "done".equals(status) ? "Sự cố đã xử lý xong" : "Sự cố đang được xử lý",
                            incident.getTitle() + (finalNote.isEmpty() ? "" : "\nGhi chú: " + finalNote));
                    incident.setStatus(status);
                    incident.setNote(finalNote);
                    bind();
                    toast("Đã cập nhật trạng thái");
                })
                .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage()));
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}