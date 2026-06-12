package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class CoinDetail {
    public String id, symbol, name;
    public String image;
    @SerializedName("market_cap_rank") public int marketCapRank;

    @SerializedName("market_data")
    public MarketData marketData;

    public static class MarketData {
        @SerializedName("current_price") public Map<String, Double> currentPrice;
        @SerializedName("price_change_percentage_24h") public double priceChange24h;
        @SerializedName("high_24h") public Map<String, Double> high24h;
        @SerializedName("low_24h") public Map<String, Double> low24h;
        @SerializedName("market_cap") public Map<String, Double> marketCap;
        @SerializedName("circulating_supply") public double circulatingSupply;
        @SerializedName("total_supply") public Double totalSupply;
        @SerializedName("total_volume") public Map<String, Double> totalVolume;
        @SerializedName("ath") public Map<String, Double> ath;
        @SerializedName("ath_change_percentage") public Map<String, Double> athChangePercentage;
        @SerializedName("price_change_percentage_7d") public double priceChange7d;
        @SerializedName("price_change_percentage_30d") public double priceChange30d;
        @SerializedName("price_change_percentage_1y") public double priceChange1y;
    }
}
