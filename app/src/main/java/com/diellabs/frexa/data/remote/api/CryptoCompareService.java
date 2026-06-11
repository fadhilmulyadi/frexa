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
