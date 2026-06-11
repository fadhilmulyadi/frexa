# Real-Time Candlestick Chart Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Chart di TerminalFragment update real-time seperti OlympTrade — candle terakhir "bernafas" mengikuti harga live, dengan timeframe switchable (1m/5m/15m/30m/1h) dari UI.

**Architecture:** Setiap tick harga dari polling CoinGecko (tiap 10 detik) dimasukkan ke `LiveCandleBuilder` yang membangun candle secara client-side sesuai timeframe aktif. `CryptoViewModel` mengekspos `chartCandles` LiveData yang menggabungkan historical OHLC (dari API) dengan live candles. Chart re-render tiap 10 detik otomatis.

**Tech Stack:** Android Java, LiveData/ViewModel, CoinGecko REST API (Retrofit), custom Canvas View, JUnit4

---

## File Map

| File | Status | Tanggung jawab |
|------|--------|----------------|
| `app/src/main/java/com/diellabs/frexa/ui/terminal/LiveCandleBuilder.java` | **Baru** | Pure logic: bangun candle dari price ticks |
| `app/src/test/java/com/diellabs/frexa/LiveCandleBuilderTest.java` | **Baru** | Unit tests untuk LiveCandleBuilder |
| `app/src/main/java/com/diellabs/frexa/data/repository/CryptoRepository.java` | Modify | Tambah callback overload fetchOhlc + fetchLivePrice |
| `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java` | Modify | Tambah chartCandles, setTimeframe(), feed ticks ke builder |
| `app/src/main/java/com/diellabs/frexa/ui/terminal/CandlestickChartView.java` | Modify | Live candle outline + current price dashed line |
| `app/src/main/res/layout/fragment_terminal.xml` | Modify | Tambah row 5 tombol timeframe di atas chart |
| `app/src/main/java/com/diellabs/frexa/ui/terminal/TerminalFragment.java` | Modify | Observe chartCandles, wire timeframe buttons |

---

## Task 1: LiveCandleBuilder — Tests + Implementation

**Files:**
- Create: `app/src/test/java/com/diellabs/frexa/LiveCandleBuilderTest.java`
- Create: `app/src/main/java/com/diellabs/frexa/ui/terminal/LiveCandleBuilder.java`

- [ ] **Step 1.1: Tulis failing tests**

Buat file `app/src/test/java/com/diellabs/frexa/LiveCandleBuilderTest.java`:

```java
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
        long now = 1_000_000L; // arbitrary timestamp
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
        long base = 60_000L; // second 60 → periodStart = 60000
        builder.addTick(100.0, base);
        builder.addTick(110.0, base + 5_000L);  // +5s, same period
        builder.addTick(90.0,  base + 9_000L);  // +9s, same period

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
        // closed candle
        assertEquals(100.0, candles.get(0).get(4), 0.001);
        // new live candle
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
        // last candle is the live one
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
        builder.addTick(100.0, 60_000L);
        builder.reset(300); // ganti ke 5-menit

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
```

- [ ] **Step 1.2: Jalankan test — pastikan FAIL**

```
./gradlew :app:test --tests "com.diellabs.frexa.LiveCandleBuilderTest"
```

Expected: BUILD FAIL karena `LiveCandleBuilder` belum ada.

- [ ] **Step 1.3: Implementasi LiveCandleBuilder**

Buat `app/src/main/java/com/diellabs/frexa/ui/terminal/LiveCandleBuilder.java`:

```java
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
            // Tutup candle saat ini, buka yang baru
            liveCandles.add(new ArrayList<>(currentCandle));
            periodStart = ps;
            currentCandle = Arrays.asList((double) periodStart, price, price, price, price);
        } else {
            // Masih dalam period yang sama — update close, high, low
            double high  = Math.max(currentCandle.get(2), price);
            double low   = Math.min(currentCandle.get(3), price);
            currentCandle = Arrays.asList(currentCandle.get(0),
                    currentCandle.get(1), // open tidak berubah
                    high, low, price);
        }
    }

    public synchronized List<List<Double>> getCandles() {
        List<List<Double>> all = new ArrayList<>();
        all.addAll(historicalCandles);
        all.addAll(liveCandles);

        // Ambil 59 terakhir dari gabungan, lalu append currentCandle
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
```

- [ ] **Step 1.4: Jalankan test — pastikan PASS**

```
./gradlew :app:test --tests "com.diellabs.frexa.LiveCandleBuilderTest"
```

Expected: BUILD SUCCESSFUL, semua 7 test hijau.

- [ ] **Step 1.5: Commit**

```
git add app/src/main/java/com/diellabs/frexa/ui/terminal/LiveCandleBuilder.java
git add app/src/test/java/com/diellabs/frexa/LiveCandleBuilderTest.java
git commit -m "feat(chart): add LiveCandleBuilder for client-side tick accumulation"
```

---

## Task 2: CryptoRepository — Callback Overloads

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/data/repository/CryptoRepository.java`

- [ ] **Step 2.1: Tambah callback interfaces dan overload methods**

Buka `CryptoRepository.java`. Tambahkan dua interface di dalam class dan dua method overload baru. **Jangan hapus method lama** — masih dipakai layar lain.

Tambahkan tepat setelah deklarasi field-field class (sebelum konstruktor):

```java
public interface OhlcCallback {
    void onResult(List<List<Double>> data);
}

public interface PriceCallback {
    void onPrice(double price);
}
```

Tambahkan dua method ini setelah method `fetchOhlc` yang sudah ada:

```java
public void fetchOhlc(String coinId, OhlcCallback callback) {
    api.getOhlc(coinId, "usd", 1).enqueue(new Callback<List<List<Double>>>() {
        @Override public void onResponse(Call<List<List<Double>>> c,
                                         Response<List<List<Double>>> r) {
            if (r.isSuccessful() && r.body() != null) callback.onResult(r.body());
        }
        @Override public void onFailure(Call<List<List<Double>>> c, Throwable t) {}
    });
}

public void fetchLivePrice(String coinId, PriceCallback callback,
                            MutableLiveData<String> error) {
    api.getPrices(coinId, "usd", false)
       .enqueue(new Callback<Map<String, Map<String, Double>>>() {
           @Override public void onResponse(Call<Map<String, Map<String, Double>>> c,
                                            Response<Map<String, Map<String, Double>>> r) {
               if (r.isSuccessful() && r.body() != null) {
                   Map<String, Double> inner = r.body().get(coinId);
                   if (inner != null && inner.get("usd") != null) {
                       callback.onPrice(inner.get("usd"));
                   }
               }
           }
           @Override public void onFailure(Call<Map<String, Map<String, Double>>> c,
                                           Throwable t) {
               error.postValue("Tidak ada koneksi");
           }
       });
}
```

- [ ] **Step 2.2: Pastikan build bersih**

```
./gradlew :app:compileDebugJavaWithJavac
```

Expected: BUILD SUCCESSFUL, tidak ada error.

- [ ] **Step 2.3: Commit**

```
git add app/src/main/java/com/diellabs/frexa/data/repository/CryptoRepository.java
git commit -m "feat(repository): add callback overloads for fetchOhlc and fetchLivePrice"
```

---

## Task 3: CryptoViewModel — chartCandles + setTimeframe

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java`

- [ ] **Step 3.1: Update CryptoViewModel**

Ganti seluruh isi `CryptoViewModel.java` dengan versi berikut:

```java
package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.data.repository.CryptoRepository;
import com.diellabs.frexa.ui.terminal.LiveCandleBuilder;
import com.diellabs.frexa.util.AppExecutors;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CryptoViewModel extends AndroidViewModel {
    public final MutableLiveData<List<CoinMarket>> coinList = new MutableLiveData<>();
    public final MutableLiveData<Double> livePrice = new MutableLiveData<>();
    public final MutableLiveData<List<List<Double>>> ohlcData = new MutableLiveData<>();
    public final MutableLiveData<List<List<Double>>> chartCandles = new MutableLiveData<>();
    public final MutableLiveData<MarketChart> marketChart = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Integer> selectedTimeframe = new MutableLiveData<>(60);

    private final CryptoRepository repo;
    private final LiveCandleBuilder candleBuilder = new LiveCandleBuilder(60);
    private ScheduledExecutorService scheduler;
    private String activeCoinId = "bitcoin";

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);
    }

    public void fetchMarkets() { repo.fetchMarkets(coinList, isLoading, errorMessage); }

    public void setActiveCoin(String id) {
        activeCoinId = id;
        int tf = selectedTimeframe.getValue() != null ? selectedTimeframe.getValue() : 60;
        candleBuilder.reset(tf);
        repo.fetchOhlc(id, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
        });
    }

    public void setTimeframe(int seconds) {
        selectedTimeframe.setValue(seconds);
        candleBuilder.reset(seconds);
        repo.fetchOhlc(activeCoinId, data -> {
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
        });
    }

    public void fetchMarketChart(String id) { repo.fetchMarketChart(id, marketChart); }

    public void startPricePolling() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = AppExecutors.getInstance().newScheduler();
        scheduler.scheduleAtFixedRate(() ->
            repo.fetchLivePrice(activeCoinId, price -> {
                livePrice.postValue(price);
                candleBuilder.addTick(price, System.currentTimeMillis());
                chartCandles.postValue(candleBuilder.getCandles());
            }, errorMessage),
            0, 10, TimeUnit.SECONDS);
    }

    public void stopPricePolling() { if (scheduler != null) scheduler.shutdownNow(); }

    @Override protected void onCleared() { stopPricePolling(); }
}
```

- [ ] **Step 3.2: Pastikan build bersih**

```
./gradlew :app:compileDebugJavaWithJavac
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3.3: Commit**

```
git add app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java
git commit -m "feat(viewmodel): add chartCandles LiveData and real-time tick feeding"
```

---

## Task 4: CandlestickChartView — Live Candle + Price Line

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/ui/terminal/CandlestickChartView.java`

- [ ] **Step 4.1: Update CandlestickChartView**

Ganti seluruh isi `CandlestickChartView.java` dengan versi berikut:

```java
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

            // Body — candle terakhir pakai outline (live, masih berjalan)
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
```

- [ ] **Step 4.2: Pastikan build bersih**

```
./gradlew :app:compileDebugJavaWithJavac
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4.3: Commit**

```
git add app/src/main/java/com/diellabs/frexa/ui/terminal/CandlestickChartView.java
git commit -m "feat(chart): add live candle outline and current price dashed line"
```

---

## Task 5: fragment_terminal.xml — Timeframe Toggle Row

**Files:**
- Modify: `app/src/main/res/layout/fragment_terminal.xml`

- [ ] **Step 5.1: Tambah timeframe row di atas chart**

Di `fragment_terminal.xml`, sisipkan `LinearLayout` timeframe buttons antara `RelativeLayout` top bar (tutup dengan `</RelativeLayout>`) dan `<com.diellabs.frexa.ui.terminal.CandlestickChartView`:

```xml
<!-- Timeframe toggle -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:paddingStart="12dp"
    android:paddingEnd="12dp"
    android:paddingBottom="4dp">

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_tf_1m"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="30dp"
        android:layout_weight="1"
        android:layout_marginEnd="3dp"
        android:text="1m"
        android:textSize="11sp"
        android:textAllCaps="false"
        android:insetTop="0dp"
        android:insetBottom="0dp"
        app:backgroundTint="@color/frx_green"
        app:strokeColor="@android:color/transparent"
        app:strokeWidth="0dp" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_tf_5m"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="30dp"
        android:layout_weight="1"
        android:layout_marginStart="3dp"
        android:layout_marginEnd="3dp"
        android:text="5m"
        android:textSize="11sp"
        android:textAllCaps="false"
        android:insetTop="0dp"
        android:insetBottom="0dp"
        app:backgroundTint="@android:color/transparent"
        app:strokeColor="@color/frx_text_3"
        app:strokeWidth="1dp" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_tf_15m"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="30dp"
        android:layout_weight="1"
        android:layout_marginStart="3dp"
        android:layout_marginEnd="3dp"
        android:text="15m"
        android:textSize="11sp"
        android:textAllCaps="false"
        android:insetTop="0dp"
        android:insetBottom="0dp"
        app:backgroundTint="@android:color/transparent"
        app:strokeColor="@color/frx_text_3"
        app:strokeWidth="1dp" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_tf_30m"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="30dp"
        android:layout_weight="1"
        android:layout_marginStart="3dp"
        android:layout_marginEnd="3dp"
        android:text="30m"
        android:textSize="11sp"
        android:textAllCaps="false"
        android:insetTop="0dp"
        android:insetBottom="0dp"
        app:backgroundTint="@android:color/transparent"
        app:strokeColor="@color/frx_text_3"
        app:strokeWidth="1dp" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btn_tf_1h"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="30dp"
        android:layout_weight="1"
        android:layout_marginStart="3dp"
        android:text="1h"
        android:textSize="11sp"
        android:textAllCaps="false"
        android:insetTop="0dp"
        android:insetBottom="0dp"
        app:backgroundTint="@android:color/transparent"
        app:strokeColor="@color/frx_text_3"
        app:strokeWidth="1dp" />

</LinearLayout>
```

Pastikan posisinya: setelah penutup `</RelativeLayout>` top bar, **sebelum** `<com.diellabs.frexa.ui.terminal.CandlestickChartView`.

- [ ] **Step 5.2: Pastikan build bersih**

```
./gradlew :app:compileDebugJavaWithJavac
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5.3: Commit**

```
git add app/src/main/res/layout/fragment_terminal.xml
git commit -m "feat(terminal): add timeframe toggle row above chart"
```

---

## Task 6: TerminalFragment — Wire Up Everything

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/ui/terminal/TerminalFragment.java`

- [ ] **Step 6.1: Update TerminalFragment**

Ganti seluruh isi `TerminalFragment.java` dengan versi berikut:

```java
package com.diellabs.frexa.ui.terminal;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.diellabs.frexa.databinding.FragmentTerminalBinding;
import com.diellabs.frexa.ui.deposit.DepositBottomSheetFragment;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.util.UserPrefs;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.TradingViewModel;
import com.google.android.material.button.MaterialButton;

public class TerminalFragment extends Fragment {
    private FragmentTerminalBinding b;
    private CryptoViewModel cryptoVm;
    private TradingViewModel tradingVm;
    private UserPrefs prefs;
    private double stakeAmount = 10.0;
    private final int[] DURATIONS = {60, 300, 900, 1800, 3600};
    private final String[] DURATION_LABELS = {"1 mnt", "5 mnt", "15 mnt", "30 mnt", "1 jam"};
    private int durationIndex = 0;
    private int durationSeconds = 60;
    private String durationLabel = "1 mnt";
    private int profitPercent = 85;
    private double currentPrice = 0;

    private static final int COLOR_ACTIVE = 0xFF36E07A;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle saved) {
        b = FragmentTerminalBinding.inflate(inf, parent, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        prefs = new UserPrefs(requireContext());
        cryptoVm = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);
        tradingVm = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);

        String coinId = prefs.getActiveCoinId();
        cryptoVm.setActiveCoin(coinId);

        setupStakeControls();
        setupDurationControls();
        setupTimeframeButtons();

        b.btnWallet.setOnClickListener(x ->
            new DepositBottomSheetFragment().show(getChildFragmentManager(), "deposit"));

        // Chart update dari chartCandles (real-time)
        cryptoVm.chartCandles.observe(getViewLifecycleOwner(),
            data -> b.chart.setOhlcData(data));

        // Harga live → update price line di chart
        cryptoVm.livePrice.observe(getViewLifecycleOwner(), price -> {
            currentPrice = price;
            b.chart.setCurrentPrice(price);
        });

        tradingVm.virtualBalance.observe(getViewLifecycleOwner(), bal ->
            b.tvBalance.setText(CurrencyFormatter.formatBalance(bal)));

        cryptoVm.coinList.observe(getViewLifecycleOwner(), coins -> {
            coins.stream().filter(c -> c.id.equals(coinId)).findFirst().ifPresent(c -> {
                b.tvAccountName.setText(c.symbol.toUpperCase() + "/USD");
                profitPercent = c.marketCapRank <= 10 ? 90 : c.marketCapRank <= 30 ? 80 : 75;
                b.tvProfitLabel.setText("Profit: " + profitPercent + "%");
            });
        });

        cryptoVm.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) b.tvOffline.setVisibility(View.VISIBLE);
            else b.tvOffline.setVisibility(View.GONE);
        });

        b.btnUp.setOnClickListener(x -> placeTrade("UP"));
        b.btnDown.setOnClickListener(x -> placeTrade("DOWN"));
    }

    private void setupStakeControls() {
        b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        b.btnStakeMinus.setOnClickListener(x -> {
            stakeAmount = Math.max(1, stakeAmount - 1);
            b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        });
        b.btnStakePlus.setOnClickListener(x -> {
            stakeAmount += 1;
            b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        });
    }

    private void setupDurationControls() {
        b.btnDuration.setOnClickListener(x -> {
            DurationBottomSheetFragment sheet = new DurationBottomSheetFragment();
            sheet.setCallback((sec, label) -> {
                durationSeconds = sec;
                durationLabel = label;
                b.tvDuration.setText(label);
                for (int i = 0; i < DURATIONS.length; i++) {
                    if (DURATIONS[i] == sec) { durationIndex = i; break; }
                }
            });
            sheet.show(getChildFragmentManager(), "duration");
        });

        b.btnDurationMinus.setOnClickListener(x -> {
            if (durationIndex > 0) { durationIndex--; updateDurationUI(); }
        });
        b.btnDurationPlus.setOnClickListener(x -> {
            if (durationIndex < DURATIONS.length - 1) { durationIndex++; updateDurationUI(); }
        });
    }

    private final MaterialButton[] tfButtons = new MaterialButton[5];

    private void setupTimeframeButtons() {
        tfButtons[0] = b.btnTf1m;
        tfButtons[1] = b.btnTf5m;
        tfButtons[2] = b.btnTf15m;
        tfButtons[3] = b.btnTf30m;
        tfButtons[4] = b.btnTf1h;

        int[] seconds = {60, 300, 900, 1800, 3600};

        for (int i = 0; i < tfButtons.length; i++) {
            final int idx = i;
            final int sec = seconds[i];
            tfButtons[i].setOnClickListener(x -> selectTimeframe(sec, idx));
        }

        // Default aktif: tombol pertama (1m)
        highlightTimeframeButton(0);
    }

    private void selectTimeframe(int seconds, int index) {
        cryptoVm.setTimeframe(seconds);
        highlightTimeframeButton(index);
    }

    private void highlightTimeframeButton(int activeIndex) {
        for (int i = 0; i < tfButtons.length; i++) {
            if (i == activeIndex) {
                tfButtons[i].setBackgroundTintList(
                    ColorStateList.valueOf(COLOR_ACTIVE));
                tfButtons[i].setStrokeWidth(0);
            } else {
                tfButtons[i].setBackgroundTintList(
                    ColorStateList.valueOf(Color.TRANSPARENT));
                tfButtons[i].setStrokeWidth(2);
                tfButtons[i].setStrokeColor(
                    ColorStateList.valueOf(0xFF4A4D55));
            }
        }
    }

    private void updateDurationUI() {
        durationSeconds = DURATIONS[durationIndex];
        durationLabel = DURATION_LABELS[durationIndex];
        b.tvDuration.setText(durationLabel);
    }

    private void placeTrade(String direction) {
        if (stakeAmount > prefs.getBalance()) {
            Toast.makeText(requireContext(), "Saldo tidak cukup", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPrice == 0) {
            Toast.makeText(requireContext(), "Menunggu harga terkini", Toast.LENGTH_SHORT).show();
            return;
        }
        String coinId = prefs.getActiveCoinId();
        String[] nameSymbolImg = {"Bitcoin", "BTC", ""};
        if (cryptoVm.coinList.getValue() != null) {
            cryptoVm.coinList.getValue().stream()
                .filter(c -> c.id.equals(coinId)).findFirst().ifPresent(c -> {
                    nameSymbolImg[0] = c.name;
                    nameSymbolImg[1] = c.symbol;
                    nameSymbolImg[2] = c.image;
                });
        }
        tradingVm.placeTrade(coinId, nameSymbolImg[0], nameSymbolImg[1], nameSymbolImg[2],
            direction, stakeAmount, profitPercent, currentPrice, durationSeconds, durationLabel);
        Toast.makeText(requireContext(),
            direction.equals("UP") ? "↑ Posisi NAIK dibuka!" : "↓ Posisi TURUN dibuka!",
            Toast.LENGTH_SHORT).show();
    }

    @Override public void onResume() { super.onResume(); cryptoVm.startPricePolling(); }
    @Override public void onPause() { super.onPause(); cryptoVm.stopPricePolling(); }
    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
```

- [ ] **Step 6.2: Build lengkap + run unit tests**

```
./gradlew :app:test
```

Expected: BUILD SUCCESSFUL, semua tests pass.

- [ ] **Step 6.3: Commit**

```
git add app/src/main/java/com/diellabs/frexa/ui/terminal/TerminalFragment.java
git commit -m "feat(terminal): wire chartCandles observer and timeframe toggle buttons"
```

---

## Verifikasi Manual

Setelah semua tasks selesai, buka app di emulator atau device:

1. Navigasi ke tab Terminal
2. Pastikan chart tampil dengan historical candles (dari CoinGecko OHLC)
3. Tunggu 10 detik — candle terakhir (outline) harus update close/high/low
4. Tap tombol **5m** — chart refresh dengan data baru, candle outline muncul di kanan
5. Tap tombol **1h** — chart refresh lagi
6. Pastikan garis putus-putus harga terlihat di posisi harga terkini
7. Tombol aktif harus berwarna hijau, sisanya outline
