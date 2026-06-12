package com.diellabs.frexa.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.data.repository.TradingRepository;
import com.diellabs.frexa.util.UserPrefs;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingViewModel extends AndroidViewModel {
    public final LiveData<List<TradeEntity>> openTrades;
    public final LiveData<List<TradeEntity>> closedTrades;
    public final MutableLiveData<Float> virtualBalance = new MutableLiveData<>();

    private final TradingRepository repo;
    private final UserPrefs prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, Runnable> pendingExpiry = new HashMap<>();

    public TradingViewModel(@NonNull Application app) {
        super(app);
        repo = new TradingRepository(app);
        prefs = new UserPrefs(app);
        openTrades = repo.getOpenTrades();
        closedTrades = repo.getClosedTrades();
        virtualBalance.setValue(prefs.getBalance());
    }

    public TradeEntity placeTrade(String coinId, String coinName, String coinSymbol,
                           String imgUrl, String direction, double stake,
                           int profitPct, double entryPrice, int durSec, String durLabel) {
        TradeEntity t = repo.placeTrade(coinId, coinName, coinSymbol, imgUrl, direction,
                        stake, profitPct, entryPrice, durSec, durLabel);
        virtualBalance.postValue(prefs.getBalance());
        return t;
    }

    public void closeTrade(TradeEntity trade, double exitPrice) {
        repo.closeTrade(trade, exitPrice);
        virtualBalance.postValue(prefs.getBalance());
    }

    public void earlyClose(TradeEntity trade, double exitPrice) {
        repo.earlyClose(trade, exitPrice);
        virtualBalance.postValue(prefs.getBalance());
    }

    public interface PriceCallback { void onExpired(double exitPrice); }

    public void scheduleExpiry(TradeEntity trade, PriceCallback cb) {
        long delay = Math.max(0, trade.closeTime - System.currentTimeMillis());
        if (pendingExpiry.containsKey(trade.id)) return;
        Runnable task = () -> {
            cb.onExpired(0);
            pendingExpiry.remove(trade.id);
        };
        pendingExpiry.put(trade.id, task);
        handler.postDelayed(task, delay);
    }

    public void cancelExpiry(String tradeId) {
        Runnable r = pendingExpiry.remove(tradeId);
        if (r != null) handler.removeCallbacks(r);
    }

    public void loadStats(TradingRepository.StatCallback cb) { repo.getStats(cb); }

    public void refreshBalance() { virtualBalance.postValue(prefs.getBalance()); }

    @Override protected void onCleared() {
        for (Runnable r : pendingExpiry.values()) handler.removeCallbacks(r);
        pendingExpiry.clear();
    }
}
