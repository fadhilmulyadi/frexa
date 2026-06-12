package com.diellabs.frexa.data.remote.api;

import com.diellabs.frexa.data.remote.model.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface CoinGeckoService {
    @GET("coins/markets")
    Call<List<CoinMarket>> getMarkets(
        @Query("vs_currency") String currency,
        @Query("order") String order,
        @Query("per_page") int perPage,
        @Query("page") int page,
        @Query("sparkline") boolean sparkline,
        @Query("price_change_percentage") String priceChangePercentage
    );

    @GET("simple/price")
    Call<Map<String, Map<String, Double>>> getPrices(
        @Query("ids") String ids,
        @Query("vs_currencies") String currencies,
        @Query("include_24hr_change") boolean change
    );

    @GET("coins/{id}")
    Call<CoinDetail> getCoinDetail(
        @Path("id") String coinId,
        @Query("localization") boolean localization,
        @Query("tickers") boolean tickers,
        @Query("community_data") boolean communityData,
        @Query("developer_data") boolean developerData
    );

    @GET("global")
    Call<GlobalData> getGlobalData();
}
