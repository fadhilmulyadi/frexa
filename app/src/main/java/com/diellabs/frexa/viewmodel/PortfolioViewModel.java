package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import com.diellabs.frexa.data.repository.PortfolioRepository;
import java.util.List;

public class PortfolioViewModel extends AndroidViewModel {
    public final LiveData<List<HoldingEntity>> holdings;
    public final MutableLiveData<Float> cashBalance = new MutableLiveData<>();
    public final MutableLiveData<Double> totalPortfolioValue = new MutableLiveData<>(0.0);
    public final MutableLiveData<Double> totalCostBasis = new MutableLiveData<>(0.0);
    public final MutableLiveData<Double> unrealizedPnl = new MutableLiveData<>(0.0);
    public final MutableLiveData<Double> pnlPercent = new MutableLiveData<>(0.0);

    private final PortfolioRepository repo;

    public PortfolioViewModel(@NonNull Application app) {
        super(app);
        repo = new PortfolioRepository(app);
        holdings = repo.getHoldings();
        cashBalance.setValue(repo.getCashBalance());
    }

    public void refreshBalance() {
        cashBalance.postValue(repo.getCashBalance());
    }

    public void updatePortfolioValue(double cryptoValue, double costBasis) {
        totalPortfolioValue.postValue(cryptoValue);
        totalCostBasis.postValue(costBasis);
        double pnl = cryptoValue - costBasis;
        unrealizedPnl.postValue(pnl);
        pnlPercent.postValue(costBasis > 0 ? (pnl / costBasis) * 100 : 0);
    }
}
