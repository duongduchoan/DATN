package com.hoan.qlpt.util;

import android.view.View;
import android.widget.TextView;

import com.hoan.qlpt.R;

public class StatCard {

    public static void label(View parent, int includeId, String label) {
        View c = parent.findViewById(includeId);
        ((TextView) c.findViewById(R.id.tvLabel)).setText(label);
    }

    public static void value(View parent, int includeId, String value) {
        View c = parent.findViewById(includeId);
        ((TextView) c.findViewById(R.id.tvValue)).setText(value);
    }
}