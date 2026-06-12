package com.diellabs.frexa.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import java.util.List;

@Dao
public interface HoldingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HoldingEntity holding);

    @Update
    void update(HoldingEntity holding);

    @Query("SELECT * FROM holdings ORDER BY totalCostBasis DESC")
    LiveData<List<HoldingEntity>> getAll();

    @Query("SELECT * FROM holdings ORDER BY totalCostBasis DESC")
    List<HoldingEntity> getAllSync();

    @Query("SELECT * FROM holdings WHERE coinId = :coinId LIMIT 1")
    HoldingEntity getByCoinId(String coinId);

    @Query("DELETE FROM holdings WHERE coinId = :coinId")
    void deleteByCoinId(String coinId);

    @Query("DELETE FROM holdings")
    void deleteAll();
}
