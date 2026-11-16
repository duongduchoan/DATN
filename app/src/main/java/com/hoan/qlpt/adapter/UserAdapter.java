package com.hoan.qlpt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hoan.qlpt.R;
import com.hoan.qlpt.model.User;
import com.hoan.qlpt.util.Role;

import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface Listener {
        void onLockChanged(User user, boolean locked);
    }

    private final List<User> users;
    private final Map<String, String> names; // uid -> họ tên
    private final Listener listener;


    public interface OnRowClick { void onClick(User u); }
    private OnRowClick onRowClick;
    public void setOnRowClick(OnRowClick l) { onRowClick = l; }

    public UserAdapter(
            List<User> users,
            Map<String, String> names,
            Listener listener) {
        this.users = users;
        this.names = names;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = users.get(position);

        String name = names.get(u.getUid());

        h.tvName.setText(
                name == null || name.isEmpty()
                        ? "(chưa có tên)"
                        : name
        );

        h.tvEmail.setText(u.getEmail());

        // Hiển thị tên vai trò dựa trên roleId
        int roleId = u.getRoleId();

        h.tvRole.setText("Vai trò: " + Role.name(roleId));

        // Cập nhật trạng thái khóa tài khoản
        h.swLock.setOnCheckedChangeListener(null);
        h.swLock.setChecked(u.isLocked());

        h.tvLockLabel.setText(
                u.isLocked() ? "Đã khóa" : "Hoạt động"
        );

        // Không cho khóa tài khoản Admin
        boolean isAdmin = roleId == Role.ADMIN;

        h.swLock.setEnabled(!isAdmin);

        if (isAdmin) {
            h.tvLockLabel.setText("Admin");
        } else {
            h.swLock.setOnCheckedChangeListener(
                    (buttonView, checked) -> {
                        if (listener != null) {
                            listener.onLockChanged(u, checked);
                        }
                    }
            );

        }
        h.itemView.setOnClickListener(v -> {
            if (onRowClick != null) {
                onRowClick.onClick(u);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail, tvRole, tvLockLabel;
        Switch swLock;

        VH(View view) {
            super(view);

            tvName = view.findViewById(R.id.tvName);
            tvEmail = view.findViewById(R.id.tvEmail);
            tvRole = view.findViewById(R.id.tvRole);
            tvLockLabel = view.findViewById(R.id.tvLockLabel);
            swLock = view.findViewById(R.id.swLock);
        }
    }
}
