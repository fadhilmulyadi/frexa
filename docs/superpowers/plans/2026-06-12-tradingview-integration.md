# TradingView Real-Time Chart Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrasi TradingView Lightweight Charts ke Frexa untuk tampilan chart yang benar-benar real-time, mulus, dan interaktif (seperti OlympTrade).

**Architecture:**
- **UI:** Mengganti `CandlestickChartView` dengan `WebView`.
- **Frontend:** Menggunakan `chart.html` lokal yang memuat library Lightweight Charts.
- **Bridge:** Menggunakan `@JavascriptInterface` (Java to JS) untuk mengirim data OHLC dan price ticks secara instan.
- **Backend:** Data tetap berasal dari Bitfinex Proxy (Polling 1s).

**Tech Stack:** Android Java, WebView, JavaScript, TradingView Lightweight Charts.

---

## Task 1: Buat Chart Assets (HTML/JS)

**Files:**
- Create: `app/src/main/assets/chart.html`

- [ ] **Step 1: Buat file HTML chart**

```html
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <style>
        body { margin: 0; padding: 0; background-color: #131722; overflow: hidden; }
        #chart-container { width: 100vw; height: 100vh; }
    </style>
    <!-- Use CDN for the library -->
    <script src="https://unpkg.com/lightweight-charts/dist/lightweight-charts.standalone.production.js"></script>
</head>
<body>
    <div id="chart-container"></div>
    <script>
        const container = document.getElementById('chart-container');
        const chart = LightweightCharts.createChart(container, {
            layout: { backgroundColor: '#131722', textColor: '#d1d4dc' },
            grid: { vertLines: { color: '#2B2B43' }, horzLines: { color: '#2B2B43' } },
            crosshair: { mode: LightweightCharts.CrosshairMode.Normal },
            rightPriceScale: { borderColor: '#2B2B43' },
            timeScale: { borderColor: '#2B2B43', timeVisible: true, secondsVisible: true }
        });

        const candleSeries = chart.addCandlestickSeries({
            upColor: '#36E07A', downColor: '#FF5D5D',
            borderVisible: false, wickUpColor: '#36E07A', wickDownColor: '#FF5D5D'
        });

        // Bridge functions
        window.setData = (data) => {
            // Data format: [{time: unix_sec, open, high, low, close}, ...]
            candleSeries.setData(data);
        };

        window.updatePrice = (tick) => {
            // Tick format: {time: unix_sec, open, high, low, close}
            candleSeries.update(tick);
        };

        window.onResize = () => {
            chart.applyOptions({ width: container.clientWidth, height: container.clientHeight });
        };
    </script>
</body>
</html>
```

---

## Task 2: Update Layout (Replace Custom View)

**Files:**
- Modify: `app/src/main/res/layout/fragment_terminal.xml`

- [ ] **Step 1: Ganti CandlestickChartView dengan WebView**

```xml
    <!-- Gantilah bagian ini -->
    <WebView
        android:id="@+id/wv_chart"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
```

---

## Task 3: Implementasi WebView Bridge di Java

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/ui/terminal/TerminalFragment.java`

- [ ] **Step 1: Setup WebView di onViewCreated**

- Tambahkan import yang diperlukan.
- Konfigurasi Settings WebView (JS enabled, DOM storage).
- Load `file:///android_asset/chart.html`.

- [ ] **Step 2: Hubungkan CryptoViewModel ke WebView**

- Di observer `chartCandles`, konversi data ke JSON Array dan panggil `wv_chart.evaluateJavascript("setData(...)")`.
- Di observer `livePrice`, buat object JSON tick dan panggil `wv_chart.evaluateJavascript("updatePrice(...)")`.

---

## Task 4: Tambahkan Permission & Proguard

**Files:**
- Modify: `app/src/main/AndroidManifest.xml` (Ensure internet access for CDN)
- Modify: `app/proguard-rules.pro` (Ensure JS Interface is not stripped)

- [ ] **Step 1: Verifikasi permission**

---

## Task 5: Final Verification

- [ ] **Step 1: Build & Run**
- [ ] **Step 2: Pastikan chart interaktif (zoom/pan)**
- [ ] **Step 3: Pastikan harga update terus tanpa lag**
