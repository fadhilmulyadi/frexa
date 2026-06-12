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

    /**
     * Bitfinex Ticker returns: [BID, BID_SIZE, ASK, ASK_SIZE, DAILY_CHANGE, ...]
     * Last price is usually at index 6 or 7.
     */
    @GET("bitfinex/ticker/{symbol}")
    Call<List<Double>> getTicker(
        @Path("symbol") String symbol
    );
}
