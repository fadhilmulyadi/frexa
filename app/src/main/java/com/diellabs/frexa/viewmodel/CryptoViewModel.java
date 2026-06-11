package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.data.repository.CryptoRepository;
import java.util.List;

public class CryptoViewModel extends AndroidViewModel {
    public final MutableLiveData<List<CoinMarket>> coinList = new MutableLiveData<>();
    public final MutableLiveData<Double> livePrice = new MutableLiveData<>();
    public final MutableLiveData<List<List<Double>>> ohlcData = new MutableLiveData<>();
    public final MutableLiveData<List<List<Double>>> chartCandles = new MutableLiveData<>();
    public final MutableLiveData<MarketChart> marketChart = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Integer> selectedTimeframe = new MutableLiveData<>(60);

    private final CryptoRepository repo;
    private String activeCoinId = "bitcoin";

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);
    }

    public void fetchMarkets() { repo.fetchMarkets(coinList, isLoading, errorMessage); }

    public void setActiveCoin(String id) {
        activeCoinId = id;
        repo.fetchOhlc(id, data -> ohlcData.postValue(data));
    }

    public void setTimeframe(int seconds) {
        selectedTimeframe.postValue(seconds);
        repo.fetchOhlc(activeCoinId, data -> ohlcData.postValue(data));
    }

    public void fetchMarketChart(String id) { repo.fetchMarketChart(id, marketChart); }

    public void startPricePolling() {
        // No-op for now; polling managed by repository
    }

    public void stopPricePolling() {
        // No-op for now; polling managed by repository
    }
}
