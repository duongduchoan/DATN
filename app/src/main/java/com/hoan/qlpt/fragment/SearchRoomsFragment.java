package com.hoan.qlpt.fragment;
import com.hoan.qlpt.util.EmptyState;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.guest.RoomDetailActivity;
import com.hoan.qlpt.adapter.RoomAdapter;
import com.hoan.qlpt.model.Room;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.hoan.qlpt.util.Amenities;

public class SearchRoomsFragment extends Fragment {

    private final List<Room> allRooms = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final Set<String> amenityFilter = new LinkedHashSet<>();
    private int sortMode = 0; // 0 mới nhất, 1 giá tăng, 2 giá giảm
    private boolean loaded = false;

    private RoomAdapter adapter;
    private TextView tvEmpty, tvCount;
    private TextInputEditText edtSearch;
    private ChipGroup chipGroupPrice;
    private Button btnFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvCount = view.findViewById(R.id.tvCount);
        edtSearch = view.findViewById(R.id.edtSearch);
        chipGroupPrice = view.findViewById(R.id.chipGroupPrice);
        btnFilter = view.findViewById(R.id.btnFilter);

        RecyclerView rv = view.findViewById(R.id.rvRooms);
        adapter = new RoomAdapter(rooms, true);
        adapter.setOnRoomClick(room -> {
            Intent i = new Intent(requireContext(), RoomDetailActivity.class);
            i.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.getId());
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(); }
        });
        chipGroupPrice.setOnCheckedStateChangeListener((g, ids) -> applyFilter());
        btnFilter.setOnClickListener(v -> showFilterSheet());
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        FirebaseFirestore.getInstance().collection("rooms")
                .whereEqualTo("status", "approved")
                .whereEqualTo("rentStatus", "available")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    allRooms.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Room r = d.toObject(Room.class);
                        if (r == null || r.isHidden()) continue;
                        r.setId(d.getId());
                        allRooms.add(r);
                    }
                    loaded = true;
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showFilterSheet() {
        BottomSheetDialog dlg = new BottomSheetDialog(requireContext());
        View v = getLayoutInflater().inflate(R.layout.sheet_filter, null);

        ChipGroup cg = v.findViewById(R.id.cgFilterAmenities);
        for (String name : Amenities.ALL) {
            Chip c = (Chip) getLayoutInflater().inflate(R.layout.chip_filter, cg, false);
            c.setText(name);
            c.setChecked(amenityFilter.contains(name));
            cg.addView(c);
        }
        RadioGroup rg = v.findViewById(R.id.rgSort);
        rg.check(sortMode == 1 ? R.id.rbPriceAsc : sortMode == 2 ? R.id.rbPriceDesc : R.id.rbNew);

        v.findViewById(R.id.btnReset).setOnClickListener(x -> {
            amenityFilter.clear();
            sortMode = 0;
            dlg.dismiss();
            updateFilterButton();
            applyFilter();
        });
        v.findViewById(R.id.btnApply).setOnClickListener(x -> {
            amenityFilter.clear();
            for (int i = 0; i < cg.getChildCount(); i++) {
                Chip c = (Chip) cg.getChildAt(i);
                if (c.isChecked()) amenityFilter.add(c.getText().toString());
            }
            int id = rg.getCheckedRadioButtonId();
            sortMode = id == R.id.rbPriceAsc ? 1 : id == R.id.rbPriceDesc ? 2 : 0;
            dlg.dismiss();
            updateFilterButton();
            applyFilter();
        });

        dlg.setContentView(v);
        dlg.show();
    }
    // Nút hiện số bộ lọc đang bật: "Bộ lọc (2)"
    private void updateFilterButton() {
        int n = amenityFilter.size() + (sortMode != 0 ? 1 : 0);
        btnFilter.setText(n > 0 ? "Bộ lọc (" + n + ")" : "Bộ lọc");
    }

    // Bỏ dấu tiếng Việt để tìm "binh thanh" ra "Bình Thạnh"
    private String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return n.toLowerCase(Locale.ROOT).trim();
    }

    private void applyFilter() {
        if (adapter == null) return;
        String keyword = norm(edtSearch.getText() == null ? "" : edtSearch.getText().toString());
        int priceId = chipGroupPrice.getCheckedChipId();

        long min = 0, max = Long.MAX_VALUE;
        if (priceId == R.id.chipP1) { max = 2_000_000L; }
        else if (priceId == R.id.chipP2) { min = 2_000_000L; max = 4_000_000L; }
        else if (priceId == R.id.chipP3) { min = 4_000_000L; }

        rooms.clear();
        for (Room r : allRooms) {
            if (r.getPrice() < min || r.getPrice() > max) continue;
            boolean okAmenity = true;
            for (String a : amenityFilter) {
                if (!Amenities.has(r.getAmenities(), a)) { okAmenity = false; break; }
            }
            if (!okAmenity) continue;
            if (!keyword.isEmpty()) {
                String hay = norm(r.getTitle() + " " + r.getDistrict() + " " + r.getAddress());
                if (!hay.contains(keyword)) continue;
            }
            rooms.add(r);
        }

        if (sortMode == 1) rooms.sort((a, b) -> Long.compare(a.getPrice(), b.getPrice()));
        else if (sortMode == 2) rooms.sort((a, b) -> Long.compare(b.getPrice(), a.getPrice()));
        else rooms.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

        adapter.notifyDataSetChanged();
        if (loaded) {
            tvCount.setText(rooms.size() + " phòng");
            if (allRooms.isEmpty()) {
                EmptyState.set(tvEmpty, "🏠", "Chưa có phòng nào", "Hãy quay lại sau nhé");
            } else {
                EmptyState.set(tvEmpty, "🔍", "Không có phòng phù hợp", "Thử đổi từ khóa hoặc bỏ bớt bộ lọc");
            }
            tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}