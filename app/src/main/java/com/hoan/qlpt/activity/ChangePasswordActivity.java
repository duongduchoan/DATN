package com.hoan.qlpt.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.hoan.qlpt.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText edtCurrent, edtNew, edtConfirm;
    private Button btnSave;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        edtCurrent = findViewById(R.id.edtCurrent);
        edtNew = findViewById(R.id.edtNew);
        edtConfirm = findViewById(R.id.edtConfirm);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);
        btnSave.setOnClickListener(v -> change());
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString();
    }

    private void change() {
        String cur = text(edtCurrent), nw = text(edtNew), cf = text(edtConfirm);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        if (cur.isEmpty()) {
            toast("Nhập mật khẩu hiện tại");
            return;
        }
        if (nw.length() < 6) {
            toast("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }
        if (!nw.equals(cf)) {
            toast("Mật khẩu nhập lại không khớp");
            return;
        }
        if (nw.equals(cur)) {
            toast("Mật khẩu mới phải khác mật khẩu hiện tại");
            return;
        }

        setLoading(true);
        // Xác thực lại bằng mật khẩu hiện tại rồi mới được đổi
        AuthCredential cred = EmailAuthProvider.getCredential(user.getEmail(), cur);
        user.reauthenticate(cred)
                .addOnSuccessListener(r -> user.updatePassword(nw)
                        .addOnSuccessListener(v -> {
                            toast("Đã đổi mật khẩu");
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            setLoading(false);
                            toast("Lỗi: " + e.getMessage());
                        }))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast(e instanceof FirebaseAuthInvalidCredentialsException
                            ? "Mật khẩu hiện tại không đúng" : "Lỗi: " + e.getMessage());
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}