package com.diellabs.frexa.data.remote.api;

import com.diellabs.frexa.data.remote.model.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface GeminiService {
    @POST("v1beta/models/gemini-pro:generateContent")
    Call<GeminiResponse> generateContent(
        @Query("key") String apiKey,
        @Body GeminiRequest request
    );
}
