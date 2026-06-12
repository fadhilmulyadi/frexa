package com.diellabs.frexa.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "watchlist")
public class WatchlistEntity {
    @PrimaryKey @NonNull
    public String coinId;
    public String symbol;
    public String name;
    public long addedAt;
}
