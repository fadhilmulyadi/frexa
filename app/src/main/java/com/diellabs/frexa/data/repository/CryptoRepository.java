package com.diellabs.frexa.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.local.dao.CachedPriceDao;
import com.diellabs.frexa.data.local.db.FrxDatabase;
import com.diellabs.frexa.data.local.entity.CachedPriceEntity;
import com.diellabs.frexa.data.remote.api.CoinGeckoService;
import com.diellabs.frexa.data.remote.api.CoinPaprikaService;
import com.diellabs.frexa.data.remote.api.FearGreedService;
import com.diellabs.frexa.data.remote.api.RetrofitClient;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.util.AppExecutors;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Locale;

public class CryptoRepository {
    private final CoinGeckoService api;
    private final CoinPaprikaService paprikaApi;
    private final FearGreedService fearGreedApi;
    private final CachedPriceDao cacheDao;
    private final AppExecutors exec;

    @FunctionalInterface
    public interface PriceCallback { void onPrice(double price); }

    public CryptoRepository(Context ctx) {
        api = RetrofitClient.getCoinGeckoService();
        paprikaApi = RetrofitClient.getCoinPaprikaService();
        fearGreedApi = RetrofitClient.getFearGreedService();
        cacheDao = FrxDatabase.getInstance(ctx).cachedPriceDao();
        exec = AppExecutors.getInstance();
    }

    public void fetchMarkets(MutableLiveData<List<CoinMarket>> live,
                             MutableLiveData<Boolean> loading,
                             MutableLiveData<String> error) {
        loading.postValue(true);
        paprikaApi.getTickers("IDR")
           .enqueue(new Callback<List<PaprikaTicker>>() {
               @Override public void onResponse(Call<List<PaprikaTicker>> c, Response<List<PaprikaTicker>> r) {
                   loading.postValue(false);
                   if (r.isSuccessful() && r.body() != null) {
                       List<CoinMarket> markets = new ArrayList<>();
                       int count = 0;
                       for (PaprikaTicker t : r.body()) {
                           if (count >= 50) break;
                           markets.add(mapToMarket(t));
                           count++;
                       }
                       live.postValue(markets);
                       exec.diskIO().execute(() -> saveToCache(markets));
                   } else { error.postValue("Gagal memuat"); loadFromCache(live); }
               }
               @Override public void onFailure(Call<List<PaprikaTicker>> c, Throwable t) {
                   loading.postValue(false);
                   error.postValue("Tidak ada koneksi");
                   loadFromCache(live);
               }
           });
    }

    public void fetchLivePrice(String coinId, PriceCallback callback) {
        paprikaApi.getTickerDetail(coinId, "IDR")
           .enqueue(new Callback<PaprikaTicker>() {
               @Override public void onResponse(Call<PaprikaTicker> c, Response<PaprikaTicker> r) {
                   if (r.isSuccessful() && r.body() != null && r.body().quotes != null) {
                       PaprikaTicker.Quote q = r.body().quotes.get("IDR");
                       if (q != null) callback.onPrice(q.price);
                   }
               }
               @Override public void onFailure(Call<PaprikaTicker> c, Throwable t) {}
           });
    }

    private static final Map<String, CoinDetail> detailCache = new HashMap<>();

    public void fetchCoinDetail(String coinId, MutableLiveData<CoinDetail> detail,
                                MutableLiveData<String> error) {
        if (detailCache.containsKey(coinId)) {
            detail.postValue(detailCache.get(coinId));
        }

        paprikaApi.getTickerDetail(coinId, "IDR")
           .enqueue(new Callback<PaprikaTicker>() {
               @Override public void onResponse(Call<PaprikaTicker> c, Response<PaprikaTicker> r) {
                   if (r.isSuccessful() && r.body() != null) {
                       CoinDetail d = mapToDetail(r.body());
                       detailCache.put(coinId, d);
                       detail.postValue(d);
                   } else if (r.code() == 429) {
                       error.postValue("Limit API terlampaui");
                   } else {
                       error.postValue("Gagal memuat detail");
                   }
               }
               @Override public void onFailure(Call<PaprikaTicker> c, Throwable t) {
                   error.postValue("Tidak ada koneksi");
               }
           });
    }

    private CoinMarket mapToMarket(PaprikaTicker t) {
        CoinMarket m = new CoinMarket();
        m.id = t.id; m.symbol = t.symbol; m.name = t.name;
        m.image = "https://static.coinpaprika.com/coin/" + t.id + "/logo.png";
        if (t.quotes != null && t.quotes.get("IDR") != null) {
            PaprikaTicker.Quote q = t.quotes.get("IDR");
            m.currentPrice = q.price;
            m.marketCap = q.marketCap;
            m.marketCapRank = t.rank;
            m.totalVolume = q.volume24h;
            m.priceChangePercentage24h = q.change24h;
        }
        return m;
    }

    private CoinDetail mapToDetail(PaprikaTicker t) {
        CoinDetail d = new CoinDetail();
        d.id = t.id; d.symbol = t.symbol; d.name = t.name;
        d.marketCapRank = t.rank;
        d.image = "https://static.coinpaprika.com/coin/" + t.id + "/logo.png";
        
        d.marketData = new CoinDetail.MarketData();
        if (t.quotes != null && t.quotes.get("IDR") != null) {
            PaprikaTicker.Quote q = t.quotes.get("IDR");
            d.marketData.currentPrice = Collections.singletonMap("idr", q.price);
            d.marketData.marketCap = Collections.singletonMap("idr", q.marketCap);
            d.marketData.circulatingSupply = t.circulatingSupply;
            d.marketData.ath = Collections.singletonMap("idr", q.athPrice);
            d.marketData.priceChange24h = q.change24h;
            d.marketData.priceChange7d = q.change7d;
            d.marketData.priceChange30d = q.change30d;
            d.marketData.priceChange1y = q.change1y;
            d.marketData.athChangePercentage = Collections.singletonMap("idr", q.percentFromAth);
            d.marketData.totalVolume = Collections.singletonMap("idr", q.volume24h);
        }
        return d;
    }

    public void fetchGlobalData(MutableLiveData<GlobalData> global,
                                MutableLiveData<String> error) {
        api.getGlobalData().enqueue(new Callback<GlobalData>() {
            @Override public void onResponse(Call<GlobalData> c, Response<GlobalData> r) {
                if (r.isSuccessful() && r.body() != null) global.postValue(r.body());
            }
            @Override public void onFailure(Call<GlobalData> c, Throwable t) {
                error.postValue("Gagal memuat data global");
            }
        });
    }

    public void fetchFearGreed(MutableLiveData<Integer> fearGreedValue, MutableLiveData<String> fearGreedLabel) {
        fearGreedApi.getFearGreed(1).enqueue(new Callback<FearGreedData>() {
            @Override public void onResponse(Call<FearGreedData> c, Response<FearGreedData> r) {
                if (r.isSuccessful() && r.body() != null && r.body().data != null && !r.body().data.isEmpty()) {
                    FearGreedData.Entry entry = r.body().data.get(0);
                    try {
                        fearGreedValue.postValue(Integer.parseInt(entry.value));
                        fearGreedLabel.postValue(translateLabel(entry.valueClassification));
                    } catch (NumberFormatException ignored) {}
                }
            }
            @Override public void onFailure(Call<FearGreedData> c, Throwable t) {}
        });
    }

    private String translateLabel(String label) {
        if (label == null) return "";
        switch (label) {
            case "Extreme Fear": return "Panik Ekstrem";
            case "Fear": return "Takut";
            case "Neutral": return "Netral";
            case "Greed": return "Serakah";
            case "Extreme Greed": return "Serakah Ekstrem";
            default: return label;
        }
    }

    private void saveToCache(List<CoinMarket> coins) {
        List<CachedPriceEntity> entities = new ArrayList<>();
        for (CoinMarket c : coins) {
            CachedPriceEntity e = new CachedPriceEntity();
            e.coinId = c.id; e.symbol = c.symbol; e.name = c.name; e.imageUrl = c.image;
            e.currentPrice = c.currentPrice; e.priceChangePercent24h = c.priceChangePercentage24h;
            e.marketCapRank = c.marketCapRank;
            e.high24h = c.high24h; e.low24h = c.low24h;
            e.marketCap = c.marketCap; e.volume24h = c.totalVolume;
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
                m.high24h = e.high24h; m.low24h = e.low24h;
                m.marketCap = e.marketCap; m.totalVolume = e.volume24h;
                result.add(m);
            }
            live.postValue(result);
        });
    }
}
