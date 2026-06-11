# Binance Real-Time Chart — Design Spec

**Date:** 2026-06-11  
**Branch:** feature/ui-redesign  
**Status:** Approved

---

## Scope

Aktifkan real-time candlestick chart di TerminalFragment menggunakan Binance REST (historical klines) dan Binance WebSocket (live kline events). CoinGecko tetap dipakai untuk market list. Tidak ada fitur baru — hanya unblock koneksi Binance dan bersihkan double-subscription.

---

## API Split

| Data | Sumber | Alasan |
|------|--------|--------|
| Market list (harga, market cap, daftar coin) | CoinGecko | Metadata lengkap, tidak ada di Binance |
| Historical klines (chart awal) | Binance REST `/api/v3/klines` | Data OHLCV per timeframe |
| Live kline ticks (chart real-time) | Binance WebSocket `stream.binance.com` | Push-based, latency rendah |
| Live price di chart | WebSocket kline event (field `close`) | Sudah ada via `livePrice` LiveData |

---

## Task 1 — Fix SSL

**File baru:** `app/src/main/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

**Edit:** `AndroidManifest.xml` — tambah `android:networkSecurityConfig="@xml/network_security_config"` ke `<application>`.

Ini memperbaiki SSL untuk semua OkHttpClient di app (Retrofit + WebSocket) karena keduanya menggunakan SSLContext default Android yang mengikuti network security config.

---

## Task 2 — Fix Double-Subscription

**Root cause:** `onViewCreated()` memanggil `setActiveCoin()` yang sudah subscribe WebSocket. Lalu `onResume()` memanggil `startPricePolling()` yang subscribe lagi ke stream yang sama.

**Fix:** Ubah `startPricePolling()` di `CryptoViewModel` menjadi reconnect-only (tidak re-subscribe jika sudah ada `subscribedStream`). `BinanceWebSocketManager.connect()` sudah ada guard `if (connected && webSocket != null) return` — kita tambah guard serupa di `startPricePolling()`.

**Perubahan di `CryptoViewModel.startPricePolling()`:**
```java
public void startPricePolling() {
    // Hanya reconnect jika WebSocket putus; stream sudah di-set via setActiveCoin/setTimeframe
    if (!wsManager.isConnected()) {
        wsManager.connect();
    }
}
```

`stopPricePolling()` tetap memanggil `wsManager.disconnect()` — tidak berubah.

---

## Data Flow (setelah fix)

```
onViewCreated()
  └─ setActiveCoin(coinId)
       ├─ candleBuilder.reset()
       ├─ repo.fetchBinanceKlines() ──→ Binance REST
       │    └─ setHistoricalCandles() → chartCandles.postValue()
       └─ wsManager.connect() + switchStream("btcusdt@kline_1m")

onResume()
  └─ startPricePolling()
       └─ wsManager.connect() if !connected (no-op jika sudah connected)

WebSocket tick
  └─ onKline(event)
       ├─ candleBuilder.updateLiveCandle()
       ├─ chartCandles.postValue()     → chart.setOhlcData()
       └─ livePrice.postValue(close)  → chart.setCurrentPrice()

onPause()
  └─ stopPricePolling() → wsManager.disconnect()
```

---

## Files yang Diubah

| File | Perubahan |
|------|-----------|
| `app/src/main/res/xml/network_security_config.xml` | Baru — trust anchor config |
| `app/src/main/AndroidManifest.xml` | Tambah `networkSecurityConfig` attribute |
| `app/src/main/java/.../viewmodel/CryptoViewModel.java` | Fix `startPricePolling()` |

---

## Files Tidak Berubah

- `BinanceService.java` — sudah benar
- `BinanceWebSocketManager.java` — sudah benar
- `CoinSymbolMapper.java` — sudah benar
- `BinanceKlineEvent.java` — sudah benar
- `CryptoRepository.java` — sudah benar
- `LiveCandleBuilder.java` — sudah benar
- `TerminalFragment.java` — sudah benar
- `CryptoRepository.fetchMarkets()` — CoinGecko, tidak diubah

---

## Out of Scope

- Menambah coin baru ke `CoinSymbolMapper` — bisa dilakukan terpisah
- Reconnect otomatis WebSocket saat koneksi putus — future work
- Fallback ke CoinGecko OHLC jika Binance tidak tersedia — future work
