# CryptoCompare + CoinCap Real-Time Chart Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Ganti Binance API (diblokir di Indonesia) dengan CryptoCompare REST (historical OHLCV) + CoinCap WebSocket (live price ticks) agar chart real-time berfungsi dari Indonesia.

**Architecture:** CryptoCompare `histominute`/`histohour` menyediakan 60 candle OHLCV saat pertama load dan saat ganti timeframe. CoinCap WebSocket `wss://ws.coincap.io/prices?assets={coinId}` mengirim price tick setiap ~500ms yang diteruskan ke `LiveCandleBuilder.addTick()` untuk membangun live candle. Saat ganti coin, WebSocket reconnect ke URL baru. Saat ganti timeframe, WebSocket tidak berubah.

**Tech Stack:** Android Java, Retrofit 2, OkHttp 4, Gson, CryptoCompare API (no key needed), CoinCap WebSocket (no key needed)

---

## File Map

| File | Status | Tanggung Jawab |
|------|--------|----------------|
| `app/src/main/java/com/diellabs/frexa/data/remote/model/CryptoCompareHistominute.java` | Baru | POJO response CryptoCompare |
| `app/src/main/java/com/diellabs/frexa/data/remote/api/CryptoCompareService.java` | Baru | Retrofit interface histominute + histohour |
| `app/src/main/java/com/diellabs/frexa/data/remote/api/CoinCapWebSocketManager.java` | Baru | WebSocket manager CoinCap price ticks |
| `app/src/main/java/com/diellabs/frexa/data/remote/api/RetrofitClient.java` | Ubah | Ganti Binance → CryptoCompare client |
| `app/src/main/java/com/diellabs/frexa/data/remote/api/CoinSymbolMapper.java` | Ubah | Ganti Binance symbol → CryptoCompare fsym |
| `app/src/main/java/com/diellabs/frexa/data/repository/CryptoRepository.java` | Ubah | Tambah `fetchCryptoCompareKlines()`, hapus `fetchBinanceKlines()` |
| `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java` | Ubah | Ganti BinanceWS → CoinCapWS, `updateLiveCandle()` → `addTick()` |
| `app/src/main/java/com/diellabs/frexa/data/remote/api/BinanceService.java` | Hapus | Tidak dipakai |
| `app/src/main/java/com/diellabs/frexa/data/remote/api/BinanceWebSocketManager.java` | Hapus | Digantikan CoinCapWebSocketManager |
| `app/src/main/java/com/diellabs/frexa/data/remote/model/BinanceKlineEvent.java` | Hapus | Tidak dipakai |

---

## Task 1: Buat CryptoCompareHistominute model

**Files:**
- Create: `app/src/main/java/com/diellabs/frexa/data/remote/model/CryptoCompareHistominute.java`

- [x] **Step 1: Buat file model**

```java
package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CryptoCompareHistominute {

    @SerializedName("Response")
    public String response;

    @SerializedName("Data")
    public DataWrapper data;

    public static class DataWrapper {
        @SerializedName("Data")
        public List<Candle> data;
    }

    public static class Candle {
        @SerializedName("time")  public long time;    // Unix seconds — kalikan 1000 untuk ms
        @SerializedName("open")  public double open;
        @SerializedName("high")  public double high;
        @SerializedName("low")   public double low;
        @SerializedName("close") public double close;
    }
}
```

- [x] **Step 2: Commit**

```
git add app/src/main/java/com/diellabs/frexa/data/remote/model/CryptoCompareHistominute.java
git commit -m "feat(model): add CryptoCompareHistominute OHLCV model"
```

---

## Task 2: Buat CryptoCompareService

**Files:**
- Create: `app/src/main/java/com/diellabs/frexa/data/remote/api/CryptoCompareService.java`

- [x] **Step 1: Buat file Retrofit interface**

```java
package com.diellabs.frexa.data.remote.api;

import com.diellabs.frexa.data.remote.model.CryptoCompareHistominute;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CryptoCompareService {

    @GET("data/v2/histominute")
    Call<CryptoCompareHistominute> getHistoMinute(
        @Query("fsym") String fsym,
        @Query("tsym") String tsym,
        @Query("limit") int limit,
        @Query("aggregate") int aggregate
    );

    @GET("data/v2/histohour")
    Call<CryptoCompareHistominute> getHistoHour(
        @Query("fsym") String fsym,
        @Query("tsym") String tsym,
        @Query("limit") int limit
    );
}
```

- [x] **Step 2: Commit**

```
git add app/src/main/java/com/diellabs/frexa/data/remote/api/CryptoCompareService.java
git commit -m "feat(api): add CryptoCompareService Retrofit interface"
```

---

## Task 3: Buat CoinCapWebSocketManager

**Files:**
- Create: `app/src/main/java/com/diellabs/frexa/data/remote/api/CoinCapWebSocketManager.java`

- [x] **Step 1: Buat file WebSocket manager**

```java
package com.diellabs.frexa.data.remote.api;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import okio.ByteString;
import java.lang.reflect.Type;
import java.util.Map;

public class CoinCapWebSocketManager {

    private static final String TAG = "CoinCapWS";
    private static final String WS_BASE = "wss://ws.coincap.io/prices?assets=";

    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    private final Type priceMapType = new TypeToken<Map<String, String>>(){}.getType();
    private WebSocket webSocket;
    private PriceListener listener;
    private String activeAssetId;
    private volatile boolean connected = false;

    public interface PriceListener {
        void onPrice(String assetId, double price);
        void onError(String message);
    }

    public CoinCapWebSocketManager(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setListener(PriceListener listener) {
        this.listener = listener;
    }

    public void connect(String assetId) {
        activeAssetId = assetId;
        if (connected && webSocket != null) return;
        Request request = new Request.Builder()
                .url(WS_BASE + assetId)
                .build();
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                connected = true;
                Log.d(TAG, "Connected: " + assetId);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                try {
                    Map<String, String> prices = gson.fromJson(text, priceMapType);
                    if (prices != null && prices.containsKey(activeAssetId) && listener != null) {
                        double price = Double.parseDouble(prices.get(activeAssetId));
                        listener.onPrice(activeAssetId, price);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + e.getMessage());
                }
            }

            @Override public void onMessage(WebSocket ws, ByteString bytes) {}

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                connected = false;
                Log.e(TAG, "Failure: " + t.getMessage());
                if (listener != null) listener.onError(t.getMessage());
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                connected = false;
                Log.d(TAG, "Closed: " + reason);
            }
        });
    }

    public void switchAsset(String assetId) {
        if (assetId.equals(activeAssetId) && connected) return;
        disconnect();
        connect(assetId);
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnect");
            webSocket = null;
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
```

- [x] **Step 2: Verifikasi build**

```
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [x] **Step 3: Commit**

```
git add app/src/main/java/com/diellabs/frexa/data/remote/api/CoinCapWebSocketManager.java
git commit -m "feat(websocket): add CoinCapWebSocketManager for live price ticks"
```

---

## Task 4: Update RetrofitClient — ganti Binance → CryptoCompare

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/data/remote/api/RetrofitClient.java`

- [x] **Step 1: Ganti seluruh isi RetrofitClient.java**

```java
package com.diellabs.frexa.data.remote.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String COINGECKO     = "https://api.coingecko.com/api/v3/";
    private static final String GEMINI        = "https://generativelanguage.googleapis.com/";
    private static final String CRYPTOCOMPARE = "https://min-api.cryptocompare.com/";

    private static CoinGeckoService      coinGecko;
    private static GeminiService         gemini;
    private static CryptoCompareService  cryptoCompare;

    private static OkHttpClient client() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder().addInterceptor(log)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
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

    public static CryptoCompareService getCryptoCompareService() {
        if (cryptoCompare == null) cryptoCompare = new Retrofit.Builder()
                .baseUrl(CRYPTOCOMPARE).client(client())
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(CryptoCompareService.class);
        return cryptoCompare;
    }
}
```

- [x] **Step 2: Verifikasi build**

```
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [x] **Step 3: Commit**

```
git add app/src/main/java/com/diellabs/frexa/data/remote/api/RetrofitClient.java
git commit -m "feat(retrofit): replace Binance client with CryptoCompare client"
```

---

## Task 5: Update CoinSymbolMapper — Binance → CryptoCompare

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/data/remote/api/CoinSymbolMapper.java`
- Test: `app/src/test/java/com/diellabs/frexa/CoinSymbolMapperTest.java`

- [x] **Step 1: Tulis test terlebih dulu**

Cek apakah file test sudah ada di `app/src/test/java/com/diellabs/frexa/`. Jika ada `CoinSymbolMapperTest.java`, hapus atau replace seluruh isinya. Jika belum ada, buat file baru.

```java
package com.diellabs.frexa;

import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoinSymbolMapperTest {

    @Test
    public void toCryptoCompareFsym_knownCoin_returnsFsym() {
        assertEquals("BTC",  CoinSymbolMapper.toCryptoCompareFsym("bitcoin"));
        assertEquals("ETH",  CoinSymbolMapper.toCryptoCompareFsym("ethereum"));
        assertEquals("BNB",  CoinSymbolMapper.toCryptoCompareFsym("binancecoin"));
        assertEquals("SOL",  CoinSymbolMapper.toCryptoCompareFsym("solana"));
        assertEquals("DOGE", CoinSymbolMapper.toCryptoCompareFsym("dogecoin"));
    }

    @Test
    public void toCryptoCompareFsym_unknownCoin_returnsFallback() {
        assertEquals("NEWCOIN", CoinSymbolMapper.toCryptoCompareFsym("new-coin"));
    }

    @Test
    public void toAggregate_returnsCorrectValue() {
        assertEquals(1,  CoinSymbolMapper.toAggregate(60));
        assertEquals(5,  CoinSymbolMapper.toAggregate(300));
        assertEquals(15, CoinSymbolMapper.toAggregate(900));
        assertEquals(30, CoinSymbolMapper.toAggregate(1800));
        assertEquals(1,  CoinSymbolMapper.toAggregate(3600));
    }

    @Test
    public void isHourly_returnsTrueOnlyFor3600() {
        assertFalse(CoinSymbolMapper.isHourly(60));
        assertFalse(CoinSymbolMapper.isHourly(1800));
        assertTrue(CoinSymbolMapper.isHourly(3600));
    }
}
```

- [x] **Step 2: Jalankan test — harus FAIL**

```
./gradlew testDebugUnitTest --tests "com.diellabs.frexa.CoinSymbolMapperTest"
```

Expected: FAIL (method tidak ada di CoinSymbolMapper)

- [x] **Step 3: Ganti seluruh isi CoinSymbolMapper.java**

```java
package com.diellabs.frexa.data.remote.api;

import java.util.HashMap;
import java.util.Map;

public final class CoinSymbolMapper {

    private static final Map<String, String> COIN_TO_FSYM = new HashMap<>();

    static {
        COIN_TO_FSYM.put("bitcoin",            "BTC");
        COIN_TO_FSYM.put("ethereum",           "ETH");
        COIN_TO_FSYM.put("binancecoin",        "BNB");
        COIN_TO_FSYM.put("solana",             "SOL");
        COIN_TO_FSYM.put("ripple",             "XRP");
        COIN_TO_FSYM.put("cardano",            "ADA");
        COIN_TO_FSYM.put("dogecoin",           "DOGE");
        COIN_TO_FSYM.put("polkadot",           "DOT");
        COIN_TO_FSYM.put("avalanche-2",        "AVAX");
        COIN_TO_FSYM.put("tron",               "TRX");
        COIN_TO_FSYM.put("chainlink",          "LINK");
        COIN_TO_FSYM.put("polygon",            "MATIC");
        COIN_TO_FSYM.put("litecoin",           "LTC");
        COIN_TO_FSYM.put("uniswap",            "UNI");
        COIN_TO_FSYM.put("near",               "NEAR");
        COIN_TO_FSYM.put("stellar",            "XLM");
        COIN_TO_FSYM.put("cosmos",             "ATOM");
        COIN_TO_FSYM.put("filecoin",           "FIL");
        COIN_TO_FSYM.put("aptos",              "APT");
        COIN_TO_FSYM.put("arbitrum",           "ARB");
        COIN_TO_FSYM.put("optimism",           "OP");
        COIN_TO_FSYM.put("shiba-inu",          "SHIB");
        COIN_TO_FSYM.put("sui",                "SUI");
        COIN_TO_FSYM.put("pepe",               "PEPE");
        COIN_TO_FSYM.put("toncoin",            "TON");
        COIN_TO_FSYM.put("internet-computer",  "ICP");
        COIN_TO_FSYM.put("aave",               "AAVE");
        COIN_TO_FSYM.put("maker",              "MKR");
        COIN_TO_FSYM.put("the-graph",          "GRT");
        COIN_TO_FSYM.put("hedera-hashgraph",   "HBAR");
        COIN_TO_FSYM.put("vechain",            "VET");
        COIN_TO_FSYM.put("algorand",           "ALGO");
        COIN_TO_FSYM.put("quant-network",      "QNT");
        COIN_TO_FSYM.put("fantom",             "FTM");
        COIN_TO_FSYM.put("the-sandbox",        "SAND");
        COIN_TO_FSYM.put("decentraland",       "MANA");
        COIN_TO_FSYM.put("tezos",              "XTZ");
        COIN_TO_FSYM.put("eos",                "EOS");
        COIN_TO_FSYM.put("theta-token",        "THETA");
        COIN_TO_FSYM.put("injective-protocol", "INJ");
        COIN_TO_FSYM.put("render-token",       "RNDR");
        COIN_TO_FSYM.put("fetch-ai",           "FET");
        COIN_TO_FSYM.put("stacks",             "STX");
        COIN_TO_FSYM.put("immutable-x",        "IMX");
        COIN_TO_FSYM.put("worldcoin-wld",      "WLD");
        COIN_TO_FSYM.put("bonk",               "BONK");
        COIN_TO_FSYM.put("celestia",           "TIA");
        COIN_TO_FSYM.put("sei-network",        "SEI");
        COIN_TO_FSYM.put("kaspa",              "KAS");
    }

    public static String toCryptoCompareFsym(String coinId) {
        String fsym = COIN_TO_FSYM.get(coinId);
        if (fsym != null) return fsym;
        return coinId.replace("-", "").toUpperCase();
    }

    /** Returns aggregate value for CryptoCompare histominute. For 1h use isHourly() instead. */
    public static int toAggregate(int seconds) {
        switch (seconds) {
            case 300:  return 5;
            case 900:  return 15;
            case 1800: return 30;
            default:   return 1;
        }
    }

    public static boolean isHourly(int seconds) {
        return seconds >= 3600;
    }
}
```

- [x] **Step 4: Jalankan test — harus PASS**

```
./gradlew testDebugUnitTest --tests "com.diellabs.frexa.CoinSymbolMapperTest"
```

Expected: `BUILD SUCCESSFUL`, semua test PASS

- [x] **Step 5: Commit**

```
git add app/src/main/java/com/diellabs/frexa/data/remote/api/CoinSymbolMapper.java
git add app/src/test/java/com/diellabs/frexa/CoinSymbolMapperTest.java
git commit -m "feat(mapper): replace Binance symbol mapper with CryptoCompare fsym mapper"
```

---

## Task 6: Update CryptoRepository — tambah fetchCryptoCompareKlines

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/data/repository/CryptoRepository.java`

- [x] **Step 1: Edit CryptoRepository.java**

**Perubahan di constructor:** Ganti `binanceApi` → `cryptoCompareApi`

**Bagian field dan constructor:**

Hapus:
```java
private final BinanceService binanceApi;
```
Tambah:
```java
private final CryptoCompareService cryptoCompareApi;
```

Di constructor, hapus:
```java
binanceApi = RetrofitClient.getBinanceService();
```
Tambah:
```java
cryptoCompareApi = RetrofitClient.getCryptoCompareService();
```

**Hapus method `fetchBinanceKlines()`** (seluruh method dari baris `public void fetchBinanceKlines(` sampai kurung tutup `}`).

**Tambah method baru setelah `fetchMarketChart()`:**

```java
public void fetchCryptoCompareKlines(String fsym, int seconds, int limit, OhlcCallback callback) {
    if (CoinSymbolMapper.isHourly(seconds)) {
        cryptoCompareApi.getHistoHour(fsym, "USD", limit)
            .enqueue(new Callback<CryptoCompareHistominute>() {
                @Override
                public void onResponse(Call<CryptoCompareHistominute> c,
                                       Response<CryptoCompareHistominute> r) {
                    if (r.isSuccessful() && r.body() != null
                            && r.body().data != null && r.body().data.data != null) {
                        callback.onResult(toOhlcList(r.body().data.data));
                    }
                }
                @Override
                public void onFailure(Call<CryptoCompareHistominute> c, Throwable t) {}
            });
    } else {
        int aggregate = CoinSymbolMapper.toAggregate(seconds);
        cryptoCompareApi.getHistoMinute(fsym, "USD", limit, aggregate)
            .enqueue(new Callback<CryptoCompareHistominute>() {
                @Override
                public void onResponse(Call<CryptoCompareHistominute> c,
                                       Response<CryptoCompareHistominute> r) {
                    if (r.isSuccessful() && r.body() != null
                            && r.body().data != null && r.body().data.data != null) {
                        callback.onResult(toOhlcList(r.body().data.data));
                    }
                }
                @Override
                public void onFailure(Call<CryptoCompareHistominute> c, Throwable t) {}
            });
    }
}

private List<List<Double>> toOhlcList(List<CryptoCompareHistominute.Candle> candles) {
    List<List<Double>> result = new ArrayList<>();
    for (CryptoCompareHistominute.Candle c : candles) {
        if (c.open > 0 && c.close > 0) {
            result.add(Arrays.asList(c.time * 1000.0, c.open, c.high, c.low, c.close));
        }
    }
    return result;
}
```

**Tambah import yang dibutuhkan** di atas file (jika belum ada):
```java
import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import com.diellabs.frexa.data.remote.api.CryptoCompareService;
import com.diellabs.frexa.data.remote.model.CryptoCompareHistominute;
```

Hapus import yang tidak lagi dibutuhkan:
```java
import com.diellabs.frexa.data.remote.api.BinanceService;
```

- [x] **Step 2: Verifikasi build**

```
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [x] **Step 3: Commit**

```
git add app/src/main/java/com/diellabs/frexa/data/repository/CryptoRepository.java
git commit -m "feat(repo): replace fetchBinanceKlines with fetchCryptoCompareKlines"
```

---

## Task 7: Update CryptoViewModel — wire CoinCap + CryptoCompare

**Files:**
- Modify: `app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java`

- [x] **Step 1: Ganti seluruh isi CryptoViewModel.java**

```java
package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.api.CoinCapWebSocketManager;
import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.data.repository.CryptoRepository;
import com.diellabs.frexa.ui.terminal.LiveCandleBuilder;
import okhttp3.OkHttpClient;
import java.util.List;
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
    private final CoinCapWebSocketManager coinCapWs;
    private String activeCoinId = "bitcoin";
    private String activeFsym = "BTC";

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);

        OkHttpClient wsClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        coinCapWs = new CoinCapWebSocketManager(wsClient);
        coinCapWs.setListener(new CoinCapWebSocketManager.PriceListener() {
            @Override
            public void onPrice(String assetId, double price) {
                candleBuilder.addTick(price, System.currentTimeMillis());
                chartCandles.postValue(candleBuilder.getCandles());
                livePrice.postValue(price);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue("WebSocket error: " + message);
            }
        });
    }

    public void fetchMarkets() { repo.fetchMarkets(coinList, isLoading, errorMessage); }

    public void setActiveCoin(String id) {
        activeCoinId = id;
        activeFsym = CoinSymbolMapper.toCryptoCompareFsym(id);
        int tf = selectedTimeframe.getValue() != null ? selectedTimeframe.getValue() : 60;

        candleBuilder.reset(tf);

        repo.fetchCryptoCompareKlines(activeFsym, tf, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
            coinCapWs.switchAsset(id);
        });
    }

    public void setTimeframe(int seconds) {
        selectedTimeframe.postValue(seconds);
        candleBuilder.reset(seconds);

        repo.fetchCryptoCompareKlines(activeFsym, seconds, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
        });
    }

    public void fetchMarketChart(String id) { repo.fetchMarketChart(id, marketChart); }

    public void startPricePolling() {
        if (!coinCapWs.isConnected()) {
            coinCapWs.connect(activeCoinId);
        }
    }

    public void stopPricePolling() {
        coinCapWs.disconnect();
    }

    @Override protected void onCleared() { coinCapWs.disconnect(); }
}
```

- [x] **Step 2: Verifikasi build**

```
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [x] **Step 3: Commit**

```
git add app/src/main/java/com/diellabs/frexa/viewmodel/CryptoViewModel.java
git commit -m "feat(viewmodel): wire CoinCap WebSocket + CryptoCompare klines, replace Binance"
```

---

## Task 8: Hapus file Binance, verifikasi semua tests

**Files:**
- Delete: `app/src/main/java/com/diellabs/frexa/data/remote/api/BinanceService.java`
- Delete: `app/src/main/java/com/diellabs/frexa/data/remote/api/BinanceWebSocketManager.java`
- Delete: `app/src/main/java/com/diellabs/frexa/data/remote/model/BinanceKlineEvent.java`

- [x] **Step 1: Hapus ketiga file**

```
git rm app/src/main/java/com/diellabs/frexa/data/remote/api/BinanceService.java
git rm app/src/main/java/com/diellabs/frexa/data/remote/api/BinanceWebSocketManager.java
git rm app/src/main/java/com/diellabs/frexa/data/remote/model/BinanceKlineEvent.java
```

- [x] **Step 2: Verifikasi build setelah penghapusan**

```
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

Jika ada compile error `cannot find symbol` untuk class Binance, cari referensi yang tersisa:

```
grep -r "Binance" app/src/main/java --include="*.java"
```

Hapus atau update setiap referensi yang ditemukan.

- [x] **Step 3: Jalankan semua unit tests**

```
./gradlew testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`, semua test PASS (termasuk `LiveCandleBuilderTest` 7 tests dan `CoinSymbolMapperTest` 4 tests)

- [x] **Step 4: Commit final**

```
git commit -m "chore: remove Binance API files, replaced by CryptoCompare + CoinCap"
```

---

## Verifikasi Manual Akhir

Setelah semua task selesai, install app dan buka TerminalFragment:

1. Filter logcat `okhttp.OkHttpClient` — pastikan ada `200 https://min-api.cryptocompare.com/data/v2/histominute` (bukan SSL error)
2. Filter logcat `CoinCapWS` — pastikan muncul `Connected: bitcoin`
3. Pastikan chart menampilkan candle-candle historical
4. Pastikan harga bergerak setiap beberapa detik (live ticks dari CoinCap)
5. Tekan tombol timeframe (5m, 15m) — chart harus reload dengan candle yang lebih lebar
