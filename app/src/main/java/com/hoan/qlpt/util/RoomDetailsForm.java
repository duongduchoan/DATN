package com.hoan.qlpt.util;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomDetailsForm {

    private static final String[] FLOORS = {"Trệt", "Lầu 1", "Lầu 2", "Lầu 3", "Lầu 4 trở lên"};
    private static final String[] TOILET = {"Riêng", "Chung"};
    private static final String[] CURFEW = {"Tự do", "Có giờ giấc"};
    private static final String[] WASHER = {"Không", "Chung", "Riêng"};
    private static final String[] YES_NO = {"Có", "Không"};
    private static final String[] NO_YES = {"Không", "Có"};
    private static final String[] PETS = {"Không", "Có", "Liên hệ"};
    private static final String[] PARKING = {"Chung", "Riêng", "Không"};

    private final ChipGroup cg;
    private final Spinner spFloor, spToilet, spCurfew, spWasher, spWindow, spBalcony, spPets, spParking, spEv;
    private final TextInputEditText edtElec, edtWater, edtService, edtParkingFee, edtMaxPeople, edtMaxVehicles;

    public RoomDetailsForm(View root) {
        cg = root.findViewById(R.id.cgAmenities);
        for (String name : Amenities.ALL) {
            Chip c = (Chip) LayoutInflater.from(root.getContext()).inflate(R.layout.chip_filter, cg, false);
            c.setText(name);
            cg.addView(c);
        }

        spFloor = setup(root, R.id.rdFloor, FLOORS);
        spToilet = setup(root, R.id.rdToilet, TOILET);
        spCurfew = setup(root, R.id.rdCurfew, CURFEW);
        spWasher = setup(root, R.id.rdWasher, WASHER);
        spWindow = setup(root, R.id.rdWindow, YES_NO);
        spBalcony = setup(root, R.id.rdBalcony, NO_YES);
        spPets = setup(root, R.id.rdPets, PETS);
        spParking = setup(root, R.id.rdParking, PARKING);
        spEv = setup(root, R.id.rdEv, NO_YES);

        edtElec = root.findViewById(R.id.rdElec);
        edtWater = root.findViewById(R.id.rdWater);
        edtService = root.findViewById(R.id.rdService);
        edtParkingFee = root.findViewById(R.id.rdParkingFee);
        edtMaxPeople = root.findViewById(R.id.rdMaxPeople);
        edtMaxVehicles = root.findViewById(R.id.rdMaxVehicles);

        // Giá trị mặc định thường gặp, chủ trọ sửa lại theo thực tế
        edtElec.setText("3500");
        edtWater.setText("15000");
        edtService.setText("0");
        edtParkingFee.setText("0");
        edtMaxPeople.setText("2");
        edtMaxVehicles.setText("2");
        MoneyTextWatcher.attach(edtElec);
        MoneyTextWatcher.attach(edtWater);
        MoneyTextWatcher.attach(edtService);
        MoneyTextWatcher.attach(edtParkingFee);
    }

    private Spinner setup(View root, int id, String[] opts) {
        Spinner sp = root.findViewById(id);
        sp.setAdapter(new ArrayAdapter<>(root.getContext(),
                android.R.layout.simple_spinner_dropdown_item, opts));
        return sp;
    }

    private void select(Spinner sp, String[] opts, String value) {
        for (int i = 0; i < opts.length; i++) {
            if (opts[i].equals(value)) {
                sp.setSelection(i);
                return;
            }
        }
    }

    private String sel(Spinner sp) {
        Object o = sp.getSelectedItem();
        return o == null ? "" : o.toString();
    }

    private long num(TextInputEditText e) {
        return MoneyTextWatcher.parse(e.getText());
    }

    // Trả về thông báo lỗi, hoặc null nếu hợp lệ
    public String validate() {
        if (num(edtElec) <= 0) return "Nhập đơn giá điện";
        if (num(edtWater) <= 0) return "Nhập đơn giá nước";
        if (num(edtMaxPeople) < 1) return "Số người tối đa phải từ 1 trở lên";
        return null;
    }

    // Điền dữ liệu có sẵn của phòng (dùng cho màn Sửa phòng)
    public void fill(Room r) {
        boolean hasDetails = r.getToilet() != null && !r.getToilet().isEmpty();
        for (int i = 0; i < cg.getChildCount(); i++) {
            Chip c = (Chip) cg.getChildAt(i);
            c.setChecked(Amenities.has(r.getAmenities(), c.getText().toString()));
        }
        if (!hasDetails) return; // phòng cũ: giữ giá trị mặc định
        edtElec.setText(String.valueOf(r.getElecPrice()));
        edtWater.setText(String.valueOf(r.getWaterPrice()));
        edtService.setText(String.valueOf(r.getServiceFee()));
        edtParkingFee.setText(String.valueOf(r.getParkingFee()));
        edtMaxPeople.setText(String.valueOf(r.getMaxPeople()));
        edtMaxVehicles.setText(String.valueOf(r.getMaxVehicles()));
        select(spFloor, FLOORS, r.getFloor());
        select(spToilet, TOILET, r.getToilet());
        select(spCurfew, CURFEW, r.getCurfew());
        select(spWasher, WASHER, r.getWasher());
        select(spWindow, YES_NO, r.getWindow());
        select(spBalcony, NO_YES, r.getBalcony());
        select(spPets, PETS, r.getPets());
        select(spParking, PARKING, r.getParking());
        select(spEv, NO_YES, r.getEvCharge());
    }

    private List<String> checked() {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < cg.getChildCount(); i++) {
            Chip c = (Chip) cg.getChildAt(i);
            if (c.isChecked()) l.add(c.getText().toString());
        }
        return l;
    }

    // Ghi vào một phòng mới
    public void apply(Room r) {
        r.setAmenities(checked());
        r.setElecPrice(num(edtElec));
        r.setWaterPrice(num(edtWater));
        r.setServiceFee(num(edtService));
        r.setParkingFee(num(edtParkingFee));
        r.setMaxPeople((int) num(edtMaxPeople));
        r.setMaxVehicles((int) num(edtMaxVehicles));
        r.setFloor(sel(spFloor));
        r.setToilet(sel(spToilet));
        r.setCurfew(sel(spCurfew));
        r.setWasher(sel(spWasher));
        r.setWindow(sel(spWindow));
        r.setBalcony(sel(spBalcony));
        r.setPets(sel(spPets));
        r.setParking(sel(spParking));
        r.setEvCharge(sel(spEv));
    }

    // Dùng cho update() ở màn Sửa phòng
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("amenities", checked());
        m.put("elecPrice", num(edtElec));
        m.put("waterPrice", num(edtWater));
        m.put("serviceFee", num(edtService));
        m.put("parkingFee", num(edtParkingFee));
        m.put("maxPeople", (int) num(edtMaxPeople));
        m.put("maxVehicles", (int) num(edtMaxVehicles));
        m.put("floor", sel(spFloor));
        m.put("toilet", sel(spToilet));
        m.put("curfew", sel(spCurfew));
        m.put("washer", sel(spWasher));
        m.put("window", sel(spWindow));
        m.put("balcony", sel(spBalcony));
        m.put("pets", sel(spPets));
        m.put("parking", sel(spParking));
        m.put("evCharge", sel(spEv));
        return m;
    }
}