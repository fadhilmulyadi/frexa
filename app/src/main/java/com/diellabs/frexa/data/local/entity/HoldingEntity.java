package com.diellabs.frexa.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "holdings")
public class HoldingEntity {
    @PrimaryKey @NonNull
    public String coinId;
    public String symbol;
    public String name;
    public double quantity;
    public double avgBuyPrice;
    public double totalCostBasis;
}
