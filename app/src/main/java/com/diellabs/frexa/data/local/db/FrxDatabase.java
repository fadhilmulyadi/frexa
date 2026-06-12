package com.diellabs.frexa.data.local.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.diellabs.frexa.data.local.dao.CachedPriceDao;
import com.diellabs.frexa.data.local.dao.HoldingDao;
import com.diellabs.frexa.data.local.dao.OrderDao;
import com.diellabs.frexa.data.local.dao.WatchlistDao;
import com.diellabs.frexa.data.local.entity.CachedPriceEntity;
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import com.diellabs.frexa.data.local.entity.OrderEntity;
import com.diellabs.frexa.data.local.entity.WatchlistEntity;

@Database(entities = {HoldingEntity.class, OrderEntity.class, WatchlistEntity.class, CachedPriceEntity.class}, version = 3, exportSchema = false)
public abstract class FrxDatabase extends RoomDatabase {
    private static volatile FrxDatabase instance;

    public abstract HoldingDao holdingDao();
    public abstract OrderDao orderDao();
    public abstract WatchlistDao watchlistDao();
    public abstract CachedPriceDao cachedPriceDao();

    public static FrxDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (FrxDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FrxDatabase.class,
                            "frx_database"
                    ).fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }
}
