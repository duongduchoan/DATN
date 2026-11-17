package com.hoan.qlpt.util;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class ReasonDialog {

    public interface OnReason {
        void onReason(String reason);
    }

    public static void show(Context ctx, String title, OnReason cb) {
        EditText edt = new EditText(ctx);
        edt.setHint("Lý do từ chối (bắt buộc)");
        edt.setPadding(48, 32, 48, 32);

        AlertDialog dlg = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setView(edt)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Từ chối", null)
                .create();
        dlg.show();

        // Ghi đè nút để hộp thoại không tự đóng khi chưa nhập đủ lý do
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String reason = edt.getText().toString().trim();
            if (reason.length() < 5) {
                Toast.makeText(ctx, "Hãy nhập lý do (ít nhất 5 ký tự)", Toast.LENGTH_SHORT).show();
                return;
            }
            dlg.dismiss();
            cb.onReason(reason);
        });
    }
}