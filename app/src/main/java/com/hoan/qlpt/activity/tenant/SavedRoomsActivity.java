package com.hoan.qlpt.activity.tenant;
import com.hoan.qlpt.util.EmptyState;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.guest.RoomDetailActivity;
import com.hoan.qlpt.adapter.RoomAdapter;
import com.hoan.qlpt.model.Room;

import java.util.ArrayList;
import java.util.List;

public class SavedRoomsActivity extends AppCompatActivity {

    private final List<Room> rooms = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RoomAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);

        ((TextView) findViewById(R.id.tvScreenTitle)).setText("Phòng đã lưu");
        findViewById(R.id.fabAdd).setVisibility(View.GONE);
        tvEmpty = findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "🤍", "Bạn chưa lưu phòng nào", "Bấm ♡ ở trang chi tiết phòng để lưu");

        adapter = new RoomAdapter(rooms, true);
        adapter.setOnRoomClick(room -> {
            Intent i = new Intent(this, RoomDetailActivity.class);
            i.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.getId());
            startActivity(i);
        });
        RecyclerView rv = findViewById(R.id.rvPending);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }
        db.collection("favorites").whereEqualTo("userId", uid).get()
                .addOnSuccessListener(snap -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String rid = d.getString("roomId");
                        if (rid != null) ids.add(rid);
                    }
                    if (ids.isEmpty()) { show(new ArrayList<>()); return; }

                    // Phòng không còn đọc được (bị gỡ, từ chối) thì bỏ qua
                    List<Room> loaded = new ArrayList<>();
                    int[] left = {ids.size()};
                    for (String id : ids) {
                        db.collection("rooms").document(id).get().addOnCompleteListener(t -> {
                            if (t.isSuccessful() && t.getResult() != null && t.getResult().exists()) {
                                Room r = t.getResult().toObject(Room.class);
                                if (r != null && !r.isHidden()) {
                                    r.setId(t.getResult().getId());
                                    loaded.add(r);
                                }
                            }
                            if (--left[0] == 0) show(loaded);
                        });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void show(List<Room> list) {
        rooms.clear();
        rooms.addAll(list);
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
    }
}