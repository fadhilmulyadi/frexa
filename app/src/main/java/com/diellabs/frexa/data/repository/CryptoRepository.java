package com.diellabs.frexa.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.local.dao.CachedPriceDao;
import com.diellabs.frexa.data.local.db.FrxDatabase;
import com.diellabs.frexa.data.local.entity.CachedPriceEntity;
import com.diellabs.frexa.data.remote.api.BitfinexService;
import com.diellabs.frexa.data.remote.api.CoinGeckoService;
import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import com.diellabs.frexa.data.remote.api.RetrofitClient;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.util.AppExecutors;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CryptoRepository {
    private final CoinGeckoService api;
    private final BitfinexService bitfinexApi;
    private final CachedPriceDao cacheDao;
    private final AppExecutors exec;

    @FunctionalInterface
    public interface OhlcCallback {
        void onResult(List<List<Double>> data);
    }

    /** Callback fires on OkHttp background thread — use postValue, not setValue. */
    @FunctionalInterface
    public interface PriceCallback {
        void onPrice(double price);
    }

    public CryptoRepository(Context ctx) {
        api = RetrofitClient.getCoinGeckoService();
        bitfinexApi = RetrofitClient.getBitfinexService();
        cacheDao = FrxDatabase.getInstance(ctx).cachedPriceDao();
        exec = AppExecutors.getInstance();
    }

    public void fetchMarkets(MutableLiveData<List<CoinMarket>> live,
                             MutableLiveData<Boolean> loading,
                             MutableLiveData<String> error) {
        loading.postValue(true);
        api.getMarkets("usd", "market_cap_desc", 50, 1, false)
           .enqueue(new Callback<List<CoinMarket>>() {
               @Override public void onResponse(Call<List<CoinMarket>> c, Response<List<CoinMarket>> r) {
                   loading.postValue(false);
                   if (r.isSuccessful() && r.body() != null) {
                       live.postValue(r.body());
                       exec.diskIO().execute(() -> saveToCache(r.body()));
                   } else { error.postValue("Gagal memuat"); loadFromCache(live); }
               }
               @Override public void onFailure(Call<List<CoinMarket>> c, Throwable t) {
                   loading.postValue(false);
                   error.postValue("Tidak ada koneksi");
                   loadFromCache(live);
               }
           });
    }

    public void fetchLivePrice(String coinId, MutableLiveData<Double> price,
                               MutableLiveData<String> error) {
        api.getPrices(coinId, "usd", false).enqueue(new Callback<Map<String, Map<String, Double>>>() {
            @Override public void onResponse(Call<Map<String, Map<String, Double>>> c,
                                             Response<Map<String, Map<String, Double>>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    Map<String, Double> inner = r.body().get(coinId);
                    if (inner != null) price.postValue(inner.get("usd"));
                }
            }
            @Override public void onFailure(Call<Map<String, Map<String, Double>>> c, Throwable t) {
                error.postValue("Tidak ada koneksi");
            }
        });
    }

    public void fetchOhlc(String coinId, MutableLiveData<List<List<Double>>> ohlc) {
        api.getOhlc(coinId, "usd", 1).enqueue(new Callback<List<List<Double>>>() {
            @Override public void onResponse(Call<List<List<Double>>> c, Response<List<List<Double>>> r) {
                if (r.isSuccessful() && r.body() != null) ohlc.postValue(r.body());
            }
            @Override public void onFailure(Call<List<List<Double>>> c, Throwable t) {}
        });
    }

    public void fetchOhlc(String coinId, OhlcCallback callback) {
        api.getOhlc(coinId, "usd", 1).enqueue(new Callback<List<List<Double>>>() {
            @Override public void onResponse(Call<List<List<Double>>> c,
                                             Response<List<List<Double>>> r) {
                if (r.isSuccessful() && r.body() != null) callback.onResult(r.body());
            }
            @Override public void onFailure(Call<List<List<Double>>> c, Throwable t) {
                // Silent — consistent with existing fetchOhlc behavior; caller handles stale/empty chart
            }
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

    public void fetchMarketChart(String coinId, MutableLiveData<MarketChart> chart) {
        api.getMarketChart(coinId, "usd", 1).enqueue(new Callback<MarketChart>() {
            @Override public void onResponse(Call<MarketChart> c, Response<MarketChart> r) {
                if (r.isSuccessful() && r.body() != null) chart.postValue(r.body());
            }
            @Override public void onFailure(Call<MarketChart> c, Throwable t) {}
        });
    }

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

    private void saveToCache(List<CoinMarket> coins) {
        List<CachedPriceEntity> entities = new ArrayList<>();
        for (CoinMarket c : coins) {
            CachedPriceEntity e = new CachedPriceEntity();
            e.coinId = c.id; e.symbol = c.symbol; e.name = c.name; e.imageUrl = c.image;
            e.currentPrice = c.currentPrice; e.priceChangePercent24h = c.priceChangePercentage24h;
            e.marketCapRank = c.marketCapRank;
            e.profitPercent = c.marketCapRank <= 10 ? 90 : c.marketCapRank <= 30 ? 80 : 75;
            e.lastUpdated = System.currentTimeMillis();
            entities.add(e);
        }
        cacheDao.deleteAll(); cacheDao.insertAll(entities);
    }

    private void loadFromCache(MutableLiveData<List<CoinMarket>> live) {
        exec.diskIO().execute(() -> {
            List<CachedPriceEntity> cached = cacheDao.getAllSync();
            List<CoinMarket> result = new ArrayList<>();
            for (CachedPriceEntity e : cached) {
                CoinMarket m = new CoinMarket();
                m.id = e.coinId; m.symbol = e.symbol; m.name = e.name;
                m.image = e.imageUrl; m.currentPrice = e.currentPrice;
                m.priceChangePercentage24h = e.priceChangePercent24h;
                m.marketCapRank = e.marketCapRank;
                result.add(m);
            }
            live.postValue(result);
        });
    }
}
