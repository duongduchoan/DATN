package com.hoan.qlpt.activity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hoan.qlpt.R;
import com.hoan.qlpt.model.Profile;
import com.hoan.qlpt.model.User;
import com.hoan.qlpt.util.Role;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtPhone, edtEmail,
            edtPassword, edtConfirm;

    private RadioButton rbLandlord;
    private Button btnRegister;
    private ProgressBar progress;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);

        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> register());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private String text(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void register() {
        String name = text(edtName);
        String phone = text(edtPhone);
        String email = text(edtEmail);
        String pass = text(edtPassword);
        String confirm = text(edtConfirm);

        if (name.isEmpty() || phone.isEmpty()
                || email.isEmpty() || pass.isEmpty()
                || confirm.isEmpty()) {
            toast("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Email không hợp lệ");
            return;
        }

        if (pass.length() < 6) {
            toast("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!pass.equals(confirm)) {
            toast("Mật khẩu nhập lại không khớp");
            return;
        }

        // Lưu vai trò bằng số thay vì chuỗi
        int roleId = rbLandlord.isChecked()
                ? Role.LANDLORD
                : Role.TENANT;

        setLoading(true);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    if (result.getUser() == null) {
                        setLoading(false);
                        toast("Không lấy được thông tin tài khoản");
                        return;
                    }

                    String uid = result.getUser().getUid();

                    // Lưu tài khoản với roleId dạng int
                    User user = new User(uid, email, roleId);
                    Profile profile = new Profile(uid, name, phone);

                    WriteBatch batch = db.batch();

                    batch.set(
                            db.collection("users").document(uid),
                            user
                    );

                    batch.set(
                            db.collection("profiles").document(uid),
                            profile
                    );

                    batch.commit()
                            .addOnSuccessListener(v -> {
                                setLoading(false);
                                toast("Đăng ký thành công");
                                auth.signOut();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                toast("Lỗi lưu hồ sơ: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Lỗi đăng ký: " + e.getMessage());
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
        btnRegister.setEnabled(!loading);
    }

    private void toast(String msg) {
        Toast.makeText(
                this,
                msg,
                Toast.LENGTH_LONG
        ).show();
    }
}
