package com.hoan.qlpt.fragment;
import com.hoan.qlpt.util.EmptyState;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.landlord.AddRoomActivity;
import com.hoan.qlpt.adapter.RoomAdapter;
import com.hoan.qlpt.model.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.hoan.qlpt.model.Contract;
import com.hoan.qlpt.util.Notifier;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyRoomsFragment extends Fragment {

    private final List<Room> rooms = new ArrayList<>();
    private RoomAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_my_rooms,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvRooms);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "🏘️", "Bạn chưa có phòng nào", "Bấm nút + để thêm phòng đầu tiên");

        adapter = new RoomAdapter(rooms);
        adapter.setOnRoomClick(this::showActions);

        rv.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        rv.setAdapter(adapter);

        // Sự kiện nút cộng: mở màn hình thêm phòng
        view.findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(
                    requireContext(),
                    AddRoomActivity.class
            );
            startActivity(intent);
        });

        loadRooms();
    }

    private void loadRooms() {
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("rooms")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) {
                        return;
                    }

                    rooms.clear();

                    for (var d : snap.getDocuments()) {
                        Room room = d.toObject(Room.class);

                        if (room != null) {
                            room.setId(d.getId());
                            rooms.add(room);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    tvEmpty.setVisibility(
                            rooms.isEmpty()
                                    ? View.VISIBLE
                                    : View.GONE
                    );
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Tải lại danh sách khi quay về từ màn hình thêm phòng
        if (adapter != null) {
            loadRooms();
        }
    }
    private void showActions(Room r) {
        List<String> labels = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        labels.add("Sửa thông tin");
        actions.add(() -> {
            Intent i = new Intent(requireContext(),
                    com.hoan.qlpt.activity.landlord.EditRoomActivity.class);
            i.putExtra("roomId", r.getId());
            startActivity(i);
        });

        labels.add(r.isHidden() ? "Hiện phòng trên trang khách" : "Ẩn phòng khỏi trang khách");
        actions.add(() -> setHidden(r, !r.isHidden()));

        if ("rented".equals(r.getRentStatus())) {
            labels.add("Hợp đồng / trả phòng");
            actions.add(() -> showContract(r));
        }
        if ("rejected".equals(r.getStatus())) {
            labels.add("Gửi lại để duyệt");
            actions.add(() -> resubmitRoom(r));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(r.getTitle())
                .setItems(labels.toArray(new String[0]), (d, which) -> actions.get(which).run())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void resubmitRoom(Room r) {
        FirebaseFirestore.getInstance().collection("rooms").document(r.getId())
                .update("status", "pending", "rejectReason", "")
                .addOnSuccessListener(v -> {
                    r.setStatus("pending");
                    r.setRejectReason("");
                    if (isAdded()) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Đã gửi lại, chờ admin duyệt",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setHidden(Room r, boolean hidden) {
        FirebaseFirestore.getInstance().collection("rooms").document(r.getId())
                .update("hidden", hidden)
                .addOnSuccessListener(v -> {
                    r.setHidden(hidden);
                    if (!isAdded()) return;
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(),
                            hidden ? "Đã ẩn phòng" : "Phòng đã hiện trở lại",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void showContract(Room r) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance().collection("contracts")
                .whereEqualTo("landlordId", uid)
                .whereEqualTo("roomId", r.getId())
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    if (snap.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Phòng này không có hợp đồng đang hiệu lực", Toast.LENGTH_LONG).show();
                        return;
                    }
                    DocumentSnapshot cd = snap.getDocuments().get(0);
                    Contract c = cd.toObject(Contract.class);
                    if (c == null) return;
                    c.setId(cd.getId());

                    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String msg = "Người thuê: " + c.getTenantName()
                            + "\nThời hạn: " + fmt.format(new Date(c.getStartDate()))
                            + " - " + fmt.format(new Date(c.getEndDate()))
                            + "\nGiá thuê: " + nf.format(c.getPrice()) + "đ/tháng"
                            + "\nTiền cọc: " + nf.format(c.getDeposit()) + "đ (nhớ hoàn trả cho người thuê)"
                            + (c.isEndRequested() ? "\n\nNgười thuê đã gửi yêu cầu trả phòng." : "");

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Hợp đồng")
                            .setMessage(msg)
                            .setNegativeButton("Đóng", null)
                            .setPositiveButton("Kết thúc hợp đồng", (d, w) -> endContract(r, c))
                            .show();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void endContract(Room r, Contract c) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Chặn kết thúc khi còn hóa đơn chưa thanh toán xong
        db.collection("invoices")
                .whereEqualTo("landlordId", uid)
                .whereEqualTo("contractId", c.getId())
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    int open = 0;
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        if (!"paid".equals(d.getString("status"))) open++;
                    }
                    if (open > 0) {
                        Toast.makeText(requireContext(), "Còn " + open
                                        + " hóa đơn chưa thanh toán xong. Hãy xử lý trước khi kết thúc hợp đồng",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    WriteBatch b = db.batch();
                    b.update(db.collection("contracts").document(c.getId()),
                            "status", "ended", "endedAt", System.currentTimeMillis());
                    b.update(db.collection("rooms").document(r.getId()), "rentStatus", "available");
                    b.commit()
                            .addOnSuccessListener(v -> {
                                Notifier.send(c.getTenantId(), "Hợp đồng đã kết thúc",
                                        "Chủ trọ đã kết thúc hợp đồng thuê " + c.getRoomTitle());
                                r.setRentStatus("available");
                                if (isAdded()) {
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(requireContext(),
                                            "Đã kết thúc hợp đồng, phòng đã trống trở lại",
                                            Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) Toast.makeText(requireContext(),
                                        "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

