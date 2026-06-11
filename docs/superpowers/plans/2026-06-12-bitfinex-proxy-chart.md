# Bitfinex Proxy Chart Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementasi chart real-time menggunakan Bitfinex API melalui Cloudflare Worker Proxy untuk menghindari blokir ISP Indonesia (SSL/403).

**Architecture:**
- **Proxy:** Cloudflare Worker bertindak sebagai gateway ke `api-pub.bitfinex.com`.
- **REST:** Mengambil historical candles `trade:1m:tBTCUSD/hist`.
- **Parsing:** Bitfinex mengembalikan `List<List<Double>>` (Array of Arrays) yang perlu dipetakan ke format chart.
- **Symbols:** Menggunakan format Bitfinex (e.g., `tBTCUSD`, `tETHUSD`).

**Tech Stack:** Android Java, Retrofit 2, OkHttp 4, Cloudflare Workers.

---

## Task 1: Buat BitfinexService

**Files:**
- Create: `app/src/main/java/com/diellabs/frexa/data/remote/api/BitfinexService.java`

- [ ] **Step 1: Buat file interface Retrofit**

```java
package com.diellabs.frexa.data.remote.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BitfinexService {
    /**
     * Bitfinex returns: [[MTS, OPEN, CLOSE, HIGH, LOW, VOLUME], ...]
     * Note: Close and High are swapped compared to some other APIs
     */
    @GET("bitfinex/candles/trade:{timeframe}:{symbol}/hist")
    Call<List<List<Double>>> getCandles(
        @Path("timeframe") String timeframe,
        @Path("symbol") String symbol,
        @Query("limit") int limit
    );
}
```

---

## Task 2: Update RetrofitClient (Point to Proxy)

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/data/remote/api/RetrofitClient.java`

- [ ] **Step 1: Ganti PROXY_URL dengan URL Cloudflare Worker milik user**
User hint: Ganti `https://frexa-api-gateway.user.workers.dev/` dengan URL asli.

```java
package com.diellabs.frexa.data.remote.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String COINGECKO = "https://api.coingecko.com/api/v3/";
    private static final String GEMINI    = "https://generativelanguage.googleapis.com/";
    // TODO: Ganti dengan URL Cloudflare Worker Anda
    private static final String PROXY_BASE = "https://frexa-api-gateway.user.workers.dev/";

    private static CoinGeckoService coinGecko;
    private static GeminiService    gemini;
    private static BitfinexService  bitfinex;

    private static OkHttpClient client() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder().addInterceptor(log)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public static CoinGeckoService getCoinGeckoService() {
        if (coinGecko == null) coinGecko = new Retrofit.Builder()
                .baseUrl(COINGECKO).client(client())
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(CoinGeckoService.class);
        return coinGecko;
    }

    public static GeminiService getGeminiService() {
        if (gemini == null) gemini = new Retrofit.Builder()
                .baseUrl(GEMINI).client(client())
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(GeminiService.class);
        return gemini;
    }

    public static BitfinexService getBitfinexService() {
        if (bitfinex == null) bitfinex = new Retrofit.Builder()
                .baseUrl(PROXY_BASE).client(client())
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(BitfinexService.class);
        return bitfinex;
    }
}
```

---

## Task 3: Update CoinSymbolMapper

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/data/remote/api/CoinSymbolMapper.java`

- [ ] **Step 1: Tambahkan format symbol Bitfinex**

```java
package com.diellabs.frexa.data.remote.api;

import java.util.HashMap;
import java.util.Map;

public final class CoinSymbolMapper {
    private static final Map<String, String> COIN_TO_BASE = new HashMap<>();

    static {
        COIN_TO_BASE.put("bitcoin",            "BTC");
        COIN_TO_BASE.put("ethereum",           "ETH");
        COIN_TO_BASE.put("binancecoin",        "BNB");
        COIN_TO_BASE.put("solana",             "SOL");
        COIN_TO_BASE.put("ripple",             "XRP");
        COIN_TO_BASE.put("cardano",            "ADA");
        COIN_TO_BASE.put("dogecoin",           "DOGE");
        COIN_TO_BASE.put("polkadot",           "DOT");
        COIN_TO_BASE.put("avalanche-2",        "AVAX");
        COIN_TO_BASE.put("tron",               "TRX");
        COIN_TO_BASE.put("chainlink",          "LINK");
        COIN_TO_BASE.put("polygon",            "MATIC");
        COIN_TO_BASE.put("litecoin",           "LTC");
        COIN_TO_BASE.put("uniswap",            "UNI");
        COIN_TO_BASE.put("near",               "NEAR");
        COIN_TO_BASE.put("stellar",            "XLM");
        COIN_TO_BASE.put("cosmos",             "ATOM");
    }

    public static String toBitfinexSymbol(String coinId) {
        String base = COIN_TO_BASE.get(coinId);
        if (base == null) base = coinId.replace("-", "").toUpperCase();
        return "t" + base + "USD";
    }

    public static String toBitfinexTimeframe(int seconds) {
        if (seconds >= 86400) return "1D";
        if (seconds >= 3600)  return "1h";
        if (seconds >= 900)   return "15m";
        if (seconds >= 300)   return "5m";
        return "1m";
    }
}
```

---

## Task 4: Update CryptoRepository

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/data/repository/CryptoRepository.java`

- [ ] **Step 1: Tambahkan fetchBitfinexKlines**

```java
    public void fetchBitfinexKlines(String symbol, int seconds, int limit, OhlcCallback callback) {
        String tf = CoinSymbolMapper.toBitfinexTimeframe(seconds);
        bitfinexApi.getCandles(tf, symbol, limit)
            .enqueue(new Callback<List<List<Double>>>() {
                @Override
                public void onResponse(Call<List<List<Double>>> c, Response<List<List<Double>>> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        callback.onResult(parseBitfinexKlines(r.body()));
                    }
                }
                @Override public void onFailure(Call<List<List<Double>>> c, Throwable t) {}
            });
    }

    /** 
     * Bitfinex format: [MTS, OPEN, CLOSE, HIGH, LOW, VOLUME]
     * Chart format: [Timestamp, Open, High, Low, Close]
     */
    private List<List<Double>> parseBitfinexKlines(List<List<Double>> body) {
        List<List<Double>> result = new ArrayList<>();
        // Bitfinex returns newest first, we need oldest first for Chart
        Collections.reverse(body); 
        for (List<Double> k : body) {
            if (k.size() >= 5) {
                result.add(Arrays.asList(
                    k.get(0), // MTS
                    k.get(1), // Open
                    k.get(3), // High
                    k.get(4), // Low
                    k.get(2)  // Close
                ));
            }
        }
        return result;
    }
```

---

## Task 5: Update CryptoViewModel

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java`

- [ ] **Step 1: Hubungkan ke Bitfinex di ViewModel**

Ganti panggillan `fetchBitgetKlines` dengan `fetchBitfinexKlines`. Matikan dulu WebSocket karena kita fokus pada kestabilan chart REST terlebih dahulu.

---

## Task 6: Verifikasi & Test

- [ ] **Step 1: Jalankan Unit Test CoinSymbolMapperTest**
- [ ] **Step 2: Build APK & Cek Chart**
