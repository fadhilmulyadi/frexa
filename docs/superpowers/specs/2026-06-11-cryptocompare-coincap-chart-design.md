# CryptoCompare + CoinCap Real-Time Chart — Design Spec

**Date:** 2026-06-11  
**Branch:** feature/ui-redesign  
**Replaces:** Binance API (blocked by Indonesian ISP/OJK)

---

## Problem

Binance API (`api.binance.com`, `stream.binance.com`) diblokir oleh ISP Indonesia (DNS redirect ke server Telkom). Perlu diganti dengan provider yang accessible dari Indonesia.

---

## Solution

- **CryptoCompare REST** → historical OHLCV candlestick data
- **CoinCap WebSocket** → real-time price ticks untuk live candle animation

Kedua provider ini gratis, tidak butuh API key, dan accessible dari Indonesia.

---

## API Details

### CryptoCompare REST

Base URL: `https://min-api.cryptocompare.com/`

Endpoints yang dipakai:
- `GET /data/v2/histominute?fsym=BTC&tsym=USD&limit=60&aggregate=1` — 1-menit candles
- `GET /data/v2/histominute?fsym=BTC&tsym=USD&limit=60&aggregate=5` — 5-menit candles
- `GET /data/v2/histominute?fsym=BTC&tsym=USD&limit=60&aggregate=15` — 15-menit candles
- `GET /data/v2/histominute?fsym=BTC&tsym=USD&limit=60&aggregate=30` — 30-menit candles
- `GET /data/v2/histohour?fsym=BTC&tsym=USD&limit=60` — 1-jam candles

Response format:
```json
{
  "Response": "Success",
  "Data": {
    "Data": [
      {
        "time": 1749654000,
        "open": 104900.0,
        "high": 105000.0,
        "low": 104800.0,
        "close": 105000.0,
        "volumefrom": 5.0,
        "volumeto": 524500.0
      }
    ]
  }
}
```

Candle format yang dihasilkan: `[timestamp_ms, open, high, low, close]` — konsisten dengan format `LiveCandleBuilder`.

### CoinCap WebSocket

URL: `wss://ws.coincap.io/prices?assets=bitcoin`

- Untuk ganti coin: disconnect + reconnect dengan URL baru
- Tidak ada subscribe/unsubscribe message — asset ditentukan lewat query parameter
- Response format: `{"bitcoin": "65432.12"}`
- Interval update: ~500ms (lebih dari cukup untuk chart smooth)

---

## Component Map

### Files Baru

| File | Tanggung Jawab |
|------|----------------|
| `data/remote/api/CryptoCompareService.java` | Retrofit interface: `getHistoMinute()`, `getHistoHour()` |
| `data/remote/model/CryptoCompareHistominute.java` | POJO untuk response CryptoCompare |
| `data/remote/api/CoinCapWebSocketManager.java` | WebSocket manager untuk CoinCap price ticks |

### Files yang Diubah

| File | Perubahan |
|------|-----------|
| `data/remote/api/RetrofitClient.java` | Tambah `getCryptoCompareService()`, hapus `getBinanceService()` |
| `data/remote/api/CoinSymbolMapper.java` | Tambah `toCryptoCompareFsym()` (bitcoin → BTC), update `toInterval()` untuk CryptoCompare |
| `data/repository/CryptoRepository.java` | Tambah `fetchCryptoCompareKlines()`, hapus `fetchBinanceKlines()` |
| `viewmodel/CryptoViewModel.java` | Ganti `BinanceWebSocketManager` → `CoinCapWebSocketManager`, ganti `updateLiveCandle()` → `addTick()` |

### Files yang Dihapus

| File | Alasan |
|------|--------|
| `data/remote/api/BinanceService.java` | Digantikan CryptoCompareService |
| `data/remote/api/BinanceWebSocketManager.java` | Digantikan CoinCapWebSocketManager |
| `data/remote/model/BinanceKlineEvent.java` | Tidak dipakai lagi |

---

## Data Flow

### Inisialisasi Coin

```
setActiveCoin("bitcoin")
  ├─ symbol = CoinSymbolMapper.toCryptoCompareFsym("bitcoin") → "BTC"
  ├─ candleBuilder.reset(60)
  ├─ repo.fetchCryptoCompareKlines("BTC", "1m", 60, callback)
  │    └─ CryptoCompare: histominute?fsym=BTC&tsym=USD&limit=60
  │         → setHistoricalCandles(data) → chartCandles.postValue()
  └─ coinCapWs.switchAsset("bitcoin")  ← reconnect dengan URL baru

WebSocket tick → {"bitcoin": "65432.12"}
  → candleBuilder.addTick(65432.12, System.currentTimeMillis())
  → chartCandles.postValue(candleBuilder.getCandles())
  → livePrice.postValue(65432.12)
```

### Ganti Timeframe

```
setTimeframe(300)  // 5 menit
  ├─ candleBuilder.reset(300)
  ├─ repo.fetchCryptoCompareKlines("BTC", "5m", 60, callback)
  │    └─ CryptoCompare: histominute?fsym=BTC&tsym=USD&limit=60&aggregate=5
  │         → setHistoricalCandles(data) → chartCandles.postValue()
  └─ (WebSocket tidak berubah — CoinCap tetap connected ke "bitcoin")
```

### Timeframe → CryptoCompare Parameters

| Timeframe (detik) | Endpoint | Aggregate |
|-------------------|----------|-----------|
| 60 (1m) | histominute | 1 |
| 300 (5m) | histominute | 5 |
| 900 (15m) | histominute | 15 |
| 1800 (30m) | histominute | 30 |
| 3600 (1h) | histohour | - |

---

## CoinCapWebSocketManager Design

```java
public class CoinCapWebSocketManager {
    private static final String WS_BASE = "wss://ws.coincap.io/prices?assets=";

    public interface PriceListener {
        void onPrice(String assetId, double price);
        void onError(String message);
    }

    void connect(String assetId);      // connect to specific asset
    void switchAsset(String assetId);  // disconnect + reconnect
    void disconnect();
    boolean isConnected();
}
```

Perbedaan dari `BinanceWebSocketManager`:
- Tidak ada `subscribe()`/`unsubscribe()` — asset ditentukan di URL
- `switchAsset()` = `disconnect()` + `connect(newAssetId)`
- Listener menerima `(assetId, price)` bukan kline event

---

## CryptoCompareHistominute Model

```java
public class CryptoCompareHistominute {
    @SerializedName("Response") public String response;
    @SerializedName("Data")     public DataWrapper data;

    public static class DataWrapper {
        @SerializedName("Data") public List<Candle> data;
    }

    public static class Candle {
        @SerializedName("time")  public long time;   // Unix seconds
        @SerializedName("open")  public double open;
        @SerializedName("high")  public double high;
        @SerializedName("low")   public double low;
        @SerializedName("close") public double close;
    }
}
```

Catatan: `time` dari CryptoCompare adalah Unix **seconds** — perlu dikali 1000 untuk milliseconds saat dikonversi ke format `[timestamp_ms, open, high, low, close]`.

---

## Out of Scope

- Reconnect otomatis WebSocket saat koneksi putus — future work
- Error handling UI khusus saat CryptoCompare/CoinCap tidak tersedia
- Menambah coin baru ke mapper — bisa dilakukan terpisah
