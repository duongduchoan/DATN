package com.hoan.qlpt.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.activity.admin.AdminMainActivity;
import com.hoan.qlpt.activity.landlord.LandlordMainActivity;
import com.hoan.qlpt.activity.tenant.TenantMainActivity;
import com.hoan.qlpt.model.User;
import com.hoan.qlpt.util.Role;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    private ProgressBar progress;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);

        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> login());

        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.tvForgot).setOnClickListener(v -> {
            String email = text(edtEmail);
            if (email.isEmpty()) {
                toast("Nhập email vào ô Email trước");
                return;
            }
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(x -> toast("Đã gửi email đặt lại mật khẩu (kiểm tra cả thư rác)"))
                    .addOnFailureListener(e -> toast("Không gửi được: " + e.getMessage()));
        });

        // Nếu đã đăng nhập thì kiểm tra vai trò
        if (auth.getCurrentUser() != null) {
            setLoading(true);
            routeByRole(auth.getCurrentUser().getUid());
        }
    }

    private String text(TextInputEditText e) {
        return e.getText() == null
                ? ""
                : e.getText().toString().trim();
    }

    private void login() {
        String email = text(edtEmail);
        String pass = text(edtPassword);

        if (email.isEmpty() || pass.isEmpty()) {
            toast("Vui lòng nhập email và mật khẩu");
            return;
        }

        setLoading(true);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    if (result.getUser() != null) {
                        routeByRole(result.getUser().getUid());
                    } else {
                        fail("Không lấy được thông tin tài khoản");
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Sai email hoặc mật khẩu");
                });
    }

    private void routeByRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        fail("Không tìm thấy dữ liệu tài khoản");
                        return;
                    }

                    User user = doc.toObject(User.class);

                    if (user == null) {
                        fail("Không đọc được dữ liệu tài khoản");
                        return;
                    }

                    if (user.isLocked()) {
                        fail("Tài khoản đã bị khóa");
                        return;
                    }

                    int roleId = user.getRoleId();
                    Intent intent;

                    switch (roleId) {
                        case Role.ADMIN:
                            intent = new Intent(
                                    LoginActivity.this,
                                    AdminMainActivity.class
                            );
                            break;

                        case Role.LANDLORD:
                            intent = new Intent(
                                    LoginActivity.this,
                                    LandlordMainActivity.class
                            );
                            break;

                        case Role.TENANT:
                            intent = new Intent(
                                    LoginActivity.this,
                                    TenantMainActivity.class
                            );
                            break;

                        default:
                            fail("Tài khoản chưa được gán vai trò hợp lệ");
                            return;
                    }

                    // Xóa màn hình đăng nhập khỏi ngăn xếp
                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        fail("Lỗi tải dữ liệu: " + e.getMessage()));
    }

    private void fail(String msg) {
        auth.signOut();
        setLoading(false);
        toast(msg);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
        btnLogin.setEnabled(!loading);
    }

    private void toast(String msg) {
        Toast.makeText(
                this,
                msg,
                Toast.LENGTH_LONG
        ).show();
    }
}