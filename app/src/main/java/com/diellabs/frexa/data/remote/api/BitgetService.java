package com.diellabs.frexa.data.remote.api;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BitgetService {
    /** 
     * Returns JsonObject containing "code", "msg", "data" (List of string arrays), and "requestTime"
     * Candle format in data: [timestamp, open, high, low, close, baseVol, quoteVol]
     */
    @GET("api/v2/spot/market/candles")
    Call<JsonObject> getCandles(
        @Query("symbol") String symbol,
        @Query("granularity") String granularity,
        @Query("limit") int limit
    );
}
