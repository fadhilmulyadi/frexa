package com.diellabs.frexa.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.diellabs.frexa.data.local.dao.WatchlistDao;
import com.diellabs.frexa.data.local.db.FrxDatabase;
import com.diellabs.frexa.data.local.entity.WatchlistEntity;
import com.diellabs.frexa.util.AppExecutors;
import java.util.List;

public class WatchlistRepository {
    private final WatchlistDao dao;
    private final AppExecutors exec;

    public WatchlistRepository(Context ctx) {
        dao = FrxDatabase.getInstance(ctx).watchlistDao();
        exec = AppExecutors.getInstance();
    }

    public LiveData<List<WatchlistEntity>> getWatchlist() { return dao.getAll(); }

    public void addToWatchlist(String coinId, String symbol, String name) {
        exec.diskIO().execute(() -> {
            WatchlistEntity e = new WatchlistEntity();
            e.coinId = coinId; e.symbol = symbol; e.name = name;
            e.addedAt = System.currentTimeMillis();
            dao.insert(e);
        });
    }

    public void removeFromWatchlist(String coinId) {
        exec.diskIO().execute(() -> dao.delete(coinId));
    }

    public boolean isInWatchlist(String coinId) {
        return dao.isWatchlisted(coinId) > 0;
    }
}
