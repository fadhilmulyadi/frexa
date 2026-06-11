package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.api.MexcWebSocketManager;
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
    private final MexcWebSocketManager mexcWs;
    private String activeCoinId = "bitcoin";
    private String activeSymbol = "BTCUSDT";

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);

        OkHttpClient wsClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        mexcWs = new MexcWebSocketManager(wsClient);
        mexcWs.setListener(new MexcWebSocketManager.PriceListener() {
            @Override
            public void onPrice(String symbol, double price) {
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
        activeSymbol = CoinSymbolMapper.toMexcSymbol(id);
        int tf = selectedTimeframe.getValue() != null ? selectedTimeframe.getValue() : 60;

        candleBuilder.reset(tf);

        repo.fetchMexcKlines(activeSymbol, tf, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
            mexcWs.switchSymbol(activeSymbol);
        });
    }

    public void setTimeframe(int seconds) {
        selectedTimeframe.postValue(seconds);
        candleBuilder.reset(seconds);

        repo.fetchMexcKlines(activeSymbol, seconds, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
        });
    }

    public void fetchMarketChart(String id) { repo.fetchMarketChart(id, marketChart); }

    public void startPricePolling() {
        if (!mexcWs.isConnected()) {
            mexcWs.connect(activeSymbol);
        }
    }

    public void stopPricePolling() {
        mexcWs.disconnect();
    }

    @Override protected void onCleared() { mexcWs.disconnect(); }
}
