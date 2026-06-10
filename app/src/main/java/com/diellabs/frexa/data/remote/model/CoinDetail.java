package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class CoinDetail {
    public String id, symbol, name;
    public ImageUrls image;
    @SerializedName("market_cap_rank") public int marketCapRank;
    @SerializedName("market_data") public MarketData marketData;

    public static class ImageUrls { public String large; }

    public static class MarketData {
        @SerializedName("current_price") public Map<String, Double> currentPrice;
        @SerializedName("price_change_percentage_24h") public double priceChange24h;
    }
}
