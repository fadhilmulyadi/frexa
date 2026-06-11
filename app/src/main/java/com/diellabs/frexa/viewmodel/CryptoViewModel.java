package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.diellabs.frexa.data.remote.api.BinanceWebSocketManager;
import com.diellabs.frexa.data.remote.api.CoinSymbolMapper;
import com.diellabs.frexa.data.remote.model.BinanceKlineEvent;
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
    private final BinanceWebSocketManager wsManager;
    private String activeCoinId = "bitcoin";
    private String activeBinanceSymbol = "BTCUSDT";
    private String activeInterval = "1m";

    public CryptoViewModel(@NonNull Application app) {
        super(app);
        repo = new CryptoRepository(app);

        OkHttpClient wsClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        wsManager = new BinanceWebSocketManager(wsClient);
        wsManager.setListener(new BinanceWebSocketManager.KlineListener() {
            @Override
            public void onKline(BinanceKlineEvent event) {
                BinanceKlineEvent.Kline k = event.kline;
                double open = Double.parseDouble(k.open);
                double high = Double.parseDouble(k.high);
                double low = Double.parseDouble(k.low);
                double close = Double.parseDouble(k.close);

                candleBuilder.updateLiveCandle(open, high, low, close, k.openTime, k.closed);
                chartCandles.postValue(candleBuilder.getCandles());
                livePrice.postValue(close);
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
        activeBinanceSymbol = CoinSymbolMapper.toBinanceSymbol(id);
        int tf = selectedTimeframe.getValue() != null ? selectedTimeframe.getValue() : 60;
        activeInterval = CoinSymbolMapper.toInterval(tf);

        candleBuilder.reset(tf);

        repo.fetchBinanceKlines(activeBinanceSymbol, activeInterval, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());

            String stream = CoinSymbolMapper.toStreamName(activeBinanceSymbol, activeInterval);
            wsManager.connect();
            wsManager.switchStream(stream);
        });
    }

    public void setTimeframe(int seconds) {
        selectedTimeframe.postValue(seconds);
        String newInterval = CoinSymbolMapper.toInterval(seconds);
        activeInterval = newInterval;
        candleBuilder.reset(seconds);

        repo.fetchBinanceKlines(activeBinanceSymbol, newInterval, 60, data -> {
            ohlcData.postValue(data);
            candleBuilder.setHistoricalCandles(data);
            chartCandles.postValue(candleBuilder.getCandles());

            String stream = CoinSymbolMapper.toStreamName(activeBinanceSymbol, newInterval);
            wsManager.switchStream(stream);
        });
    }

    public void fetchMarketChart(String id) { repo.fetchMarketChart(id, marketChart); }

    public void startPricePolling() {
        if (!wsManager.isConnected()) {
            wsManager.connect();
        }
    }

    public void stopPricePolling() {
        wsManager.disconnect();
    }

    @Override protected void onCleared() { wsManager.disconnect(); }
}
