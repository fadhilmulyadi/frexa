package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.data.repository.CryptoRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CryptoViewModel extends AndroidViewModel {
    public final MutableLiveData<List<CoinMarket>> coinList = new MutableLiveData<>();
    public final MutableLiveData<List<CoinMarket>> topGainers = new MutableLiveData<>();
    public final MutableLiveData<List<CoinMarket>> topLosers = new MutableLiveData<>();
    public final MutableLiveData<Double> livePrice = new MutableLiveData<>();
    public final MutableLiveData<GlobalData> globalData = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Integer> fearGreedValue = new MutableLiveData<>();
    public final MutableLiveData<String> fearGreedLabel = new MutableLiveData<>();
    public final MutableLiveData<CoinDetail> coinDetail = new MutableLiveData<>();

    private final CryptoRepository repo;

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);
    }

    public void fetchMarkets() {
        repo.fetchMarkets(coinList, isLoading, errorMessage);
    }

    public void computeTopMovers() {
        List<CoinMarket> all = coinList.getValue();
        if (all == null || all.isEmpty()) return;

        List<CoinMarket> gainers = new ArrayList<>(all);
        Collections.sort(gainers, (a, b) -> Double.compare(b.priceChangePercentage24h, a.priceChangePercentage24h));
        topGainers.postValue(gainers.subList(0, Math.min(20, gainers.size())));

        List<CoinMarket> losers = new ArrayList<>(all);
        Collections.sort(losers, (a, b) -> Double.compare(a.priceChangePercentage24h, b.priceChangePercentage24h));
        topLosers.postValue(losers.subList(0, Math.min(20, losers.size())));
    }

    public void fetchGlobalData() {
        repo.fetchGlobalData(globalData, errorMessage);
    }

    public void fetchLivePrice(String coinId) {
        repo.fetchLivePrice(coinId, price -> livePrice.postValue(price));
    }

    public void fetchFearGreed() {
        repo.fetchFearGreed(fearGreedValue, fearGreedLabel);
    }

    public void fetchCoinDetail(String coinId) {
        repo.fetchCoinDetail(coinId, coinDetail, errorMessage);
    }
}
