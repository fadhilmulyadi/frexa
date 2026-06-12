package com.diellabs.frexa.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.res.ResourcesCompat;
import com.diellabs.frexa.R;
import java.util.HashMap;
import java.util.Map;

public class MonogramView extends View {
    private static final Map<String, String> MONOGRAMS = new HashMap<>();

    static {
        MONOGRAMS.put("BTC", "\u20BF");
        MONOGRAMS.put("ETH", "\u039E");
        MONOGRAMS.put("SOL", "\u25CE");
        MONOGRAMS.put("DOGE", "\u00D0");
        MONOGRAMS.put("XRP", "\u2715");
    }

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String symbol = "??";

    public MonogramView(Context context) { super(context); init(); }
    public MonogramView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public MonogramView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        bgPaint.setColor(0xFF1B2025);
        bgPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(0xFFE8B25A);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        try {
            Typeface tf = ResourcesCompat.getFont(getContext(), R.font.spline_sans_mono);
            if (tf != null) textPaint.setTypeface(tf);
        } catch (Exception e) {
            textPaint.setTypeface(Typeface.MONOSPACE);
        }
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol != null ? symbol.toUpperCase() : "??";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float radius = dp(10);

        RectF bg = new RectF(0, 0, w, h);
        canvas.drawRoundRect(bg, radius, radius, bgPaint);

        String display = MONOGRAMS.get(symbol);
        if (display == null) {
            display = symbol.length() >= 2 ? symbol.substring(0, 2) : symbol;
        }

        float textSize = MONOGRAMS.containsKey(symbol) ? dp(16) : dp(13);
        textPaint.setTextSize(textSize);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = (h - fm.ascent - fm.descent) / 2f;
        canvas.drawText(display, w / 2f, textY, textPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
