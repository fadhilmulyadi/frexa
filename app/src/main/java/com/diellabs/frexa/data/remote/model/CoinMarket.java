package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class CoinMarket {
    public String id, symbol, name, image;
    @SerializedName("current_price") public double currentPrice;
    @SerializedName("market_cap_rank") public int marketCapRank;
    @SerializedName("price_change_percentage_24h") public double priceChangePercentage24h;
    @SerializedName("high_24h") public double high24h;
    @SerializedName("low_24h") public double low24h;
    @SerializedName("market_cap") public double marketCap;
    @SerializedName("total_volume") public double totalVolume;

    public String getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public double getCurrentPrice() { return currentPrice; }
    public double getPriceChangePercentage24h() { return priceChangePercentage24h; }
    public double getHigh24h() { return high24h; }
    public double getLow24h() { return low24h; }
    public double getMarketCap() { return marketCap; }
    public double getTotalVolume() { return totalVolume; }
}
