package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.diellabs.frexa.data.local.entity.OrderEntity;
import com.diellabs.frexa.data.repository.TradingRepository;
import com.diellabs.frexa.util.UserPrefs;
import java.util.List;

public class TradingViewModel extends AndroidViewModel {
    public final LiveData<List<OrderEntity>> orders;
    public final MutableLiveData<Float> virtualBalance = new MutableLiveData<>();

    private final TradingRepository repo;
    private final UserPrefs prefs;

    public TradingViewModel(@NonNull Application app) {
        super(app);
        repo = new TradingRepository(app);
        prefs = new UserPrefs(app);
        orders = repo.getOrders();
        virtualBalance.setValue(prefs.getBalance());
    }

    public void buyCoin(String coinId, String symbol, String name, double nominalIdr, double currentPrice) {
        repo.buyCoin(coinId, symbol, name, nominalIdr, currentPrice);
        virtualBalance.postValue(prefs.getBalance());
    }

    public void sellCoin(String coinId, String symbol, String name, double quantity, double currentPrice) {
        repo.sellCoin(coinId, symbol, name, quantity, currentPrice);
        virtualBalance.postValue(prefs.getBalance());
    }

    public void refreshBalance() { virtualBalance.postValue(prefs.getBalance()); }

    public void loadStats(TradingRepository.StatCallback cb) { repo.getStats(cb); }
}
