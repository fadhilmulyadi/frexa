package com.diellabs.frexa.data.remote.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String COINGECKO = "https://api.coingecko.com/api/v3/";
    private static final String GEMINI = "https://generativelanguage.googleapis.com/";
    private static CoinGeckoService coinGecko;
    private static GeminiService gemini;

    private static OkHttpClient client() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder().addInterceptor(log)
                .connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
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
}
