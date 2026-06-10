package com.diellabs.frexa.ui.home;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.databinding.FragmentHomeBinding;
import com.diellabs.frexa.ui.deposit.DepositBottomSheetFragment;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.util.UserPrefs;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.TradingViewModel;
import com.google.android.material.tabs.TabLayout;
import java.util.*;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding b;
    private CryptoViewModel cryptoVm;
    private TradingViewModel tradingVm;
    private MoverAdapter moverAdapter;
    private CoinAdapter coinAdapter;
    private List<CoinMarket> allCoins = new ArrayList<>();
    private UserPrefs prefs;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle saved) {
        b = FragmentHomeBinding.inflate(inf, parent, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        prefs = new UserPrefs(requireContext());
        cryptoVm = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);
        tradingVm = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);

        b.tvAccountName.setText(prefs.getUserName());
        b.tvBalance.setText(CurrencyFormatter.formatBalance(prefs.getBalance()));
        b.ivAvatar.setOnClickListener(x ->
            Navigation.findNavController(v).navigate(R.id.action_home_to_profile));
        b.btnBell.setOnClickListener(x ->
            Navigation.findNavController(v).navigate(R.id.action_home_to_notification));
        b.btnDeposit.setOnClickListener(x ->
            new DepositBottomSheetFragment().show(getChildFragmentManager(), "deposit"));

        moverAdapter = new MoverAdapter(this::openTerminal);
        b.rvMovers.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        b.rvMovers.setAdapter(moverAdapter);

        coinAdapter = new CoinAdapter(new CoinAdapter.OnCoinAction() {
            @Override public void onRowClick(CoinMarket coin) { openTerminal(coin); }
            @Override public void onInfoClick(CoinMarket coin) { openAssetDetail(coin); }
        });
        b.rvAssets.setAdapter(coinAdapter);

        b.tabAssets.addTab(b.tabAssets.newTab().setText("Top"));
        b.tabAssets.addTab(b.tabAssets.newTab().setText("Gainers"));
        b.tabAssets.addTab(b.tabAssets.newTab().setText("Losers"));
        b.tabAssets.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { filterCoins(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        cryptoVm.coinList.observe(getViewLifecycleOwner(), coins -> {
            allCoins = coins;
            List<CoinMarket> movers = coins.stream()
                    .sorted((a, c2) -> Double.compare(c2.priceChangePercentage24h, a.priceChangePercentage24h))
                    .limit(5).collect(Collectors.toList());
            moverAdapter.setData(movers);
            filterCoins(b.tabAssets.getSelectedTabPosition());
            b.tvOfflineBanner.setVisibility(View.GONE);
            b.btnRefresh.setVisibility(View.GONE);
        });

        cryptoVm.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                b.tvOfflineBanner.setVisibility(View.VISIBLE);
                b.btnRefresh.setVisibility(View.VISIBLE);
            }
        });

        b.btnRefresh.setOnClickListener(x -> cryptoVm.fetchMarkets());

        tradingVm.virtualBalance.observe(getViewLifecycleOwner(), bal ->
            b.tvBalance.setText(CurrencyFormatter.formatBalance(bal)));

        cryptoVm.fetchMarkets();
    }

    private void filterCoins(int tab) {
        if (allCoins.isEmpty()) return;
        List<CoinMarket> filtered;
        if (tab == 1) {
            filtered = allCoins.stream()
                .sorted((a, c) -> Double.compare(c.priceChangePercentage24h, a.priceChangePercentage24h))
                .collect(Collectors.toList());
        } else if (tab == 2) {
            filtered = allCoins.stream()
                .sorted(Comparator.comparingDouble(c -> c.priceChangePercentage24h))
                .collect(Collectors.toList());
        } else {
            filtered = allCoins.stream()
                .sorted(Comparator.comparingInt(c -> c.marketCapRank))
                .collect(Collectors.toList());
        }
        coinAdapter.setData(filtered);
    }

    private void openTerminal(CoinMarket coin) {
        prefs.setActiveCoinId(coin.id);
        cryptoVm.setActiveCoin(coin.id);
        Navigation.findNavController(requireView()).navigate(R.id.terminalFragment);
    }

    private void openAssetDetail(CoinMarket coin) {
        Bundle args = new Bundle();
        args.putString("coinId", coin.id);
        Navigation.findNavController(requireView())
                  .navigate(R.id.action_home_to_asset_detail, args);
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
