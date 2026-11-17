package com.hoan.qlpt.activity.tenant;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.RentalRequest;
import com.hoan.qlpt.model.Room;
import com.hoan.qlpt.util.Notifier;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookVisitActivity extends AppCompatActivity {

    private static final String[] MOVE_IN = {"Ngay lập tức", "Trong 1 tuần", "Trong 2 tuần",
            "Trong 1 tháng", "Chưa xác định"};

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Room room;
    private long visitDate = 0;
    private Spinner spPeople, spVehicles, spPet, spMoveIn;
    private TextInputEditText edtName, edtPhone, edtNote;
    private Button btnVisitDate, btnSubmit;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_visit);

        spPeople = findViewById(R.id.spPeople);
        spVehicles = findViewById(R.id.spVehicles);
        spPet = findViewById(R.id.spPet);
        spMoveIn = findViewById(R.id.spMoveIn);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtNote = findViewById(R.id.edtNote);
        btnVisitDate = findViewById(R.id.btnVisitDate);
        btnSubmit = findViewById(R.id.btnSubmit);
        progress = findViewById(R.id.progress);

        spPet.setAdapter(adapter(new String[]{"Không", "Có"}));
        spMoveIn.setAdapter(adapter(MOVE_IN));
        btnSubmit.setEnabled(false);

        String roomId = getIntent().getStringExtra("roomId");
        String uid = FirebaseAuth.getInstance().getUid();
        if (roomId == null || uid == null) {
            finish();
            return;
        }

        // Điền sẵn họ tên, số điện thoại từ hồ sơ
        db.collection("profiles").document(uid).get().addOnSuccessListener(p -> {
            edtName.setText(p.getString("fullName"));
            edtPhone.setText(p.getString("phone"));
        });

        db.collection("rooms").document(roomId).get()
                .addOnSuccessListener(d -> {
                    room = d.toObject(Room.class);
                    if (room == null) {
                        toast("Không tìm thấy phòng");
                        finish();
                        return;
                    }
                    room.setId(d.getId());
                    bindRoom();
                })
                .addOnFailureListener(e -> {
                    toast("Lỗi tải: " + e.getMessage());
                    finish();
                });

        btnVisitDate.setOnClickListener(v -> pickDate());
        btnSubmit.setOnClickListener(v -> submit(uid));
    }

    private ArrayAdapter<String> adapter(String[] items) {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
    }

    private String[] range(int from, int to) {
        List<String> l = new ArrayList<>();
        for (int i = from; i <= to; i++) l.add(String.valueOf(i));
        return l.toArray(new String[0]);
    }

    private void bindRoom() {
        ((TextInputEditText) findViewById(R.id.edtAddress)).setText(
                room.getAddress() + ", " + room.getDistrict());
        ((TextInputEditText) findViewById(R.id.edtRoomCode)).setText(
                room.getRoomNumber() == null || room.getRoomNumber().isEmpty()
                        ? room.getTitle() : room.getRoomNumber());
        ((TextInputEditText) findViewById(R.id.edtPriceShow)).setText(
                NumberFormat.getInstance(new Locale("vi", "VN")).format(room.getPrice()));

        // Giới hạn số người và xe theo điều kiện chủ trọ đặt ra
        int maxP = room.getMaxPeople() > 0 ? room.getMaxPeople() : 6;
        int maxV = room.getMaxPeople() > 0 || room.getMaxVehicles() > 0 ? room.getMaxVehicles() : 5;
        spPeople.setAdapter(adapter(range(1, maxP)));
        spVehicles.setAdapter(adapter(range(0, maxV)));
        btnSubmit.setEnabled(true);
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(y, m, d, 0, 0, 0);
            sel.set(Calendar.MILLISECOND, 0);
            visitDate = sel.getTimeInMillis();
            btnVisitDate.setText(fmt.format(new Date(visitDate)));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // không chọn ngày quá khứ
        dlg.show();
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void submit(String uid) {
        if (room == null) return;
        String name = text(edtName), phone = text(edtPhone);

        if (uid.equals(room.getLandlordId())) {
            toast("Đây là phòng của chính bạn");
            return;
        }
        if ("rented".equals(room.getRentStatus())) {
            toast("Phòng này đã có người thuê");
            return;
        }
        if (visitDate == 0) {
            toast("Hãy chọn ngày xem phòng");
            return;
        }
        if (name.isEmpty()) {
            toast("Nhập tên khách hàng");
            return;
        }
        if (!phone.matches("0\\d{9,10}")) {
            toast("Số điện thoại không hợp lệ");
            return;
        }

        setLoading(true);
        // Chặn gửi trùng: đã có lịch đang chờ cho phòng này
        db.collection("rentalRequests")
                .whereEqualTo("tenantId", uid)
                .whereEqualTo("roomId", room.getId())
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        setLoading(false);
                        toast("Bạn đã đặt lịch cho phòng này rồi");
                        return;
                    }
                    DocumentReference ref = db.collection("rentalRequests").document();
                    RentalRequest r = new RentalRequest();
                    r.setId(ref.getId());
                    r.setRoomId(room.getId());
                    r.setRoomTitle(room.getTitle());
                    r.setLandlordId(room.getLandlordId());
                    r.setTenantId(uid);
                    r.setTenantName(name);
                    r.setTenantPhone(phone);
                    r.setMessage(text(edtNote));
                    r.setVisitDate(visitDate);
                    r.setExpectedMoveIn(MOVE_IN[spMoveIn.getSelectedItemPosition()]);
                    r.setPeopleCount(Integer.parseInt((String) spPeople.getSelectedItem()));
                    r.setVehicleCount(Integer.parseInt((String) spVehicles.getSelectedItem()));
                    r.setPets((String) spPet.getSelectedItem());
                    r.setCreatedAt(System.currentTimeMillis());

                    ref.set(r)
                            .addOnSuccessListener(v -> {
                                Notifier.send(room.getLandlordId(), "Lịch xem phòng mới",
                                        name + " đặt lịch xem " + room.getTitle() + " ngày " + fmt.format(new Date(visitDate))
                                                + ". Bấm để xác nhận.",
                                        "visit_new", ref.getId());
                                toast("Đã đặt lịch, chờ chủ trọ xác nhận");
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                toast("Lỗi: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Lỗi: " + e.getMessage());
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}