package com.diellabs.frexa.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "trades")
public class TradeEntity {
    @PrimaryKey @NonNull
    public String id;
    public String coinId;
    public String coinName;
    public String coinSymbol;
    public String coinImageUrl;
    public String direction;   // "UP" or "DOWN"
    public double stakeAmount;
    public int profitPercent;
    public double entryPrice;
    public double exitPrice;
    public String durationLabel;
    public int durationSeconds;
    public long openTime;
    public long closeTime;
    public boolean isWin;
    public double pnl;
    public String status;      // "OPEN" or "CLOSED"
}
