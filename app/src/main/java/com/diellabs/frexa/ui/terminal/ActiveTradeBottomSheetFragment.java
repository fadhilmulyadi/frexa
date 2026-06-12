package com.diellabs.frexa.ui.terminal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.databinding.BottomSheetActiveTradeBinding;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.TradingViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Locale;

public class ActiveTradeBottomSheetFragment extends BottomSheetDialogFragment {

    private static final double EARLY_CLOSE_PENALTY = 0.46;
    private static final double REVERSAL_FEE = 0.09;

    private BottomSheetActiveTradeBinding b;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable countdownTask;
    private double livePrice = 0;

    private String tradeId, coinId, coinName, coinSymbol, coinImageUrl, direction, durationLabel;
    private double stakeAmount, entryPrice;
    private int profitPercent, durationSeconds;
    private long closeTime;

    public interface Callback {
        void onTradeReversed(TradeEntity newTrade);
        void onTradeClosed();
    }

    public static ActiveTradeBottomSheetFragment newInstance(TradeEntity t) {
        Bundle a = new Bundle();
        a.putString("id", t.id);
        a.putString("coinId", t.coinId);
        a.putString("coinName", t.coinName);
        a.putString("coinSymbol", t.coinSymbol);
        a.putString("coinImageUrl", t.coinImageUrl != null ? t.coinImageUrl : "");
        a.putString("direction", t.direction);
        a.putDouble("stake", t.stakeAmount);
        a.putDouble("entryPrice", t.entryPrice);
        a.putInt("profitPct", t.profitPercent);
        a.putInt("durSec", t.durationSeconds);
        a.putString("durLabel", t.durationLabel);
        a.putLong("closeTime", t.closeTime);
        ActiveTradeBottomSheetFragment f = new ActiveTradeBottomSheetFragment();
        f.setArguments(a);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup container, Bundle saved) {
        b = BottomSheetActiveTradeBinding.inflate(inf, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        Bundle a = requireArguments();
        tradeId      = a.getString("id");
        coinId       = a.getString("coinId");
        coinName     = a.getString("coinName");
        coinSymbol   = a.getString("coinSymbol");
        coinImageUrl = a.getString("coinImageUrl");
        direction    = a.getString("direction");
        stakeAmount  = a.getDouble("stake");
        entryPrice   = a.getDouble("entryPrice");
        profitPercent  = a.getInt("profitPct");
        durationSeconds = a.getInt("durSec");
        durationLabel  = a.getString("durLabel");
        closeTime    = a.getLong("closeTime");

        boolean isUp = "UP".equals(direction);
        b.tvTradePair.setText(coinSymbol.toUpperCase() + "/USD · " + profitPercent + "%");
        b.tvTradeStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        b.tvTradeDirection.setText(isUp ? "↑" : "↓");
        b.tvTradeDirection.setTextColor(isUp ? 0xFF36E07A : 0xFFFF5D5D);
        b.tvEarlyCloseAmount.setText("-" + CurrencyFormatter.formatUsd(stakeAmount * EARLY_CLOSE_PENALTY));
        b.btnReverse.setText("Balik Arah dengan " + CurrencyFormatter.formatUsd(stakeAmount * REVERSAL_FEE));

        b.btnReverse.setOnClickListener(x -> doReverse());
        b.btnEarlyClose.setOnClickListener(x -> doEarlyClose());

        startCountdown();
        observeLivePrice();
    }

    private void startCountdown() {
        countdownTask = new Runnable() {
            @Override public void run() {
                if (b == null) return;
                long rem = closeTime - System.currentTimeMillis();
                if (rem <= 0) {
                    notifyTradeClosed();
                    dismiss();
                    return;
                }
                long s = rem / 1000;
                b.tvTradeTimer.setText(String.format(Locale.US, "%02d:%02d", s / 60, s % 60));
                handler.postDelayed(this, 500);
            }
        };
        handler.post(countdownTask);
    }

    private void observeLivePrice() {
        CryptoViewModel vm = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);
        vm.livePrice.observe(getViewLifecycleOwner(), price -> {
            livePrice = price;
            if (b != null) updatePnl(price);
        });
    }

    private void updatePnl(double price) {
        boolean winning = "UP".equals(direction) ? price > entryPrice : price < entryPrice;
        double pnl = winning ? stakeAmount * profitPercent / 100.0 : -stakeAmount;
        b.tvTradePnl.setText((pnl >= 0 ? "+" : "-") + CurrencyFormatter.formatUsd(Math.abs(pnl)));
        b.tvTradePnl.setTextColor(pnl >= 0 ? 0xFF36E07A : 0xFFFF5D5D);
    }

    private void doEarlyClose() {
        TradingViewModel tradingVm = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);
        tradingVm.earlyClose(buildEntity(), livePrice > 0 ? livePrice : entryPrice);
        notifyTradeClosed();
        dismiss();
    }

    private void doReverse() {
        TradingViewModel tradingVm = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);
        double price = livePrice > 0 ? livePrice : entryPrice;
        tradingVm.earlyClose(buildEntity(), price);
        String newDir = "UP".equals(direction) ? "DOWN" : "UP";
        TradeEntity newTrade = tradingVm.placeTrade(coinId, coinName, coinSymbol, coinImageUrl,
            newDir, stakeAmount, profitPercent, price, durationSeconds, durationLabel);
        Fragment parent = getParentFragment();
        if (parent instanceof Callback) ((Callback) parent).onTradeReversed(newTrade);
        dismiss();
    }

    private void notifyTradeClosed() {
        Fragment parent = getParentFragment();
        if (parent instanceof Callback) ((Callback) parent).onTradeClosed();
    }

    private TradeEntity buildEntity() {
        TradeEntity t = new TradeEntity();
        t.id = tradeId; t.coinId = coinId; t.coinName = coinName;
        t.coinSymbol = coinSymbol; t.coinImageUrl = coinImageUrl;
        t.direction = direction; t.stakeAmount = stakeAmount;
        t.profitPercent = profitPercent; t.entryPrice = entryPrice;
        t.durationSeconds = durationSeconds; t.durationLabel = durationLabel;
        t.closeTime = closeTime;
        t.openTime = closeTime - (durationSeconds * 1000L);
        t.status = "OPEN";
        return t;
    }

    @Override public void onDestroyView() {
        if (countdownTask != null) handler.removeCallbacks(countdownTask);
        b = null;
        super.onDestroyView();
    }
}
