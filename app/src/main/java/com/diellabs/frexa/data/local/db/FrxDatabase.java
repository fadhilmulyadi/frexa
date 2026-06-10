package com.diellabs.frexa.data.local.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.diellabs.frexa.data.local.dao.CachedPriceDao;
import com.diellabs.frexa.data.local.dao.TradeDao;
import com.diellabs.frexa.data.local.entity.CachedPriceEntity;
import com.diellabs.frexa.data.local.entity.TradeEntity;

@Database(entities = {TradeEntity.class, CachedPriceEntity.class}, version = 1, exportSchema = false)
public abstract class FrxDatabase extends RoomDatabase {
    private static volatile FrxDatabase instance;

    public abstract TradeDao tradeDao();
    public abstract CachedPriceDao cachedPriceDao();

    public static FrxDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (FrxDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FrxDatabase.class,
                            "frx_database"
                    ).build();
                }
            }
        }
        return instance;
    }
}
