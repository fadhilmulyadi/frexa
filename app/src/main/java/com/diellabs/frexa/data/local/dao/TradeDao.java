package com.diellabs.frexa.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import java.util.List;

@Dao
public interface TradeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TradeEntity trade);

    @Update
    void update(TradeEntity trade);

    @Query("SELECT * FROM trades WHERE status = 'OPEN' ORDER BY openTime DESC")
    LiveData<List<TradeEntity>> getOpenTrades();

    @Query("SELECT * FROM trades WHERE status = 'CLOSED' ORDER BY closeTime DESC")
    LiveData<List<TradeEntity>> getClosedTrades();

    @Query("SELECT * FROM trades WHERE id = :id LIMIT 1")
    TradeEntity getTradeById(String id);

    @Query("SELECT * FROM trades WHERE status = 'OPEN'")
    List<TradeEntity> getOpenTradesSync();

    @Query("SELECT COUNT(*) FROM trades WHERE status = 'CLOSED'")
    int getTotalTrades();

    @Query("SELECT COUNT(*) FROM trades WHERE status = 'CLOSED' AND isWin = 1")
    int getWinCount();

    @Query("SELECT MAX(pnl) FROM trades WHERE status = 'CLOSED' AND isWin = 1")
    double getBestProfit();
}
