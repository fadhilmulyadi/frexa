package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.data.repository.CryptoRepository;
import com.diellabs.frexa.util.AppExecutors;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CryptoViewModel extends AndroidViewModel {
    public final MutableLiveData<List<CoinMarket>> coinList = new MutableLiveData<>();
    public final MutableLiveData<Double> livePrice = new MutableLiveData<>();
    public final MutableLiveData<List<List<Double>>> ohlcData = new MutableLiveData<>();
    public final MutableLiveData<MarketChart> marketChart = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final CryptoRepository repo;
    private ScheduledExecutorService scheduler;
    private String activeCoinId = "bitcoin";

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);
    }

    public void fetchMarkets() { repo.fetchMarkets(coinList, isLoading, errorMessage); }

    public void setActiveCoin(String id) {
        activeCoinId = id;
        repo.fetchOhlc(id, ohlcData);
    }

    public void fetchMarketChart(String id) { repo.fetchMarketChart(id, marketChart); }

    public void startPricePolling() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = AppExecutors.getInstance().newScheduler();
        scheduler.scheduleAtFixedRate(
            () -> repo.fetchLivePrice(activeCoinId, livePrice, errorMessage),
            0, 10, TimeUnit.SECONDS);
    }

    public void stopPricePolling() { if (scheduler != null) scheduler.shutdownNow(); }

    @Override protected void onCleared() { stopPricePolling(); }
}
