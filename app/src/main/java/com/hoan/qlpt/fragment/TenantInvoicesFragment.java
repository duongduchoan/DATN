package com.hoan.qlpt.fragment;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.tenant.InvoiceDetailActivity;
import com.hoan.qlpt.adapter.InvoiceAdapter;
import com.hoan.qlpt.model.Invoice;
import com.hoan.qlpt.util.EmptyState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TenantInvoicesFragment extends Fragment {

    private final List<Invoice> items = new ArrayList<>();
    private InvoiceAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tenant_invoices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rvPending);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "🧾", "Chưa có hóa đơn nào", "Hóa đơn sẽ hiện khi chủ trọ tạo");
        adapter = new InvoiceAdapter(items, i -> {
            Intent it = new Intent(requireContext(), InvoiceDetailActivity.class);
            it.putExtra("invoiceId", i.getId());
            startActivity(it);
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnStats).setOnClickListener(v ->
                startActivity(new Intent(requireContext(),
                        com.hoan.qlpt.activity.tenant.StatsActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance().collection("invoices")
                .whereEqualTo("tenantId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    items.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Invoice i = d.toObject(Invoice.class);
                        if (i != null) {
                            i.setId(d.getId());
                            items.add(i);
                        }
                    }
                    Collections.sort(items, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}