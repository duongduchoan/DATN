package com.hoan.qlpt.util;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.hoan.qlpt.R;

import java.util.HashMap;
import java.util.Map;

public class EmptyState {

    // Đổi emoji cũ sang Vector Drawable tương ứng (giữ nguyên chữ ký hàm để không phải sửa nơi gọi)
    private static final Map<String, Integer> ICONS = new HashMap<>();
    static {
        ICONS.put("🛠", R.drawable.ic_build);
        ICONS.put("🏠", R.drawable.ic_home);
        ICONS.put("🏘", R.drawable.ic_apartment);
        ICONS.put("👥", R.drawable.ic_group);
        ICONS.put("📨", R.drawable.ic_send);
        ICONS.put("📭", R.drawable.ic_inbox);
        ICONS.put("🤍", R.drawable.ic_favorite_border);
        ICONS.put("✅", R.drawable.ic_check_circle);
        ICONS.put("🧾", R.drawable.ic_receipt);
        ICONS.put("🔔", R.drawable.ic_notifications);
        ICONS.put("🔍", R.drawable.ic_search);
        ICONS.put("🔑", R.drawable.ic_key);
    }

    private static final int ICON_DP = 56;

    // icon lớn + tiêu đề đậm + gợi ý xám (hint có thể rỗng)
    public static void set(TextView tv, String emoji, String title, String hint) {
        Context ctx = tv.getContext();
        float density = ctx.getResources().getDisplayMetrics().density;

        // Bỏ ký tự biến thể (U+FE0F) để tra bảng icon
        Integer iconRes = ICONS.get(emoji.replace("️", ""));
        Drawable icon = iconRes == null ? null : ContextCompat.getDrawable(ctx, iconRes);

        // Không có icon tương ứng thì vẫn hiện emoji như cũ
        String head = icon == null ? emoji + "\n" : "";
        boolean hasHint = hint != null && !hint.isEmpty();
        String full = head + title + (hasHint ? "\n" + hint : "");
        SpannableString s = new SpannableString(full);

        if (!head.isEmpty()) {
            s.setSpan(new RelativeSizeSpan(3f), 0, emoji.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        int t0 = head.length();
        s.setSpan(new StyleSpan(Typeface.BOLD), t0, t0 + title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.qlpt_text)),
                t0, t0 + title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (hasHint) {
            int h0 = t0 + title.length() + 1;
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.qlpt_text_hint)),
                    h0, full.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new RelativeSizeSpan(0.9f), h0, full.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (icon != null) {
            icon = icon.mutate();
            icon.setTint(ContextCompat.getColor(ctx, R.color.qlpt_primary));
            int size = (int) (ICON_DP * density);
            icon.setBounds(0, 0, size, size);
        }
        // Luôn gán lại (kể cả null) vì cùng một TextView có thể được gọi nhiều lần với icon khác nhau
        tv.setCompoundDrawablesRelative(null, icon, null, null);
        tv.setCompoundDrawablePadding((int) (12 * density));

        tv.setText(s);
        tv.setGravity(Gravity.CENTER);
        tv.setLineSpacing(0f, 1.25f);
        int pad = (int) (24 * density);
        tv.setPadding(pad, pad, pad, pad);
    }
}
