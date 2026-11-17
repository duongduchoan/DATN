package com.hoan.qlpt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.adapter.UserAdapter;
import com.hoan.qlpt.model.User;
import com.hoan.qlpt.util.EmptyState;
import com.hoan.qlpt.util.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUsersFragment extends Fragment {

    private final List<User> users = new ArrayList<>();
    private final Map<String, String> names = new HashMap<>();
    private UserAdapter adapter;
    private TextView tvEmpty;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_approve_rooms,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvPending);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        EmptyState.set(tvEmpty, "👥", "Chưa có người dùng nào", "");

        adapter = new UserAdapter(users, names, this::setLocked);
        adapter.setOnRowClick(this::chooseRole);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        load();
    }

    private void load() {
        // Tải họ tên trước, rồi tải danh sách tài khoản
        db.collection("profiles").get().addOnSuccessListener(ps -> {
            names.clear();

            for (DocumentSnapshot d : ps.getDocuments()) {
                names.put(d.getId(), d.getString("fullName"));
            }

            db.collection("users").get().addOnSuccessListener(us -> {
                if (!isAdded()) return;

                users.clear();
                String me = FirebaseAuth.getInstance().getUid();

                for (DocumentSnapshot d : us.getDocuments()) {
                    User u = d.toObject(User.class);
                    if (u == null) continue;

                    u.setUid(d.getId());

                    if (d.getId().equals(me)) continue; // ẩn chính mình

                    users.add(u);
                }

                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(
                        users.isEmpty() ? View.VISIBLE : View.GONE
                );
            });
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(
                        requireContext(),
                        "Lỗi tải: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setLocked(User u, boolean locked) {
        db.collection("users").document(u.getUid())
                .update("locked", locked)
                .addOnSuccessListener(v -> {
                    u.setLocked(locked);

                    if (isAdded()) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(
                                requireContext(),
                                locked ? "Đã khóa tài khoản"
                                        : "Đã mở khóa tài khoản",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                "Lỗi: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Chọn vai trò mới khi nhấn vào một người dùng
    private void chooseRole(User u) {
        if (u.getRoleId() == Role.ADMIN) {
            Toast.makeText(
                    requireContext(),
                    "Không đổi vai trò của admin tại đây",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String[] items = {"Người thuê", "Chủ trọ"};
        int checked = u.getRoleId() == Role.LANDLORD ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi vai trò")
                .setSingleChoiceItems(items, checked, (d, which) -> {
                    d.dismiss();

                    int newRole = which == 1
                            ? Role.LANDLORD
                            : Role.TENANT;

                    if (newRole == u.getRoleId()) return;

                    // Không hạ quyền chủ trọ nếu còn hợp đồng hiệu lực
                    if (u.getRoleId() == Role.LANDLORD
                            && newRole == Role.TENANT) {

                        db.collection("contracts")
                                .whereEqualTo("landlordId", u.getUid())
                                .whereEqualTo("status", "active")
                                .get()
                                .addOnSuccessListener(s -> {
                                    if (!isAdded()) return;

                                    if (!s.isEmpty()) {
                                        Toast.makeText(
                                                requireContext(),
                                                "Chủ trọ còn hợp đồng đang hiệu lực, không thể hạ quyền",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    } else {
                                        updateRole(u, newRole);
                                    }
                                });
                    } else {
                        updateRole(u, newRole);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Cập nhật vai trò trong Firebase
    private void updateRole(User u, int newRole) {
        db.collection("users").document(u.getUid())
                .update("roleId", newRole)
                .addOnSuccessListener(v -> {
                    u.setRoleId(newRole);

                    if (isAdded()) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(
                                requireContext(),
                                "Đã đổi vai trò (người dùng cần đăng nhập lại)",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                "Lỗi: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
