package com.diellabs.frexa.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "cached_prices")
public class CachedPriceEntity {
    @PrimaryKey @NonNull
    public String coinId;
    public String symbol;
    public String name;
    public String imageUrl;
    public double currentPrice;
    public double priceChangePercent24h;
    public int marketCapRank;
    public double high24h;
    public double low24h;
    public double marketCap;
    public double volume24h;
    public long lastUpdated;
}
