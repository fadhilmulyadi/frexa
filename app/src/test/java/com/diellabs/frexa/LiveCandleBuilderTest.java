package com.diellabs.frexa;

import com.diellabs.frexa.ui.terminal.LiveCandleBuilder;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class LiveCandleBuilderTest {

    private LiveCandleBuilder builder;

    @Before
    public void setUp() {
        builder = new LiveCandleBuilder(60); // 1-menit period
    }

    @Test
    public void addTick_firstTick_opensNewCandle() {
        long now = 1_000_000L;
        builder.addTick(100.0, now);

        List<List<Double>> candles = builder.getCandles();
        assertEquals(1, candles.size());
        List<Double> c = candles.get(0);
        assertEquals(100.0, c.get(1), 0.001); // open
        assertEquals(100.0, c.get(2), 0.001); // high
        assertEquals(100.0, c.get(3), 0.001); // low
        assertEquals(100.0, c.get(4), 0.001); // close
    }

    @Test
    public void addTick_samePeriod_updatesHighLowClose() {
        long base = 60_000L;
        builder.addTick(100.0, base);
        builder.addTick(110.0, base + 5_000L);
        builder.addTick(90.0,  base + 9_000L);

        List<List<Double>> candles = builder.getCandles();
        assertEquals(1, candles.size());
        List<Double> c = candles.get(0);
        assertEquals(100.0, c.get(1), 0.001); // open unchanged
        assertEquals(110.0, c.get(2), 0.001); // high
        assertEquals(90.0,  c.get(3), 0.001); // low
        assertEquals(90.0,  c.get(4), 0.001); // close = last tick
    }

    @Test
    public void addTick_periodElapsed_closesAndOpensNewCandle() {
        long base = 60_000L;
        builder.addTick(100.0, base);
        builder.addTick(105.0, base + 60_000L); // next period

        List<List<Double>> candles = builder.getCandles();
        assertEquals(2, candles.size());
        assertEquals(100.0, candles.get(0).get(4), 0.001);
        assertEquals(105.0, candles.get(1).get(1), 0.001);
        assertEquals(105.0, candles.get(1).get(4), 0.001);
    }

    @Test
    public void getCandles_combinesHistoricalAndLive() {
        List<List<Double>> hist = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            hist.add(Arrays.asList((double)(i * 60_000L), 100.0, 110.0, 90.0, 105.0));
        }
        builder.setHistoricalCandles(hist);
        builder.addTick(200.0, 400_000L);

        List<List<Double>> candles = builder.getCandles();
        assertEquals(6, candles.size());
        assertEquals(200.0, candles.get(5).get(4), 0.001);
    }

    @Test
    public void getCandles_capsAt60() {
        List<List<Double>> hist = new ArrayList<>();
        for (int i = 0; i < 70; i++) {
            hist.add(Arrays.asList((double)(i * 60_000L), 100.0, 110.0, 90.0, 105.0));
        }
        builder.setHistoricalCandles(hist);
        builder.addTick(200.0, 4_300_000L);

        List<List<Double>> candles = builder.getCandles();
        assertEquals(60, candles.size());
    }

    @Test
    public void reset_clearsLiveState() {
        // Prime with historical data so reset() must also clear it
        List<List<Double>> hist = new ArrayList<>();
        hist.add(Arrays.asList(0.0, 100.0, 110.0, 90.0, 105.0));
        builder.setHistoricalCandles(hist);

        builder.addTick(100.0, 60_000L);
        builder.reset(300);

        List<List<Double>> candles = builder.getCandles();
        assertTrue(candles.isEmpty());
    }

    @Test
    public void setHistoricalCandles_clearsLiveState() {
        builder.addTick(100.0, 60_000L);
        builder.setHistoricalCandles(new ArrayList<>());

        List<List<Double>> candles = builder.getCandles();
        assertTrue(candles.isEmpty());
    }
}
