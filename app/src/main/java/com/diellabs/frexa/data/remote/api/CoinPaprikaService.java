package com.diellabs.frexa.data.remote.api;

import com.diellabs.frexa.data.remote.model.PaprikaTicker;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface CoinPaprikaService {
    @GET("tickers")
    Call<List<PaprikaTicker>> getTickers(@Query("quotes") String quotes);

    @GET("tickers/{id}")
    Call<PaprikaTicker> getTickerDetail(@Path("id") String coinId, @Query("quotes") String quotes);
}
