package com.hoan.qlpt.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.Locale;

public class BarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private long[] values = new long[12];
    private int highlight = -1;

    public BarChartView(Context c, AttributeSet a) {
        super(c, a);
        barPaint.setColor(0xFF009688);
        highlightPaint.setColor(0xFFFF9800);

        labelPaint.setColor(0xFF666666);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(sp(11));

        valuePaint.setColor(0xFF333333);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(sp(9));

        linePaint.setColor(0xFFBDBDBD);
        linePaint.setStrokeWidth(dp(1));
    }

    // values: đúng 12 phần tử (tháng 1..12); highlightIndex: -1 nếu không tô nổi bật
    public void setData(long[] v, int highlightIndex) {
        values = v;
        highlight = highlightIndex;
        invalidate();
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v,
                getResources().getDisplayMetrics());
    }

    // 1250000 -> "1,3tr", 245000 -> "245k"
    private String shortText(long v) {
        if (v >= 1_000_000) return String.format(Locale.US, "%.1ftr", v / 1_000_000f).replace('.', ',');
        if (v >= 1_000) return (v / 1_000) + "k";
        return String.valueOf(v);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = dp(8), right = getWidth() - dp(8);
        float top = dp(24), bottom = getHeight() - dp(24);

        long max = 1;
        for (long v : values) max = Math.max(max, v);

        float slot = (right - left) / 12f;
        float barW = slot * 0.62f;

        canvas.drawLine(left, bottom, right, bottom, linePaint);

        for (int i = 0; i < 12; i++) {
            float cx = left + slot * i + slot / 2f;
            long v = values[i];

            if (v > 0) {
                float barH = (bottom - top) * v / (float) max;
                rect.set(cx - barW / 2f, bottom - barH, cx + barW / 2f, bottom);
                canvas.drawRoundRect(rect, dp(4), dp(4), i == highlight ? highlightPaint : barPaint);
                canvas.drawText(shortText(v), cx, bottom - barH - dp(4), valuePaint);
            }
            canvas.drawText("T" + (i + 1), cx, bottom + dp(16), labelPaint);
        }
    }
}