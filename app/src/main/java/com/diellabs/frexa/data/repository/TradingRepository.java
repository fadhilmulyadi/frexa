package com.diellabs.frexa.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.diellabs.frexa.data.local.dao.TradeDao;
import com.diellabs.frexa.data.local.db.FrxDatabase;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.util.AppExecutors;
import com.diellabs.frexa.util.UserPrefs;
import java.util.List;
import java.util.UUID;

public class TradingRepository {
    private final TradeDao dao;
    private final UserPrefs prefs;
    private final AppExecutors exec;

    public TradingRepository(Context ctx) {
        dao = FrxDatabase.getInstance(ctx).tradeDao();
        prefs = new UserPrefs(ctx);
        exec = AppExecutors.getInstance();
    }

    public LiveData<List<TradeEntity>> getOpenTrades() { return dao.getOpenTrades(); }
    public LiveData<List<TradeEntity>> getClosedTrades() { return dao.getClosedTrades(); }

    public void placeTrade(String coinId, String coinName, String coinSymbol,
                           String coinImageUrl, String direction, double stake,
                           int profitPercent, double entryPrice,
                           int durationSec, String durationLabel) {
        TradeEntity t = new TradeEntity();
        t.id = UUID.randomUUID().toString();
        t.coinId = coinId; t.coinName = coinName; t.coinSymbol = coinSymbol;
        t.coinImageUrl = coinImageUrl; t.direction = direction;
        t.stakeAmount = stake; t.profitPercent = profitPercent;
        t.entryPrice = entryPrice; t.durationSeconds = durationSec;
        t.durationLabel = durationLabel;
        t.openTime = System.currentTimeMillis();
        t.closeTime = t.openTime + (durationSec * 1000L);
        t.status = "OPEN";
        prefs.setBalance(prefs.getBalance() - (float) stake);
        exec.diskIO().execute(() -> dao.insert(t));
    }

    public void closeTrade(TradeEntity t, double exitPrice) {
        t.exitPrice = exitPrice; t.status = "CLOSED";
        t.isWin = t.direction.equals("UP") ? exitPrice > t.entryPrice : exitPrice < t.entryPrice;
        t.pnl = t.isWin ? t.stakeAmount * (t.profitPercent / 100.0) : -t.stakeAmount;
        float delta = t.isWin ? (float)(t.stakeAmount + t.pnl) : 0;
        prefs.setBalance(prefs.getBalance() + delta);
        exec.diskIO().execute(() -> dao.update(t));
    }

    public void getStats(StatCallback cb) {
        exec.diskIO().execute(() -> {
            int total = dao.getTotalTrades(), wins = dao.getWinCount();
            double best = dao.getBestProfit();
            float wr = total > 0 ? (wins * 100f / total) : 0;
            AppExecutors.getInstance().mainThread().execute(() -> cb.onResult(total, wr, best));
        });
    }

    public interface StatCallback { void onResult(int total, float winRate, double best); }
}
