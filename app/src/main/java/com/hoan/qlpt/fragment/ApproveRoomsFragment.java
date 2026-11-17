package com.hoan.qlpt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.PendingRoomAdapter;
import com.hoan.qlpt.model.Room;
import com.hoan.qlpt.util.EmptyState;
import com.hoan.qlpt.util.Notifier;
import com.hoan.qlpt.util.ReasonDialog;

import java.util.ArrayList;
import java.util.List;

public class ApproveRoomsFragment extends Fragment {

    private final List<Room> rooms = new ArrayList<>();
    private PendingRoomAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_approve_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rvPending);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "✅", "Không có phòng nào chờ duyệt", "Mọi tin đăng đã được xử lý");

        adapter = new PendingRoomAdapter(rooms, new PendingRoomAdapter.Listener() {
            @Override
            public void onApprove(Room room) {
                updateStatus(room, "approved", "");
            }

            @Override
            public void onReject(Room room) {
                ReasonDialog.show(requireContext(), "Từ chối phòng: " + room.getTitle(),
                        reason -> updateStatus(room, "rejected", reason));
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        load();
    }

    private void load() {
        FirebaseFirestore.getInstance().collection("rooms")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    rooms.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Room r = d.toObject(Room.class);
                        if (r != null) {
                            r.setId(d.getId());
                            rooms.add(r);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateStatus(Room room, String status, String reason) {
        boolean approved = "approved".equals(status);
        FirebaseFirestore.getInstance().collection("rooms").document(room.getId())
                .update("status", status, "rejectReason", reason)
                .addOnSuccessListener(v -> {
                    Notifier.send(room.getLandlordId(),
                            approved ? "Phòng đã được duyệt" : "Phòng bị từ chối",
                            approved ? room.getTitle() : room.getTitle() + "\nLý do: " + reason);
                    if (!isAdded()) return;
                    int index = rooms.indexOf(room);
                    if (index >= 0) {
                        rooms.remove(index);
                        adapter.notifyItemRemoved(index);
                    }
                    tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(requireContext(),
                            approved ? "Đã duyệt tin" : "Đã từ chối tin", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(requireContext(),
                            "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}