package com.diellabs.frexa.ui.transaksi;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.databinding.FragmentTransaksiBinding;
import com.diellabs.frexa.ui.tradedetail.TradeDetailActivity;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.TradingViewModel;

public class TransaksiFragment extends Fragment {
    private FragmentTransaksiBinding b;
    private TradingViewModel tradingVm;
    private CryptoViewModel cryptoVm;
    private OpenTradeAdapter openAdapter;
    private HistoryAdapter histAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle saved) {
        b = FragmentTransaksiBinding.inflate(inf, parent, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        tradingVm = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);
        cryptoVm = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);

        openAdapter = new OpenTradeAdapter();
        b.rvOpenTrades.setAdapter(openAdapter);

        histAdapter = new HistoryAdapter();
        histAdapter.setListener(trade -> {
            Intent i = new Intent(requireContext(), TradeDetailActivity.class);
            i.putExtra("trade_id", trade.id);
            startActivity(i);
        });
        b.rvHistory.setAdapter(histAdapter);

        tradingVm.openTrades.observe(getViewLifecycleOwner(), trades -> {
            openAdapter.setData(trades);
            for (TradeEntity t : trades) {
                tradingVm.scheduleExpiry(t, exitPrice -> {
                    double price = cryptoVm.livePrice.getValue() != null
                        ? cryptoVm.livePrice.getValue() : t.entryPrice;
                    tradingVm.closeTrade(t, price);
                });
            }
        });

        tradingVm.closedTrades.observe(getViewLifecycleOwner(), histAdapter::setData);
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
