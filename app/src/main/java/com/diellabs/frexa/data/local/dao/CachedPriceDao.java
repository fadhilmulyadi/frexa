package com.diellabs.frexa.data.local.dao;

import androidx.room.*;
import com.diellabs.frexa.data.local.entity.CachedPriceEntity;
import java.util.List;

@Dao
public interface CachedPriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CachedPriceEntity> prices);

    @Query("SELECT * FROM cached_prices ORDER BY marketCapRank ASC")
    List<CachedPriceEntity> getAllSync();

    @Query("SELECT * FROM cached_prices WHERE coinId = :coinId LIMIT 1")
    CachedPriceEntity getById(String coinId);

    @Query("DELETE FROM cached_prices")
    void deleteAll();
}
