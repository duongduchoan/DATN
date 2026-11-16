package com.hoan.qlpt.activity;
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
import com.hoan.qlpt.adapter.ApprovalAdapter;
import com.hoan.qlpt.model.ApprovalItem;
import com.hoan.qlpt.model.Incident;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncidentsActivity extends AppCompatActivity {

    private final List<ApprovalItem> items = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private boolean landlordMode;
    private ApprovalAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);

        landlordMode = "landlord".equals(getIntent().getStringExtra("mode"));
        ((TextView) findViewById(R.id.tvScreenTitle)).setText(
                landlordMode ? "Sự cố từ người thuê" : "Sự cố của tôi");
        tvEmpty = findViewById(R.id.tvEmpty);
        if (landlordMode) {
            EmptyState.set(tvEmpty, "🛠️", "Chưa có sự cố nào", "Sự cố người thuê báo sẽ hiện ở đây");
        } else {
            EmptyState.set(tvEmpty, "🛠️", "Chưa có sự cố nào", "Bấm + để báo sự cố khi cần");
        }

        View fab = findViewById(R.id.fabAdd);
        fab.setVisibility(landlordMode ? View.GONE : View.VISIBLE);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CreateIncidentActivity.class)));

        adapter = new ApprovalAdapter(items, new ApprovalAdapter.Listener() {
            @Override public void onApprove(ApprovalItem it) {}
            @Override public void onReject(ApprovalItem it) {}
        });
        adapter.setShowActions(false);
        adapter.setOnItemClick(it -> {
            Intent i = new Intent(this, IncidentDetailActivity.class);
            i.putExtra("incidentId", it.id);
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
        FirebaseFirestore.getInstance().collection("incidents")
                .whereEqualTo(landlordMode ? "landlordId" : "tenantId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Incident> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Incident i = d.toObject(Incident.class);
                        if (i != null) { i.setId(d.getId()); list.add(i); }
                    }
                    Collections.sort(list, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                    items.clear();
                    for (Incident i : list) {
                        items.add(new ApprovalItem(i.getId(), i.getTitle(),
                                i.getRoomTitle() + " • " + i.getCategory()
                                        + (landlordMode ? " • " + i.getTenantName() : ""),
                                Incident.statusText(i.getStatus()) + " • "
                                        + fmt.format(new Date(i.getCreatedAt())), i));
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi tải: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}