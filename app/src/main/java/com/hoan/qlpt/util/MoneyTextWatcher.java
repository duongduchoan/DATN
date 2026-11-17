package com.hoan.qlpt.util;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyTextWatcher implements TextWatcher {

    private static final NumberFormat NF = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final int MAX_DIGITS = 12; // tối đa 12 chữ số

    private final EditText edt;
    private boolean editing = false;

    private MoneyTextWatcher(EditText e) {
        edt = e;
    }

    // Gắn vào một ô nhập tiền
    public static void attach(EditText e) {
        e.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        e.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        MoneyTextWatcher w = new MoneyTextWatcher(e);
        e.addTextChangedListener(w);
        w.afterTextChanged(e.getText()); // định dạng luôn giá trị có sẵn
    }

    // Đọc số từ chuỗi đã định dạng: "5.000.000" -> 5000000 (rỗng thì 0)
    public static long parse(CharSequence s) {
        if (s == null) return 0;
        String d = s.toString().replaceAll("[^\\d]", "");
        if (d.isEmpty()) return 0;
        if (d.length() > MAX_DIGITS) d = d.substring(0, MAX_DIGITS);
        return Long.parseLong(d);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int a, int b, int c) {
    }

    @Override
    public void onTextChanged(CharSequence s, int a, int b, int c) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (editing) return;
        editing = true;
        String digits = s.toString().replaceAll("[^\\d]", "");
        if (digits.length() > MAX_DIGITS) digits = digits.substring(0, MAX_DIGITS);
        String formatted = digits.isEmpty() ? "" : NF.format(Long.parseLong(digits));
        if (!formatted.equals(s.toString())) {
            s.replace(0, s.length(), formatted);
            try {
                edt.setSelection(edt.getText().length());
            } catch (Exception ignored) {
            }
        }
        editing = false;
    }
}