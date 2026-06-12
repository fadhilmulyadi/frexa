package com.diellabs.frexa.data.remote.api;

import com.diellabs.frexa.data.remote.model.FearGreedData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FearGreedService {
    @GET("fng/")
    Call<FearGreedData> getFearGreed(@Query("limit") int limit);
}
