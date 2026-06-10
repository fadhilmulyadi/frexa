package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class CoinMarket {
    public String id, symbol, name, image;
    @SerializedName("current_price") public double currentPrice;
    @SerializedName("market_cap_rank") public int marketCapRank;
    @SerializedName("price_change_percentage_24h") public double priceChangePercentage24h;
}
