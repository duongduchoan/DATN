package com.hoan.qlpt.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hoan.qlpt.R;
import com.hoan.qlpt.util.Role;

import java.util.HashMap;
import java.util.Map;

/** Đăng ký tài khoản mới. Luôn tạo người thuê (roleId = 3), chủ trọ phải được admin duyệt sau. */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextInputEditText edtName, edtPhone, edtEmail, edtPassword, edtConfirm;
    private Button btnRegister;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);

        btnRegister.setOnClickListener(v -> register());
        ((TextView) findViewById(R.id.tvGoLogin)).setOnClickListener(v -> finish());
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void register() {
        String name = text(edtName);
        String phone = text(edtPhone);
        String email = text(edtEmail);
        String pass = text(edtPassword);
        String confirm = text(edtConfirm);

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            toast("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        if (!phone.matches("0\\d{9,10}")) { toast("Số điện thoại không hợp lệ"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Email không hợp lệ"); return; }
        if (pass.length() < 6) { toast("Mật khẩu phải có ít nhất 6 ký tự"); return; }
        if (!pass.equals(confirm)) { toast("Mật khẩu nhập lại không khớp"); return; }

        setLoading(true);
        // Bước 1: tạo tài khoản đăng nhập (Firebase Authentication)
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> saveData(result.getUser(), name, phone, email))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Tạo tài khoản thất bại", e);
                    setLoading(false);
                    toast(authError(e));
                });
    }

    // Bước 2: ghi users + profiles trong một batch, khóa phải khớp đúng Security Rules
    private void saveData(FirebaseUser fu, String name, String phone, String email) {
        if (fu == null) {
            setLoading(false);
            toast("Không lấy được tài khoản vừa tạo, hãy thử lại");
            return;
        }
        String uid = fu.getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("email", email);
        user.put("roleId", Role.TENANT);
        user.put("locked", false);

        Map<String, Object> profile = new HashMap<>();
        profile.put("uid", uid);
        profile.put("fullName", name);
        profile.put("phone", phone);
        profile.put("avatarUrl", "");

        WriteBatch batch = db.batch();
        batch.set(db.collection("users").document(uid), user);
        batch.set(db.collection("profiles").document(uid), profile);
        batch.commit()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Đã lưu users và profiles cho " + uid);
                    toast("Đăng ký thành công");
                    goToApp();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ghi Firestore thất bại", e);
                    rollback(fu, e);
                });
    }

    // Ghi Firestore lỗi thì xóa luôn tài khoản vừa tạo, tránh để tài khoản mồ côi
    private void rollback(FirebaseUser fu, Exception cause) {
        fu.delete().addOnCompleteListener(t -> {
            auth.signOut();
            setLoading(false);
            toast("Không lưu được dữ liệu: " + cause.getMessage() + "\nHãy thử đăng ký lại.");
        });
    }

    // Vào thẳng ứng dụng: LoginActivity tự điều hướng theo vai trò khi đã đăng nhập
    private void goToApp() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private String authError(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) return "Email này đã được đăng ký";
        if (e instanceof FirebaseAuthWeakPasswordException) return "Mật khẩu quá yếu, hãy đặt dài hơn";
        if (e instanceof FirebaseAuthInvalidCredentialsException) return "Email không hợp lệ";
        if (e instanceof FirebaseNetworkException) return "Không có kết nối mạng";
        return "Đăng ký thất bại: " + e.getMessage();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}