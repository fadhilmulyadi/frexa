package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.api.BitfinexWebSocketManager;
import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.data.repository.CryptoRepository;
import com.diellabs.frexa.ui.terminal.LiveCandleBuilder;
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
    private final LiveCandleBuilder candleBuilder = new LiveCandleBuilder(60);
    private final BitfinexWebSocketManager wsManager = new BitfinexWebSocketManager();

    private String activeCoinId = "bitcoin";
    private String activeSymbol = "tBTCUSD";

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);
    }

    public void fetchMarkets() { repo.fetchMarkets(coinList, isLoading, errorMessage); }

    public void setActiveCoin(String id) {
        activeCoinId = id;
        activeSymbol = CoinSymbolMapper.toBitfinexSymbol(id);
        int tf = selectedTimeframe.getValue() != null ? selectedTimeframe.getValue() : 60;

        candleBuilder.reset(tf);

        repo.fetchBitfinexKlines(activeSymbol, tf, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
        });

        startPricePolling();
    }

    public void setTimeframe(int seconds) {
        selectedTimeframe.postValue(seconds);
        candleBuilder.reset(seconds);

        repo.fetchBitfinexKlines(activeSymbol, seconds, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());
        });
    }

    public void fetchMarketChart(String id) { repo.fetchMarketChart(id, marketChart); }

    public void startPricePolling() {
        wsManager.connect(activeSymbol, price -> {
            candleBuilder.addTick(price, System.currentTimeMillis());
            chartCandles.postValue(candleBuilder.getCandles());
            livePrice.postValue(price);
        });
    }

    public void stopPricePolling() {
        wsManager.disconnect();
    }

    @Override protected void onCleared() {
        wsManager.disconnect();
    }
}
