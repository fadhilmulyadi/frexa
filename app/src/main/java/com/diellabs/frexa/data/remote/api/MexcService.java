package com.diellabs.frexa.data.remote.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MexcService {
    @GET("api/v3/klines")
    Call<List<List<Object>>> getKlines(
        @Query("symbol") String symbol,
        @Query("interval") String interval,
        @Query("limit") int limit
    );
}
