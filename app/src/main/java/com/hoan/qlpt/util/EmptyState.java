package com.hoan.qlpt.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.widget.TextView;

public class EmptyState {

    // emoji lớn + tiêu đề đậm + gợi ý xám (hint có thể rỗng)
    public static void set(TextView tv, String emoji, String title, String hint) {
        boolean hasHint = hint != null && !hint.isEmpty();
        String full = emoji + "\n" + title + (hasHint ? "\n" + hint : "");
        SpannableString s = new SpannableString(full);

        s.setSpan(new RelativeSizeSpan(3f), 0, emoji.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int t0 = emoji.length() + 1;
        s.setSpan(new StyleSpan(Typeface.BOLD), t0, t0 + title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(0xFF424242), t0, t0 + title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (hasHint) {
            int h0 = t0 + title.length() + 1;
            s.setSpan(new ForegroundColorSpan(0xFF9E9E9E), h0, full.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new RelativeSizeSpan(0.9f), h0, full.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tv.setText(s);
        tv.setGravity(Gravity.CENTER);
        tv.setLineSpacing(0f, 1.25f);
        int pad = (int) (24 * tv.getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);
    }
}