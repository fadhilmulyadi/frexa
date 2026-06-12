package com.diellabs.frexa.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.diellabs.frexa.data.local.dao.HoldingDao;
import com.diellabs.frexa.data.local.db.FrxDatabase;
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import com.diellabs.frexa.util.UserPrefs;
import java.util.List;

public class PortfolioRepository {
    private final HoldingDao holdingDao;
    private final UserPrefs prefs;

    public PortfolioRepository(Context ctx) {
        holdingDao = FrxDatabase.getInstance(ctx).holdingDao();
        prefs = new UserPrefs(ctx);
    }

    public LiveData<List<HoldingEntity>> getHoldings() { return holdingDao.getAll(); }

    public List<HoldingEntity> getHoldingsSync() { return holdingDao.getAllSync(); }

    public HoldingEntity getHoldingByCoinId(String coinId) { return holdingDao.getByCoinId(coinId); }

    public float getCashBalance() { return prefs.getBalance(); }
}
