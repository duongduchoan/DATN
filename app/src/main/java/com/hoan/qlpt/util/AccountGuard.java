package com.hoan.qlpt.util;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.hoan.qlpt.activity.LoginActivity;

public class AccountGuard {

    private ListenerRegistration reg;

    // expectedRole: vai trò mà màn hình hiện tại dành cho (Role.ADMIN / LANDLORD / TENANT)
    public void start(Activity act, int expectedRole) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        stop();
        reg = FirebaseFirestore.getInstance().collection("users").document(uid)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null || !snap.exists()) return;
                    Boolean locked = snap.getBoolean("locked");
                    Long rid = snap.getLong("roleId");
                    if (Boolean.TRUE.equals(locked)) {
                        kick(act, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ admin.");
                    } else if (rid != null && rid.intValue() != expectedRole) {
                        kick(act, "Vai trò của bạn đã thay đổi, vui lòng đăng nhập lại.");
                    }
                });
    }

    public void stop() {
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    private void kick(Activity act, String msg) {
        stop();
        if (act.isFinishing()) return;
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(act, msg, Toast.LENGTH_LONG).show();
        Intent i = new Intent(act, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        act.startActivity(i);
    }
}