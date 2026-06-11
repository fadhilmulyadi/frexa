package com.diellabs.frexa.ui.terminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LiveCandleBuilder {

    // Candle format: [timestamp, open, high, low, close]
    private List<List<Double>> historicalCandles = new ArrayList<>();
    private final List<List<Double>> liveCandles = new ArrayList<>();
    private List<Double> currentCandle = null;
    private int periodSeconds;
    private long periodStart = 0;

    public LiveCandleBuilder(int periodSeconds) {
        this.periodSeconds = periodSeconds;
    }

    public synchronized void setHistoricalCandles(List<List<Double>> ohlc) {
        historicalCandles = new ArrayList<>(ohlc);
        liveCandles.clear();
        currentCandle = null;
        periodStart = 0;
    }

    public synchronized void addTick(double price, long timestampMs) {
        long periodMs = (long) periodSeconds * 1000L;
        long ps = (timestampMs / periodMs) * periodMs;

        if (currentCandle == null) {
            periodStart = ps;
            currentCandle = Arrays.asList((double) periodStart, price, price, price, price);
        } else if (timestampMs >= periodStart + periodMs) {
            liveCandles.add(new ArrayList<>(currentCandle));
            periodStart = ps;
            currentCandle = Arrays.asList((double) periodStart, price, price, price, price);
        } else {
            double high  = Math.max(currentCandle.get(2), price);
            double low   = Math.min(currentCandle.get(3), price);
            currentCandle = Arrays.asList(currentCandle.get(0),
                    currentCandle.get(1), // open unchanged
                    high, low, price);
        }
    }

    public synchronized List<List<Double>> getCandles() {
        List<List<Double>> all = new ArrayList<>();
        all.addAll(historicalCandles);
        all.addAll(liveCandles);

        int fromIndex = Math.max(0, all.size() - 59);
        List<List<Double>> result = new ArrayList<>(all.subList(fromIndex, all.size()));

        if (currentCandle != null) {
            result.add(new ArrayList<>(currentCandle));
        }
        return result;
    }

    public synchronized void reset(int newPeriodSeconds) {
        periodSeconds = newPeriodSeconds;
        liveCandles.clear();
        currentCandle = null;
        periodStart = 0;
    }
}
