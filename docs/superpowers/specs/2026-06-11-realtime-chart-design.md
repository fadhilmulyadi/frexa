# Real-Time Candlestick Chart — Design Spec

**Date:** 2026-06-11  
**Branch:** feature/ui-redesign  
**Goal:** Chart di TerminalFragment update secara real-time seperti OlympTrade — candle terakhir "bernafas" mengikuti harga live, dengan timeframe switchable dari UI.

---

## 1. Problem Statement

Chart saat ini statis: `setActiveCoin()` memanggil `fetchOhlc()` sekali saja, dan hasilnya tidak pernah diperbarui. `livePrice` memang di-poll tiap 10 detik, tapi tidak pernah digunakan untuk mengupdate chart.

---

## 2. Approach: Client-Side Tick Accumulation

Setiap tick harga yang masuk dari polling CoinGecko (tiap 10 detik) dimasukkan ke `LiveCandleBuilder`. Builder membangun candle sesuai timeframe yang dipilih. Chart di-render ulang setiap tick — menghasilkan efek candle terakhir yang terus bergerak.

**Tidak ada API atau dependency baru.** Semua berbasis polling CoinGecko yang sudah ada.

---

## 3. Architecture

```
livePrice polling (tiap 10s, CoinGecko)
        │
        ▼
  LiveCandleBuilder  ◄── historical OHLC (fetchOhlc, sekali saat setActiveCoin/setTimeframe)
  [period: 60/300/900/1800/3600 detik]
        │
        ▼
  chartCandles LiveData  ──►  CandlestickChartView
        
  TerminalFragment
  [toggle: 1m | 5m | 15m | 30m | 1h]
        │ cryptoVm.setTimeframe(seconds)
        ▼
  CryptoViewModel → candleBuilder.reset() + fetchOhlc()
```

---

## 4. Component Designs

### 4.1 `LiveCandleBuilder` (baru)

File: `app/src/main/java/com/diellabs/frexa/ui/terminal/LiveCandleBuilder.java`

**Candle format** (sesuai CoinGecko OHLC): `[timestamp, open, high, low, close]`

**Fields:**
- `List<List<Double>> historicalCandles` — dari CoinGecko OHLC, diset sekali via `setHistoricalCandles()`
- `List<List<Double>> liveCandles` — candle yang sudah ditutup, dibangun dari ticks
- `List<Double> currentCandle` — candle yang sedang berjalan (null jika belum ada tick)
- `int periodSeconds` — 60, 300, 900, 1800, atau 3600
- `long periodStart` — timestamp awal period candle saat ini (ms)

**Methods:**
- `LiveCandleBuilder(int periodSeconds)` — konstruktor
- `void setHistoricalCandles(List<List<Double>> ohlc)` — seed historical, clear live state
- `void addTick(double price, long timestampMs)`:
  - Jika `currentCandle == null`: buka candle baru (open=close=high=low=price, periodStart=floor timestamp ke period)
  - Jika masih dalam period: update close, adjust high/low
  - Jika period habis: tutup current (tambah ke `liveCandles`), buka candle baru
- `List<List<Double>> getCandles()`: gabung `historicalCandles` + `liveCandles`, ambil 59 terakhir, append `currentCandle` → max 60 candle
- `void reset(int newPeriodSeconds)`: ganti period, clear `liveCandles` dan `currentCandle`

**Period boundary logic:**
```
periodStart = (timestampMs / (periodSeconds * 1000L)) * (periodSeconds * 1000L)
```
Candle baru dimulai saat `timestampMs >= periodStart + periodSeconds * 1000L`.

### 4.2 `CryptoViewModel` (update)

File: `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java`

**Tambahan fields:**
```java
private final LiveCandleBuilder candleBuilder = new LiveCandleBuilder(60);
public final MutableLiveData<List<List<Double>>> chartCandles = new MutableLiveData<>();
public final MutableLiveData<Integer> selectedTimeframe = new MutableLiveData<>(60);
```

**`setActiveCoin(String id)` — diupdate:**
```java
public void setActiveCoin(String id) {
    activeCoinId = id;
    candleBuilder.reset(selectedTimeframe.getValue() != null ? selectedTimeframe.getValue() : 60);
    repo.fetchOhlc(id, data -> {
        candleBuilder.setHistoricalCandles(data);
        chartCandles.postValue(candleBuilder.getCandles());
    });
}
```

**`startPricePolling()` — diupdate**, tiap tick feed ke builder:
```java
scheduler.scheduleAtFixedRate(() -> {
    repo.fetchLivePrice(activeCoinId, price -> {
        livePrice.postValue(price);
        candleBuilder.addTick(price, System.currentTimeMillis());
        chartCandles.postValue(candleBuilder.getCandles());
    }, errorMessage);
}, 0, 10, TimeUnit.SECONDS);
```

**Baru: `setTimeframe(int seconds)`:**
```java
public void setTimeframe(int seconds) {
    selectedTimeframe.setValue(seconds);
    candleBuilder.reset(seconds);
    repo.fetchOhlc(activeCoinId, data -> {
        candleBuilder.setHistoricalCandles(data);
        chartCandles.postValue(candleBuilder.getCandles());
    });
}
```

`ohlcData` LiveData yang lama tetap ada — digunakan oleh layar lain (AssetDetail, QuoteHistory).

### 4.3 `CandlestickChartView` (update)

File: `app/src/main/java/com/diellabs/frexa/ui/terminal/CandlestickChartView.java`

**Tambahan:**
- `private double currentPrice = 0` — di-set via `setCurrentPrice(double)`
- `private final Paint liveOutlinePaint` — stroke, warna hijau/merah sesuai arah candle
- `private final Paint pricePaint` — dashed line horizontal, warna putih/abu terang

**Candle terakhir** dirender dengan outline (bukan fill solid), menandakan candle masih berjalan:
```java
boolean isLast = (i == visible.size() - 1);
Paint bodyPaint = isLast ? liveOutlinePaint : (bull ? upPaint : downPaint);
if (isLast) liveOutlinePaint.setColor(bull ? 0xFF36E07A : 0xFFFF5D5D);
canvas.drawRect(x - candleW/2, Math.min(yOpen, yClose),
                x + candleW/2, Math.max(yOpen, yClose), bodyPaint);
```

**Price line** — setelah semua candle digambar:
```java
if (currentPrice > 0) {
    float yPrice = toY(currentPrice, minLow, range, h, pad);
    pricePaint.setPathEffect(new DashPathEffect(new float[]{8, 6}, 0));
    canvas.drawLine(0, yPrice, w, yPrice, pricePaint);
}
```

### 4.4 `TerminalFragment` (update)

**Observe `chartCandles` (bukan `ohlcData`):**
```java
cryptoVm.chartCandles.observe(getViewLifecycleOwner(),
    data -> b.chart.setOhlcData(data));

cryptoVm.livePrice.observe(getViewLifecycleOwner(), price -> {
    currentPrice = price;
    b.chart.setCurrentPrice(price);
});
```

**Timeframe buttons:**
```java
b.btnTf1m.setOnClickListener(x  -> selectTimeframe(60,   b.btnTf1m));
b.btnTf5m.setOnClickListener(x  -> selectTimeframe(300,  b.btnTf5m));
b.btnTf15m.setOnClickListener(x -> selectTimeframe(900,  b.btnTf15m));
b.btnTf30m.setOnClickListener(x -> selectTimeframe(1800, b.btnTf30m));
b.btnTf1h.setOnClickListener(x  -> selectTimeframe(3600, b.btnTf1h));

private void selectTimeframe(int seconds, View selected) {
    cryptoVm.setTimeframe(seconds);
    highlightTimeframeButton(selected);
}
```

**`highlightTimeframeButton()`** — set alpha/background aktif pada tombol terpilih, reset sisanya.

### 4.5 `fragment_terminal.xml` (update)

Tambah `LinearLayout` horizontal berisi 5 `MaterialButton` timeframe, ditempatkan langsung di atas `CandlestickChartView`. Style: outline `MaterialButton` dengan state selector untuk aktif/non-aktif.

---

## 5. Files Changed

| File | Perubahan |
|------|-----------|
| `LiveCandleBuilder.java` | **Baru** — pure logic class |
| `CryptoRepository.java` | Tambah callback overload untuk `fetchOhlc` dan `fetchLivePrice` |
| `CryptoViewModel.java` | Tambah `chartCandles`, `setTimeframe()`, feed ticks ke builder |
| `CandlestickChartView.java` | Live candle outline, price line |
| `TerminalFragment.java` | Observe `chartCandles`, timeframe buttons |
| `fragment_terminal.xml` | Row 5 tombol timeframe di atas chart |

---

## 6. CryptoRepository — Callback Overloads Needed

`CryptoRepository` saat ini pakai signature `fetchOhlc(String, MutableLiveData<...>)` dan `fetchLivePrice(String, MutableLiveData<Double>, MutableLiveData<String>)`. ViewModel perlu callback agar bisa feed data ke builder tanpa coupling ke LiveData.

Tambah overload di `CryptoRepository`:
```java
// Callback interface (inner atau terpisah)
public interface OhlcCallback { void onResult(List<List<Double>> data); }
public interface PriceCallback { void onPrice(double price); }

public void fetchOhlc(String coinId, OhlcCallback callback) { ... }
public void fetchLivePrice(String coinId, PriceCallback callback,
                           MutableLiveData<String> error) { ... }
```

Overload lama tetap ada untuk layar lain yang masih pakai LiveData langsung.

---

## 7. Constraints & Notes

- **CoinGecko OHLC granularity**: `days=1` → 30-menit candles. Ini hanya untuk historical backdrop. Candle 1m/5m/15m dibangun penuh dari ticks client-side.
- **Session-only live candles**: Candle yang dibangun dari ticks tidak dipersist — reset saat app restart. Historical OHLC tetap di-fetch ulang.
- **Rate limit**: Polling 10 detik tetap sama. Tidak ada request tambahan kecuali saat `setTimeframe()` dipanggil (trigger `fetchOhlc` ulang).
- **Thread safety**: `LiveCandleBuilder` dipanggil dari background thread scheduler. Semua update ke LiveData via `postValue()`.
