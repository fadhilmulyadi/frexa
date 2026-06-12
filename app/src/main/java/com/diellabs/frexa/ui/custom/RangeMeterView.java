package com.diellabs.frexa.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class RangeMeterView extends View {
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private double low = 0;
    private double high = 1;
    private double current = 0.5;
    private boolean isUp = true;

    public RangeMeterView(Context context) { super(context); init(); }
    public RangeMeterView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public RangeMeterView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        bgPaint.setColor(0xFF1B2025);
        bgPaint.setStyle(Paint.Style.FILL);

        dotPaint.setColor(0xFFEDF1F4);
        dotPaint.setStyle(Paint.Style.FILL);

        dotBorderPaint.setColor(0xFF3DD68C);
        dotBorderPaint.setStyle(Paint.Style.STROKE);
        dotBorderPaint.setStrokeWidth(dp(2.5f));
    }

    public void setData(double low, double high, double current) {
        this.low = low;
        this.high = high;
        this.current = current;
        this.isUp = current >= low + (high - low) * 0.5;
        dotBorderPaint.setColor(isUp ? 0xFF3DD68C : 0xFFFF6259);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float barH = dp(5);
        float barY = (h - barH) / 2f;
        float radius = barH / 2f;

        RectF bgRect = new RectF(0, barY, w, barY + barH);
        canvas.drawRoundRect(bgRect, radius, radius, bgPaint);

        float position = (high > low) ? (float) ((current - low) / (high - low)) : 0.5f;
        position = Math.max(0, Math.min(1, position));
        float fillW = w * position;

        if (fillW > 0) {
            int startColor = isUp ? 0x403DD68C : 0x40FF6259;
            int endColor = isUp ? 0xFF3DD68C : 0xFFFF6259;
            fillPaint.setShader(new LinearGradient(0, 0, fillW, 0, startColor, endColor, Shader.TileMode.CLAMP));
            RectF fillRect = new RectF(0, barY, fillW, barY + barH);
            canvas.drawRoundRect(fillRect, radius, radius, fillPaint);
            fillPaint.setShader(null);
        }

        float dotX = w * position;
        float dotY = h / 2f;
        float dotR = dp(5);
        canvas.drawCircle(dotX, dotY, dotR + dp(2), dotBorderPaint);
        canvas.drawCircle(dotX, dotY, dotR, dotPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
