package com.diellabs.frexa.ui.tradedetail;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class MiniChartView extends View {
    private List<float[]> points = new ArrayList<>();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MiniChartView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        linePaint.setColor(0xFF3FE0C0); linePaint.setStrokeWidth(3f); linePaint.setStyle(Paint.Style.STROKE);
        fillPaint.setColor(0x303FE0C0);
    }

    public void setData(List<List<Double>> prices, double entry, double exit, boolean win) {
        if (prices == null || prices.isEmpty()) return;
        double minP = prices.stream().mapToDouble(p -> p.get(1)).min().orElse(0);
        double maxP = prices.stream().mapToDouble(p -> p.get(1)).max().orElse(1);
        double range = maxP - minP;
        if (range == 0) range = 1;
        points.clear();
        for (int i = 0; i < prices.size(); i++) {
            float xPct = (float) i / (prices.size() - 1);
            float yPct = (float)((prices.get(i).get(1) - minP) / range);
            points.add(new float[]{xPct, yPct});
        }
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        if (points.isEmpty()) return;
        int w = getWidth(), h = getHeight(), pad = 20;
        Path linePath = new Path(), fillPath = new Path();
        for (int i = 0; i < points.size(); i++) {
            float x = pad + points.get(i)[0] * (w - 2*pad);
            float y = (h-pad) - points.get(i)[1] * (h - 2*pad);
            if (i == 0) { linePath.moveTo(x, y); fillPath.moveTo(x, y); }
            else { linePath.lineTo(x, y); fillPath.lineTo(x, y); }
        }
        float lastX = pad + (w - 2*pad);
        fillPath.lineTo(lastX, h - pad);
        fillPath.lineTo(pad, h - pad);
        fillPath.close();
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }
}
