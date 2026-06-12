package com.diellabs.frexa.ui.terminal;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.databinding.FragmentTerminalBinding;
import com.diellabs.frexa.ui.deposit.DepositBottomSheetFragment;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.util.UserPrefs;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.TradingViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class TerminalFragment extends Fragment implements ActiveTradeBottomSheetFragment.Callback {
    private FragmentTerminalBinding b;
    private CryptoViewModel cryptoVm;
    private TradingViewModel tradingVm;
    private UserPrefs prefs;
    private double stakeAmount = 10.0;
    private final int[] DURATIONS = {60, 300, 900, 1800, 3600};
    private final String[] DURATION_LABELS = {"1 mnt", "5 mnt", "15 mnt", "30 mnt", "1 jam"};
    private int durationIndex = 0;
    private int durationSeconds = 60;
    private String durationLabel = "1 mnt";
    private int profitPercent = 85;
    private double currentPrice = 0;

    private static final int COLOR_ACTIVE = 0xFF36E07A;
    private boolean chartReady = false;
    private List<List<Double>> pendingInitData = null;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle saved) {
        b = FragmentTerminalBinding.inflate(inf, parent, false);
        return b.getRoot();
    }

    private void setupWebView() {
        WebSettings ws = b.chart.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        b.chart.setBackgroundColor(0xFF1A1C20);
        b.chart.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                chartReady = true;
                if (pendingInitData != null) {
                    evalInitChart(pendingInitData);
                    pendingInitData = null;
                }
            }
        });
        b.chart.loadUrl("file:///android_asset/chart.html");
    }

    private void evalInitChart(List<List<Double>> data) {
        String json = candlesToJson(data);
        b.chart.evaluateJavascript("initChart('" + json + "')", null);
    }

    private void evalUpdateLastCandle(List<List<Double>> data) {
        if (data == null || data.isEmpty()) return;
        List<Double> last = data.get(data.size() - 1);
        if (last.size() < 5) return;
        String json = "[" + last.get(0).longValue() + ","
                + last.get(1) + "," + last.get(2) + ","
                + last.get(3) + "," + last.get(4) + "]";
        b.chart.evaluateJavascript("updateLastCandle('" + json + "')", null);
    }

    private String candlesToJson(List<List<Double>> candles) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < candles.size(); i++) {
            List<Double> c = candles.get(i);
            if (c.size() < 5) continue;
            if (sb.length() > 1) sb.append(",");
            sb.append("[").append(c.get(0).longValue()).append(",")
              .append(c.get(1)).append(",").append(c.get(2)).append(",")
              .append(c.get(3)).append(",").append(c.get(4)).append("]");
        }
        return sb.append("]").toString();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        prefs = new UserPrefs(requireContext());
        cryptoVm = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);
        tradingVm = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);

        setupWebView();

        String coinId = prefs.getActiveCoinId();
        cryptoVm.setActiveCoin(coinId);

        setupStakeControls();
        setupDurationControls();
        setupTimeframeButtons();

        b.btnWallet.setOnClickListener(x ->
            new DepositBottomSheetFragment().show(getChildFragmentManager(), "deposit"));

        // Historical data → reset chart (dipanggil saat coin/timeframe ganti)
        cryptoVm.ohlcData.observe(getViewLifecycleOwner(), data -> {
            if (data == null) return;
            if (!chartReady) { pendingInitData = data; return; }
            evalInitChart(data);
        });

        // Live candle update setiap 1 detik
        cryptoVm.chartCandles.observe(getViewLifecycleOwner(), data -> {
            if (!chartReady || data == null) return;
            evalUpdateLastCandle(data);
        });

        // Harga live → price line dashed
        cryptoVm.livePrice.observe(getViewLifecycleOwner(), price -> {
            currentPrice = price;
            if (chartReady)
                b.chart.evaluateJavascript("setCurrentPrice(" + price + ")", null);
        });

        tradingVm.virtualBalance.observe(getViewLifecycleOwner(), bal ->
            b.tvBalance.setText(CurrencyFormatter.formatBalance(bal)));

        cryptoVm.coinList.observe(getViewLifecycleOwner(), coins -> {
            coins.stream().filter(c -> c.id.equals(coinId)).findFirst().ifPresent(c -> {
                b.tvAccountName.setText(c.symbol.toUpperCase() + "/USD");
                profitPercent = c.marketCapRank <= 10 ? 90 : c.marketCapRank <= 30 ? 80 : 75;
                b.tvProfitLabel.setText("Profit: " + profitPercent + "%");
            });
        });

        cryptoVm.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) b.tvOffline.setVisibility(View.VISIBLE);
            else b.tvOffline.setVisibility(View.GONE);
        });

        b.btnUp.setOnClickListener(x -> placeTrade("UP"));
        b.btnDown.setOnClickListener(x -> placeTrade("DOWN"));
    }

    private void setupStakeControls() {
        b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        b.btnStakeMinus.setOnClickListener(x -> {
            stakeAmount = Math.max(1, stakeAmount - 1);
            b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        });
        b.btnStakePlus.setOnClickListener(x -> {
            stakeAmount += 1;
            b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        });
    }

    private void setupDurationControls() {
        b.btnDuration.setOnClickListener(x -> {
            DurationBottomSheetFragment sheet = new DurationBottomSheetFragment();
            sheet.setCallback((sec, label) -> {
                durationSeconds = sec;
                durationLabel = label;
                b.tvDuration.setText(label);
                for (int i = 0; i < DURATIONS.length; i++) {
                    if (DURATIONS[i] == sec) { durationIndex = i; break; }
                }
            });
            sheet.show(getChildFragmentManager(), "duration");
        });

        b.btnDurationMinus.setOnClickListener(x -> {
            if (durationIndex > 0) { durationIndex--; updateDurationUI(); }
        });
        b.btnDurationPlus.setOnClickListener(x -> {
            if (durationIndex < DURATIONS.length - 1) { durationIndex++; updateDurationUI(); }
        });
    }

    private final MaterialButton[] tfButtons = new MaterialButton[5];

    private void setupTimeframeButtons() {
        tfButtons[0] = b.btnTf1m;
        tfButtons[1] = b.btnTf5m;
        tfButtons[2] = b.btnTf15m;
        tfButtons[3] = b.btnTf30m;
        tfButtons[4] = b.btnTf1h;

        int[] seconds = {60, 300, 900, 1800, 3600};

        for (int i = 0; i < tfButtons.length; i++) {
            final int idx = i;
            final int sec = seconds[i];
            tfButtons[i].setOnClickListener(x -> selectTimeframe(sec, idx));
        }

        // Default aktif: tombol pertama (1m)
        highlightTimeframeButton(0);
    }

    private void selectTimeframe(int seconds, int index) {
        cryptoVm.setTimeframe(seconds);
        highlightTimeframeButton(index);
    }

    private void highlightTimeframeButton(int activeIndex) {
        for (int i = 0; i < tfButtons.length; i++) {
            if (i == activeIndex) {
                tfButtons[i].setBackgroundTintList(
                    ColorStateList.valueOf(COLOR_ACTIVE));
                tfButtons[i].setStrokeWidth(0);
            } else {
                tfButtons[i].setBackgroundTintList(
                    ColorStateList.valueOf(Color.TRANSPARENT));
                tfButtons[i].setStrokeWidth(2);
                tfButtons[i].setStrokeColor(
                    ColorStateList.valueOf(0xFF4A4D55));
            }
        }
    }

    private void updateDurationUI() {
        durationSeconds = DURATIONS[durationIndex];
        durationLabel = DURATION_LABELS[durationIndex];
        b.tvDuration.setText(durationLabel);
    }

    private void placeTrade(String direction) {
        if (stakeAmount > prefs.getBalance()) {
            Toast.makeText(requireContext(), "Saldo tidak cukup", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPrice == 0) {
            Toast.makeText(requireContext(), "Menunggu harga terkini", Toast.LENGTH_SHORT).show();
            return;
        }
        String coinId = prefs.getActiveCoinId();
        String[] nameSymbolImg = {"Bitcoin", "BTC", ""};
        if (cryptoVm.coinList.getValue() != null) {
            cryptoVm.coinList.getValue().stream()
                .filter(c -> c.id.equals(coinId)).findFirst().ifPresent(c -> {
                    nameSymbolImg[0] = c.name;
                    nameSymbolImg[1] = c.symbol;
                    nameSymbolImg[2] = c.image;
                });
        }
        TradeEntity trade = tradingVm.placeTrade(coinId, nameSymbolImg[0], nameSymbolImg[1],
            nameSymbolImg[2], direction, stakeAmount, profitPercent, currentPrice, durationSeconds, durationLabel);
        if (chartReady) {
            b.chart.evaluateJavascript("setTradeMarker('" + direction + "')", null);
            b.chart.evaluateJavascript("setEntryLine(" + currentPrice + ",'" + direction + "')", null);
        }
        ActiveTradeBottomSheetFragment.newInstance(trade)
            .show(getChildFragmentManager(), "active_trade");
        Toast.makeText(requireContext(),
            direction.equals("UP") ? "↑ Posisi NAIK dibuka!" : "↓ Posisi TURUN dibuka!",
            Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTradeReversed(TradeEntity newTrade) {
        if (!chartReady) return;
        b.chart.evaluateJavascript("setTradeMarker('" + newTrade.direction + "')", null);
        b.chart.evaluateJavascript("setEntryLine(" + newTrade.entryPrice + ",'" + newTrade.direction + "')", null);
    }

    @Override
    public void onTradeClosed() {
        if (chartReady) b.chart.evaluateJavascript("clearEntryLine()", null);
    }

    @Override public void onResume() { super.onResume(); cryptoVm.startPricePolling(); }
    @Override public void onPause() { super.onPause(); cryptoVm.stopPricePolling(); }
    @Override public void onDestroyView() {
        super.onDestroyView();
        chartReady = false;
        pendingInitData = null;
        b = null;
    }
}
