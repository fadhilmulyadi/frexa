package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class GlobalData {
    @SerializedName("data")
    public InnerData data;

    public static class InnerData {
        @SerializedName("total_market_cap") public Map<String, Double> totalMarketCap;
        @SerializedName("total_volume") public Map<String, Double> totalVolume;
        @SerializedName("market_cap_percentage") public Map<String, Double> marketCapPercentage;
        @SerializedName("market_cap_change_percentage_24h_usd") public double marketCapChange24h;
    }
}
