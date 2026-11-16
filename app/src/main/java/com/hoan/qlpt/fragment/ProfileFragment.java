package com.hoan.qlpt.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.ChangePasswordActivity;
import com.hoan.qlpt.activity.EditProfileActivity;
import com.hoan.qlpt.activity.IncidentsActivity;
import com.hoan.qlpt.activity.LoginActivity;
import com.hoan.qlpt.activity.NotificationsActivity;
import com.hoan.qlpt.activity.landlord.BankInfoActivity;
import com.hoan.qlpt.activity.landlord.TenantsActivity;
import com.hoan.qlpt.activity.tenant.RequestLandlordActivity;
import com.hoan.qlpt.util.ImageUrl;
import com.hoan.qlpt.util.Role;

public class ProfileFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvName, tvEmail, tvRole, tvWorkCaption;
    private ImageView imgAvatar;
    private LinearLayout llAccount, llWork;
    private View cardWork;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRole = view.findViewById(R.id.tvRole);
        tvWorkCaption = view.findViewById(R.id.tvWorkCaption);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        llAccount = view.findViewById(R.id.llAccount);
        llWork = view.findViewById(R.id.llWork);
        cardWork = view.findViewById(R.id.cardWork);

        // Bấm avatar hoặc huy hiệu camera đều mở màn sửa hồ sơ (nơi đổi ảnh)
        View.OnClickListener edit = v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class));
        imgAvatar.setOnClickListener(edit);
        view.findViewById(R.id.btnCameraBadge).setOnClickListener(edit);

        view.findViewById(R.id.btnLogout).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn muốn đăng xuất khỏi tài khoản này?")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Đăng xuất", (d, w) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent i = new Intent(requireContext(), LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        })
                        .show());
    }

    // Quay lại từ màn hình sửa / gửi yêu cầu thì cập nhật lại
    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
        loadRole();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    // Có ảnh thì hiện ảnh tròn; không có thì hiện icon người làm placeholder
    private void showAvatar(@Nullable String url) {
        if (url != null && !url.isEmpty()) {
            imgAvatar.setPadding(0, 0, 0, 0);
            imgAvatar.setImageTintList(null);
            Glide.with(this).load(ImageUrl.thumb(url, 200)).circleCrop().into(imgAvatar);
        } else {
            Glide.with(this).clear(imgAvatar);
            int p = dp(14);
            imgAvatar.setPadding(p, p, p, p);
            imgAvatar.setImageResource(R.drawable.ic_person);
            imgAvatar.setImageTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        }
    }

    private void loadProfile() {
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu == null) return;
        tvEmail.setText(fu.getEmail());
        db.collection("profiles").document(fu.getUid()).get().addOnSuccessListener(d -> {
            if (!isAdded() || !d.exists()) return;
            tvName.setText(d.getString("fullName"));
            showAvatar(d.getString("avatarUrl"));
        });
    }

    private void loadRole() {
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu == null) return;
        db.collection("users").document(fu.getUid()).get().addOnSuccessListener(d -> {
            if (!isAdded() || !d.exists()) return;
            Long rid = d.getLong("roleId");
            int roleId = rid == null ? 0 : rid.intValue();
            tvRole.setText(Role.name(roleId));
            buildMenu(roleId, fu.getUid());
        });
    }

    private void buildMenu(int roleId, String uid) {
        llAccount.removeAllViews();
        llWork.removeAllViews();

        // Nhóm chung cho mọi vai trò
        addRow(llAccount, R.drawable.ic_edit, "Chỉnh sửa hồ sơ", null,
                v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        addRow(llAccount, R.drawable.ic_lock, "Đổi mật khẩu", null,
                v -> startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));
        trimLast(llAccount);

        if (roleId == Role.TENANT) {
            showWork("NÂNG CẤP TÀI KHOẢN");
            loadRequestState(uid);
        } else if (roleId == Role.LANDLORD) {
            showWork("QUẢN LÝ");
            View noti = addRow(llWork, R.drawable.ic_notifications, "Thông báo", null,
                    v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));
            addRow(llWork, R.drawable.ic_warning, "Sự cố từ người thuê", null,
                    v -> startActivity(new Intent(requireContext(), IncidentsActivity.class)
                            .putExtra("mode", "landlord")));
            addRow(llWork, R.drawable.ic_group, "Người thuê đang ở", null,
                    v -> startActivity(new Intent(requireContext(), TenantsActivity.class)));
            addRow(llWork, R.drawable.ic_account_balance, "Thông tin nhận tiền",
                    "Dùng để tạo mã QR trên hóa đơn",
                    v -> startActivity(new Intent(requireContext(), BankInfoActivity.class)));
            trimLast(llWork);
            loadUnread(uid, noti);
        } else if (roleId == Role.ADMIN) {
            showWork("QUẢN LÝ");
            View noti = addRow(llWork, R.drawable.ic_notifications, "Thông báo", null,
                    v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));
            trimLast(llWork);
            loadUnread(uid, noti);
        } else {
            cardWork.setVisibility(View.GONE);
            tvWorkCaption.setVisibility(View.GONE);
        }
    }

    private void showWork(String caption) {
        tvWorkCaption.setText(caption);
        tvWorkCaption.setVisibility(View.VISIBLE);
        cardWork.setVisibility(View.VISIBLE);
    }

    // Người thuê: dòng "Đăng ký làm chủ trọ" kèm trạng thái yêu cầu gần nhất
    private void loadRequestState(String uid) {
        db.collection("roleRequests").whereEqualTo("uid", uid).get().addOnSuccessListener(snap -> {
            if (!isAdded()) return;
            DocumentSnapshot latest = null;
            for (DocumentSnapshot d : snap.getDocuments()) {
                Long t = d.getLong("createdAt");
                if (t == null) continue;
                Long lt = latest == null ? null : latest.getLong("createdAt");
                if (latest == null || (lt != null && t > lt)) latest = d;
            }

            llWork.removeAllViews();
            String st = latest == null ? null : latest.getString("status");

            if ("pending".equals(st)) {
                View row = addRow(llWork, R.drawable.ic_schedule,
                        "Yêu cầu làm chủ trọ đang chờ duyệt",
                        "Admin sẽ xem xét và phản hồi cho bạn", null);
                row.findViewById(R.id.rowRoot).setClickable(false);
                row.findViewById(R.id.ivChevron).setVisibility(View.GONE);
            } else if ("rejected".equals(st)) {
                String reason = latest.getString("rejectReason");
                View row = addRow(llWork, R.drawable.ic_warning, "Gửi lại yêu cầu làm chủ trọ",
                        "Yêu cầu trước bị từ chối"
                                + (reason == null || reason.isEmpty() ? "" : ": " + reason),
                        v -> startActivity(new Intent(requireContext(), RequestLandlordActivity.class)));
                ((TextView) row.findViewById(R.id.tvSub)).setTextColor(Color.parseColor("#E53935"));
            } else {
                addRow(llWork, R.drawable.ic_home, "Đăng ký làm chủ trọ",
                        "Cho thuê phòng và quản lý hóa đơn trên QLPT",
                        v -> startActivity(new Intent(requireContext(), RequestLandlordActivity.class)));
            }
            trimLast(llWork);
        });
    }

    // Số thông báo chưa đọc hiện thành huy hiệu đỏ trên dòng Thông báo
    private void loadUnread(String uid, View row) {
        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("read", false)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(r -> {
                    if (!isAdded()) return;
                    TextView badge = row.findViewById(R.id.tvBadge);
                    if (r.getCount() > 0) {
                        badge.setText(String.valueOf(r.getCount()));
                        badge.setVisibility(View.VISIBLE);
                    } else {
                        badge.setVisibility(View.GONE);
                    }
                });
    }

    // Thêm một dòng menu: icon vector, tiêu đề, dòng phụ (có thể null), sự kiện bấm (có thể null)
    private View addRow(LinearLayout parent, @DrawableRes int drawableResId, String title,
                        @Nullable String sub, @Nullable View.OnClickListener click) {
        View row = getLayoutInflater().inflate(R.layout.item_menu_row, parent, false);
        ((ImageView) row.findViewById(R.id.ivIcon)).setImageResource(drawableResId);
        ((TextView) row.findViewById(R.id.tvTitle)).setText(title);

        TextView tvSub = row.findViewById(R.id.tvSub);
        if (sub == null || sub.isEmpty()) {
            tvSub.setVisibility(View.GONE);
        } else {
            tvSub.setText(sub);
            tvSub.setVisibility(View.VISIBLE);
        }
        if (click != null) row.findViewById(R.id.rowRoot).setOnClickListener(click);
        parent.addView(row);
        return row;
    }

    // Dòng cuối của mỗi thẻ không cần đường kẻ phân cách
    private void trimLast(LinearLayout parent) {
        int n = parent.getChildCount();
        if (n > 0) parent.getChildAt(n - 1).findViewById(R.id.divider).setVisibility(View.GONE);
    }
}