package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class PaprikaTicker {
    public String id, name, symbol;
    public int rank;
    @SerializedName("circulating_supply") public double circulatingSupply;
    @SerializedName("total_supply") public double totalSupply;
    @SerializedName("max_supply") public double maxSupply;
    @SerializedName("last_updated") public String lastUpdated;
    public Map<String, Quote> quotes;

    public static class Quote {
        public double price;
        @SerializedName("volume_24h") public double volume24h;
        @SerializedName("market_cap") public double marketCap;
        @SerializedName("percent_change_15m") public double change15m;
        @SerializedName("percent_change_30m") public double change30m;
        @SerializedName("percent_change_1h") public double change1h;
        @SerializedName("percent_change_6h") public double change6h;
        @SerializedName("percent_change_12h") public double change12h;
        @SerializedName("percent_change_24h") public double change24h;
        @SerializedName("percent_change_7d") public double change7d;
        @SerializedName("percent_change_30d") public double change30d;
        @SerializedName("percent_change_1y") public double change1y;
        @SerializedName("ath_price") public double athPrice;
        @SerializedName("ath_date") public String athDate;
        @SerializedName("percent_from_price_ath") public double percentFromAth;
    }
}
