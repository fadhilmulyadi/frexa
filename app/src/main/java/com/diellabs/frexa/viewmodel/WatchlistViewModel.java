package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.diellabs.frexa.data.local.entity.WatchlistEntity;
import com.diellabs.frexa.data.repository.WatchlistRepository;
import java.util.List;

public class WatchlistViewModel extends AndroidViewModel {
    public final LiveData<List<WatchlistEntity>> watchlist;

    private final WatchlistRepository repo;

    public WatchlistViewModel(@NonNull Application app) {
        super(app);
        repo = new WatchlistRepository(app);
        watchlist = repo.getWatchlist();
    }

    public void add(String coinId, String symbol, String name) {
        repo.addToWatchlist(coinId, symbol, name);
    }

    public void remove(String coinId) {
        repo.removeFromWatchlist(coinId);
    }
}
