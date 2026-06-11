package com.diellabs.frexa.ui.terminal;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.diellabs.frexa.databinding.FragmentTerminalBinding;
import com.diellabs.frexa.ui.deposit.DepositBottomSheetFragment;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.util.UserPrefs;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.TradingViewModel;

public class TerminalFragment extends Fragment {
    private FragmentTerminalBinding b;
    private CryptoViewModel cryptoVm;
    private TradingViewModel tradingVm;
    private UserPrefs prefs;
    private double stakeAmount = 10.0;
    private int durationSeconds = 60;
    private String durationLabel = "1 mnt";
    private int profitPercent = 85;
    private double currentPrice = 0;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle saved) {
        b = FragmentTerminalBinding.inflate(inf, parent, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        prefs = new UserPrefs(requireContext());
        cryptoVm = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);
        tradingVm = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);

        String coinId = prefs.getActiveCoinId();
        cryptoVm.setActiveCoin(coinId);

        b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        b.btnStakeMinus.setOnClickListener(x -> {
            stakeAmount = Math.max(1, stakeAmount - 1);
            b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        });
        b.btnStakePlus.setOnClickListener(x -> {
            stakeAmount += 1;
            b.tvStake.setText(CurrencyFormatter.formatUsd(stakeAmount));
        });

        b.btnDuration.setOnClickListener(x -> {
            DurationBottomSheetFragment sheet = new DurationBottomSheetFragment();
            sheet.setCallback((sec, label) -> {
                durationSeconds = sec; durationLabel = label;
                b.tvDuration.setText(label);
            });
            sheet.show(getChildFragmentManager(), "duration");
        });

        b.btnWallet.setOnClickListener(x ->
            new DepositBottomSheetFragment().show(getChildFragmentManager(), "deposit"));

        cryptoVm.livePrice.observe(getViewLifecycleOwner(), price -> {
            currentPrice = price;
        });

        tradingVm.virtualBalance.observe(getViewLifecycleOwner(), bal ->
            b.tvBalance.setText(CurrencyFormatter.formatBalance(bal)));

        cryptoVm.ohlcData.observe(getViewLifecycleOwner(), data -> b.chart.setOhlcData(data));

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
                    nameSymbolImg[0] = c.name; nameSymbolImg[1] = c.symbol; nameSymbolImg[2] = c.image;
                });
        }
        tradingVm.placeTrade(coinId, nameSymbolImg[0], nameSymbolImg[1], nameSymbolImg[2],
            direction, stakeAmount, profitPercent, currentPrice, durationSeconds, durationLabel);
        Toast.makeText(requireContext(),
            direction.equals("UP") ? "↑ Posisi NAIK dibuka!" : "↓ Posisi TURUN dibuka!",
            Toast.LENGTH_SHORT).show();
    }

    @Override public void onResume() { super.onResume(); cryptoVm.startPricePolling(); }
    @Override public void onPause() { super.onPause(); cryptoVm.stopPricePolling(); }
    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
