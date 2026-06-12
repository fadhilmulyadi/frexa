package com.diellabs.frexa.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.diellabs.frexa.data.local.entity.WatchlistEntity;
import java.util.List;

@Dao
public interface WatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WatchlistEntity item);

    @Query("DELETE FROM watchlist WHERE coinId = :coinId")
    void delete(String coinId);

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    LiveData<List<WatchlistEntity>> getAll();

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    List<WatchlistEntity> getAllSync();

    @Query("SELECT COUNT(*) FROM watchlist WHERE coinId = :coinId")
    int isWatchlisted(String coinId);

    @Query("DELETE FROM watchlist")
    void deleteAll();
}
