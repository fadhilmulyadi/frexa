package com.diellabs.frexa.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "orders")
public class OrderEntity {
    @PrimaryKey @NonNull
    public String id;
    public String coinId;
    public String symbol;
    public String name;
    public String type;        // "BUY" or "SELL"
    public String orderKind;   // "MARKET", "LIMIT", "STOP_LOSS"
    public double nominalIdr;
    public double quantity;
    public double pricePerCoin;
    public double fee;
    public double totalIdr;
    public long timestamp;
    public double pnlPercent; // for SELL orders: (sellPrice - avgBuyPrice) / avgBuyPrice * 100
}
