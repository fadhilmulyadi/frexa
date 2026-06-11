package com.diellabs.frexa.ui.terminal;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class CandlestickChartView extends View {
    private List<List<Double>> ohlcData = new ArrayList<>();
    private double currentPrice = 0;

    private final Paint upPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint downPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint liveOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pricePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CandlestickChartView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        upPaint.setColor(0xFF36E07A);
        upPaint.setStyle(Paint.Style.FILL);

        downPaint.setColor(0xFFFF5D5D);
        downPaint.setStyle(Paint.Style.FILL);

        wickPaint.setColor(0xFF9A9DA3);
        wickPaint.setStrokeWidth(2f);

        gridPaint.setColor(0x222A2B30);
        gridPaint.setStrokeWidth(1f);

        liveOutlinePaint.setStyle(Paint.Style.STROKE);
        liveOutlinePaint.setStrokeWidth(1.5f);

        pricePaint.setColor(0xCCFFFFFF);
        pricePaint.setStrokeWidth(1f);
        pricePaint.setStyle(Paint.Style.STROKE);
    }

    public void setOhlcData(List<List<Double>> data) {
        ohlcData = data != null ? data : new ArrayList<>();
        invalidate();
    }

    public void setCurrentPrice(double price) {
        currentPrice = price;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (ohlcData.isEmpty()) return;
        int w = getWidth(), h = getHeight();
        int pad = 16, count = Math.min(ohlcData.size(), 60);
        List<List<Double>> visible = ohlcData.subList(
                Math.max(0, ohlcData.size() - count), ohlcData.size());

        double minLow = Double.MAX_VALUE, maxHigh = -Double.MAX_VALUE;
        for (List<Double> c : visible) {
            if (c.size() < 5) continue;
            minLow = Math.min(minLow, c.get(3));
            maxHigh = Math.max(maxHigh, c.get(2));
        }
        double range = maxHigh - minLow;
        if (range == 0) return;

        // Grid lines
        for (int i = 0; i <= 4; i++) {
            float y = pad + (h - 2 * pad) * i / 4f;
            canvas.drawLine(0, y, w, y, gridPaint);
        }

        float candleW = (float) (w - 2 * pad) / count * 0.6f;
        float gap = (float) (w - 2 * pad) / count;

        for (int i = 0; i < visible.size(); i++) {
            List<Double> c = visible.get(i);
            if (c.size() < 5) continue;
            double open = c.get(1), close = c.get(4), high = c.get(2), low = c.get(3);
            float x = pad + i * gap + gap / 2f;
            float yHigh  = toY(high,  minLow, range, h, pad);
            float yLow   = toY(low,   minLow, range, h, pad);
            float yOpen  = toY(open,  minLow, range, h, pad);
            float yClose = toY(close, minLow, range, h, pad);
            boolean bull = close >= open;

            // Wick
            canvas.drawLine(x, yHigh, x, yLow, wickPaint);

            // Body — last candle uses outline (live, still building)
            boolean isLast = (i == visible.size() - 1);
            if (isLast) {
                liveOutlinePaint.setColor(bull ? 0xFF36E07A : 0xFFFF5D5D);
                canvas.drawRect(x - candleW / 2, Math.min(yOpen, yClose),
                        x + candleW / 2, Math.max(yOpen, yClose), liveOutlinePaint);
            } else {
                canvas.drawRect(x - candleW / 2, Math.min(yOpen, yClose),
                        x + candleW / 2, Math.max(yOpen, yClose),
                        bull ? upPaint : downPaint);
            }
        }

        // Current price dashed line
        if (currentPrice > 0 && currentPrice >= minLow && currentPrice <= maxHigh) {
            float yPrice = toY(currentPrice, minLow, range, h, pad);
            pricePaint.setPathEffect(new DashPathEffect(new float[]{8f, 6f}, 0));
            canvas.drawLine(0, yPrice, w, yPrice, pricePaint);
        }
    }

    private float toY(double val, double min, double range, int h, int pad) {
        return pad + (float) ((1 - (val - min) / range) * (h - 2 * pad));
    }
}
