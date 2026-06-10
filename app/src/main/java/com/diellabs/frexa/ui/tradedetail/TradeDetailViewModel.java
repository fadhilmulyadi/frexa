package com.diellabs.frexa.ui.tradedetail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.diellabs.frexa.BuildConfig;
import com.diellabs.frexa.data.local.db.FrxDatabase;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.data.remote.api.RetrofitClient;
import com.diellabs.frexa.data.remote.model.*;
import com.diellabs.frexa.data.repository.CryptoRepository;
import com.diellabs.frexa.util.AppExecutors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TradeDetailViewModel extends AndroidViewModel {
    public final MutableLiveData<TradeEntity> tradeDetail = new MutableLiveData<>();
    public final MutableLiveData<MarketChart> chartData = new MutableLiveData<>();
    public final MutableLiveData<String> aiAnalysis = new MutableLiveData<>();
    public final MutableLiveData<Boolean> aiLoading = new MutableLiveData<>(false);

    private final CryptoRepository cryptoRepo;

    public TradeDetailViewModel(@NonNull Application app) {
        super(app);
        cryptoRepo = new CryptoRepository(app);
    }

    public void loadTrade(String tradeId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            TradeEntity t = FrxDatabase.getInstance(getApplication()).tradeDao().getTradeById(tradeId);
            tradeDetail.postValue(t);
            if (t != null) cryptoRepo.fetchMarketChart(t.coinId, chartData);
        });
    }

    public void analyzeWithAI(TradeEntity trade) {
        aiLoading.setValue(true);
        String prompt = String.format(
            "Analisa transaksi crypto berikut dalam Bahasa Indonesia (singkat, maks 150 kata):\n" +
            "Aset: %s (%s)\nArah: %s\nNominal: $%.2f\nDurasi: %s\n" +
            "Harga masuk: $%.2f\nHarga keluar: $%.2f\nHasil: %s\nProfit/Rugi: $%.2f",
            trade.coinName, trade.coinSymbol, trade.direction, trade.stakeAmount,
            trade.durationLabel, trade.entryPrice, trade.exitPrice,
            trade.isWin ? "Menang" : "Kalah", trade.pnl
        );

        RetrofitClient.getGeminiService()
            .generateContent(BuildConfig.GEMINI_API_KEY, new GeminiRequest(prompt))
            .enqueue(new Callback<GeminiResponse>() {
                @Override public void onResponse(Call<GeminiResponse> c, Response<GeminiResponse> r) {
                    aiLoading.postValue(false);
                    if (r.isSuccessful() && r.body() != null) {
                        aiAnalysis.postValue(r.body().extractText());
                    } else {
                        aiAnalysis.postValue("Gagal mendapatkan analisa AI.");
                    }
                }
                @Override public void onFailure(Call<GeminiResponse> c, Throwable t) {
                    aiLoading.postValue(false);
                    aiAnalysis.postValue("Tidak ada koneksi untuk analisa AI.");
                }
            });
    }
}
