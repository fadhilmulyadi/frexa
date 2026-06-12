package com.diellabs.frexa.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.diellabs.frexa.data.local.dao.HoldingDao;
import com.diellabs.frexa.data.local.dao.OrderDao;
import com.diellabs.frexa.data.local.db.FrxDatabase;
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import com.diellabs.frexa.data.local.entity.OrderEntity;
import com.diellabs.frexa.util.AppExecutors;
import com.diellabs.frexa.util.UserPrefs;
import java.util.List;
import java.util.UUID;

public class TradingRepository {
    private static final double FEE_RATE = 0.0015;

    private final OrderDao orderDao;
    private final HoldingDao holdingDao;
    private final UserPrefs prefs;
    private final AppExecutors exec;

    public TradingRepository(Context ctx) {
        FrxDatabase db = FrxDatabase.getInstance(ctx);
        orderDao = db.orderDao();
        holdingDao = db.holdingDao();
        prefs = new UserPrefs(ctx);
        exec = AppExecutors.getInstance();
    }

    public LiveData<List<OrderEntity>> getOrders() { return orderDao.getAll(); }

    public LiveData<List<OrderEntity>> getOrdersByType(String type) { return orderDao.getByType(type); }

    public void buyCoin(String coinId, String symbol, String name, double nominalIdr, double currentPrice) {
        exec.diskIO().execute(() -> {
            double fee = nominalIdr * FEE_RATE;
            double quantity = nominalIdr / currentPrice;
            double total = nominalIdr + fee;

            float currentBalance = prefs.getBalance();
            if (total > currentBalance + 0.01) return; // insufficient funds

            OrderEntity order = new OrderEntity();
            order.id = UUID.randomUUID().toString();
            order.coinId = coinId; order.symbol = symbol; order.name = name;
            order.type = "BUY"; order.orderKind = "MARKET";
            order.nominalIdr = nominalIdr; order.quantity = quantity;
            order.pricePerCoin = currentPrice; order.fee = fee;
            order.totalIdr = total; order.timestamp = System.currentTimeMillis();
            order.pnlPercent = 0;
            orderDao.insert(order);

            HoldingEntity existing = holdingDao.getByCoinId(coinId);
            if (existing != null) {
                existing.quantity += quantity;
                existing.totalCostBasis += nominalIdr;
                existing.avgBuyPrice = existing.totalCostBasis / existing.quantity;
                holdingDao.update(existing);
            } else {
                HoldingEntity h = new HoldingEntity();
                h.coinId = coinId; h.symbol = symbol; h.name = name;
                h.quantity = quantity; h.avgBuyPrice = currentPrice;
                h.totalCostBasis = nominalIdr;
                holdingDao.insert(h);
            }

            prefs.setBalance(Math.max(0f, currentBalance - (float) total));
        });
    }

    public void sellCoin(String coinId, String symbol, String name, double quantity, double currentPrice) {
        exec.diskIO().execute(() -> {
            HoldingEntity existing = holdingDao.getByCoinId(coinId);
            if (existing == null || existing.quantity < quantity - 1e-10) return; // insufficient coins

            double nominalIdr = quantity * currentPrice;
            double fee = nominalIdr * FEE_RATE;
            double received = nominalIdr - fee;

            double pnlPercent = existing.avgBuyPrice > 0
                ? (currentPrice - existing.avgBuyPrice) / existing.avgBuyPrice * 100
                : 0;

            OrderEntity order = new OrderEntity();
            order.id = UUID.randomUUID().toString();
            order.coinId = coinId; order.symbol = symbol; order.name = name;
            order.type = "SELL"; order.orderKind = "MARKET";
            order.nominalIdr = nominalIdr; order.quantity = quantity;
            order.pricePerCoin = currentPrice; order.fee = fee;
            order.totalIdr = received; order.timestamp = System.currentTimeMillis();
            order.pnlPercent = pnlPercent;
            orderDao.insert(order);

            double soldCost = existing.avgBuyPrice * quantity;
            existing.quantity -= quantity;
            existing.totalCostBasis -= soldCost;
            if (existing.quantity <= 0.00000001) {
                holdingDao.deleteByCoinId(coinId);
            } else {
                holdingDao.update(existing);
            }

            prefs.setBalance(Math.max(0f, prefs.getBalance() + (float) received));
        });
    }

    public List<OrderEntity> getRecentOrders(int limit) {
        return orderDao.getRecent(limit);
    }

    public void getStats(StatCallback cb) {
        exec.diskIO().execute(() -> {
            int total = orderDao.getTotalOrders();
            double winRate = orderDao.getWinRate();
            double bestTrade = orderDao.getBestTradePercent();
            AppExecutors.getInstance().mainThread().execute(() -> cb.onResult(total, winRate, bestTrade));
        });
    }

    public interface StatCallback {
        void onResult(int totalOrders, double winRate, double bestTradePercent);
    }
}
