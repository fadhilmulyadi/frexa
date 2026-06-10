package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class CoinMarket {
    public String id, symbol, name, image;
    @SerializedName("current_price") public double currentPrice;
    @SerializedName("market_cap_rank") public int marketCapRank;
    @SerializedName("price_change_percentage_24h") public double priceChangePercentage24h;
    public transient double profitPercent;

    public String getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public double getCurrentPrice() { return currentPrice; }
    public double getPriceChangePercentage24h() { return priceChangePercentage24h; }
    public double getProfitPercent() {
        if (profitPercent > 0) return profitPercent;
        if (marketCapRank <= 10) return 90;
        if (marketCapRank <= 30) return 80;
        return 75;
    }
}
