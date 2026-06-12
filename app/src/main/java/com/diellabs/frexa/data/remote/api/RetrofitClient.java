package com.diellabs.frexa.data.remote.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String COINGECKO    = "https://api.coingecko.com/api/v3/";
    private static final String COINPAPRIKA  = "https://api.coinpaprika.com/v1/";
    private static final String FEAR_GREED   = "https://api.alternative.me/";

    private static CoinGeckoService  coinGecko;
    private static CoinPaprikaService coinPaprika;
    private static FearGreedService  fearGreed;

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

    public static CoinPaprikaService getCoinPaprikaService() {
        if (coinPaprika == null) coinPaprika = new Retrofit.Builder()
                .baseUrl(COINPAPRIKA).client(client())
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(CoinPaprikaService.class);
        return coinPaprika;
    }

    public static FearGreedService getFearGreedService() {
        if (fearGreed == null) fearGreed = new Retrofit.Builder()
                .baseUrl(FEAR_GREED).client(client())
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(FearGreedService.class);
        return fearGreed;
    }
}
