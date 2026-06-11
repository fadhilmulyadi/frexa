package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.api.CoinCapWebSocketManager;
import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.data.repository.CryptoRepository;
import com.diellabs.frexa.ui.terminal.LiveCandleBuilder;
import okhttp3.OkHttpClient;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private final LiveCandleBuilder candleBuilder = new LiveCandleBuilder(60);
    private final CoinCapWebSocketManager coinCapWs;
    private String activeCoinId = "bitcoin";
    private String activeFsym = "BTC";

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);

        OkHttpClient wsClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        coinCapWs = new CoinCapWebSocketManager(wsClient);
        coinCapWs.setListener(new CoinCapWebSocketManager.PriceListener() {
            @Override
            public void onPrice(String assetId, double price) {
                candleBuilder.addTick(price, System.currentTimeMillis());
                chartCandles.postValue(candleBuilder.getCandles());
                livePrice.postValue(price);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue("WebSocket error: " + message);
            }
        });
    }

    public void fetchMarkets() { repo.fetchMarkets(coinList, isLoading, errorMessage); }

    public void setActiveCoin(String id) {
        activeCoinId = id;
        activeFsym = CoinSymbolMapper.toCryptoCompareFsym(id);
        int tf = selectedTimeframe.getValue() != null ? selectedTimeframe.getValue() : 60;

        candleBuilder.reset(tf);

        repo.fetchCryptoCompareKlines(activeFsym, tf, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
            coinCapWs.switchAsset(id);
        });
    }

    public void setTimeframe(int seconds) {
        selectedTimeframe.postValue(seconds);
        candleBuilder.reset(seconds);

        repo.fetchCryptoCompareKlines(activeFsym, seconds, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
        });
    }

    public void fetchMarketChart(String id) { repo.fetchMarketChart(id, marketChart); }

    public void startPricePolling() {
        if (!coinCapWs.isConnected()) {
            coinCapWs.connect(activeCoinId);
        }
    }

    public void stopPricePolling() {
        coinCapWs.disconnect();
    }

    @Override protected void onCleared() { coinCapWs.disconnect(); }
}
