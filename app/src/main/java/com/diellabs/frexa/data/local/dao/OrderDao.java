package com.diellabs.frexa.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.diellabs.frexa.data.local.entity.OrderEntity;
import java.util.List;

@Dao
public interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OrderEntity order);

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    LiveData<List<OrderEntity>> getAll();

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    List<OrderEntity> getAllSync();

    @Query("SELECT * FROM orders WHERE type = :type ORDER BY timestamp DESC")
    LiveData<List<OrderEntity>> getByType(String type);

    @Query("SELECT * FROM orders ORDER BY timestamp DESC LIMIT :limit")
    List<OrderEntity> getRecent(int limit);

    @Query("SELECT COUNT(*) FROM orders")
    int getTotalOrders();

    @Query("SELECT CASE WHEN COUNT(*) = 0 THEN 0 ELSE (SUM(CASE WHEN pnlPercent > 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) END FROM orders WHERE type = 'SELL'")
    double getWinRate();

    @Query("SELECT COALESCE(MAX(pnlPercent), 0) FROM orders WHERE type = 'SELL'")
    double getBestTradePercent();

    @Query("DELETE FROM orders")
    void deleteAll();
}
