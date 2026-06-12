package com.diellabs.frexa.ui.coindetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.remote.model.CoinDetail;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.PortfolioViewModel;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import com.diellabs.frexa.util.FrxSnackbar;
import java.util.List;

public class CoinDetailFragment extends Fragment implements OrderBottomSheetFragment.OrderResultListener {
    private String coinId, coinSymbol, coinName;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coin_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            coinId = getArguments().getString("coinId", "");
            coinSymbol = getArguments().getString("coinSymbol", "");
            coinName = getArguments().getString("coinName", "");
        }

        ImageView coinImage = view.findViewById(R.id.coin_image);
        TextView tvPair = view.findViewById(R.id.tv_pair);
        TextView tvCoinName = view.findViewById(R.id.tv_coin_name);
        TextView tvPrice = view.findViewById(R.id.tv_price);
        TextView tvChange = view.findViewById(R.id.tv_change);
        TextView perf24h = view.findViewById(R.id.perf_24h);
        TextView perf7d = view.findViewById(R.id.perf_7d);
        TextView perf30d = view.findViewById(R.id.perf_30d);
        TextView perf1y = view.findViewById(R.id.perf_1y);
        TextView perfAth = view.findViewById(R.id.perf_ath);
        TextView perfVol = view.findViewById(R.id.perf_vol);
        TextView statRank = view.findViewById(R.id.stat_rank);
        TextView statMarketCap = view.findViewById(R.id.stat_market_cap);
        TextView statSupply = view.findViewById(R.id.stat_supply);
        TextView statAth = view.findViewById(R.id.stat_ath);

        tvPair.setText(coinSymbol.toUpperCase() + "/IDR");
        tvCoinName.setText(coinName);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
            Navigation.findNavController(v).navigateUp());

        CryptoViewModel cryptoVM = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);
        PortfolioViewModel portfolioVM = new ViewModelProvider(requireActivity()).get(PortfolioViewModel.class);
        com.diellabs.frexa.viewmodel.WatchlistViewModel watchlistVM = new ViewModelProvider(requireActivity()).get(com.diellabs.frexa.viewmodel.WatchlistViewModel.class);

        // Observe before fetching
        cryptoVM.coinDetail.observe(getViewLifecycleOwner(), detail -> {
            if (detail == null || detail.marketData == null) return;
            CoinDetail.MarketData md = detail.marketData;

            if (detail.image != null && !detail.image.isEmpty()) {
                Glide.with(requireContext()).load(detail.image).circleCrop().into(coinImage);
            } else {
                coinImage.setImageDrawable(null);
            }

            applyPerf(perf7d, md.priceChange7d);
            applyPerf(perf30d, md.priceChange30d);
            applyPerf(perf1y, md.priceChange1y);

            double athPct = md.athChangePercentage != null && md.athChangePercentage.containsKey("idr")
                ? md.athChangePercentage.get("idr") : 0;
            applyPerf(perfAth, athPct);

            statRank.setText("#" + detail.marketCapRank);

            double mcap = md.marketCap != null && md.marketCap.containsKey("idr") ? md.marketCap.get("idr") : 0;
            statMarketCap.setText(CurrencyFormatter.formatCompact(mcap));

            statSupply.setText(CurrencyFormatter.formatCompact(md.circulatingSupply) + " " + detail.symbol.toUpperCase());

            double athPrice = md.ath != null && md.ath.containsKey("idr") ? md.ath.get("idr") : 0;
            statAth.setText(CurrencyFormatter.formatIdrFull(athPrice));
        });

        cryptoVM.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), error, android.widget.Toast.LENGTH_SHORT).show();
                cryptoVM.errorMessage.setValue("");
            }
        });

        ImageView btnWatchlist = view.findViewById(R.id.btn_watchlist);
        watchlistVM.watchlist.observe(getViewLifecycleOwner(), items -> {
            boolean isWatchlisted = false;
            if (items != null) {
                for (var item : items) {
                    if (item.coinId.equals(coinId)) {
                        isWatchlisted = true;
                        break;
                    }
                }
            }
            btnWatchlist.setImageResource(isWatchlisted ? R.drawable.ic_watchlist_filled : R.drawable.ic_watchlist);
        });

        btnWatchlist.setOnClickListener(v -> {
            boolean isWatchlisted = false;
            List<com.diellabs.frexa.data.local.entity.WatchlistEntity> items = watchlistVM.watchlist.getValue();
            if (items != null) {
                for (var item : items) {
                    if (item.coinId.equals(coinId)) {
                        isWatchlisted = true;
                        break;
                    }
                }
            }
            if (isWatchlisted) {
                watchlistVM.remove(coinId);
                FrxSnackbar.showSuccess(getView(), "Watchlist", "Dihapus dari watchlist", null, null);
            } else {
                watchlistVM.add(coinId, coinSymbol, coinName);
                FrxSnackbar.showSuccess(getView(), "Watchlist", "Ditambahkan ke watchlist", null, null);
            }
        });

        cryptoVM.coinList.observe(getViewLifecycleOwner(), coins -> {
            if (coins == null) return;
            for (CoinMarket c : coins) {
                if (c.id.equals(coinId)) {
                    if (c.image != null && !c.image.isEmpty()) {
                        Glide.with(requireContext()).load(c.image).circleCrop().into(coinImage);
                        coinImage.setVisibility(View.VISIBLE);
                    }
                    tvPrice.setText(CurrencyFormatter.formatIdrFull(c.currentPrice));
                    boolean isUp = c.priceChangePercentage24h >= 0;
                    tvChange.setText(CurrencyFormatter.formatPercent(c.priceChangePercentage24h));
                    tvChange.setTextColor(getResources().getColor(isUp ? R.color.frx_up : R.color.frx_down, null));
                    tvChange.setBackgroundResource(isUp ? R.drawable.bg_chip_up : R.drawable.bg_chip_down);
                    perf24h.setText(CurrencyFormatter.formatPercent(c.priceChangePercentage24h));
                    perf24h.setTextColor(getResources().getColor(isUp ? R.color.frx_up : R.color.frx_down, null));
                    perfVol.setText(CurrencyFormatter.formatCompact(c.totalVolume));
                    break;
                }
            }
        });

        cryptoVM.fetchCoinDetail(coinId);

        portfolioVM.holdings.observe(getViewLifecycleOwner(), holdings -> {
            if (holdings == null) return;
            for (HoldingEntity h : holdings) {
                if (h.coinId.equals(coinId)) {
                    view.findViewById(R.id.position_card).setVisibility(View.VISIBLE);
                    TextView tvPos = view.findViewById(R.id.tv_position);
                    tvPos.setText(CurrencyFormatter.formatCoinQty(h.quantity) + " " + coinSymbol.toUpperCase() + " · avg " + CurrencyFormatter.formatCompact(h.avgBuyPrice));
                    break;
                }
            }
        });

        view.findViewById(R.id.btn_buy).setOnClickListener(v -> {
            OrderBottomSheetFragment sheet = OrderBottomSheetFragment.newInstance(coinId, coinSymbol, coinName, "BUY");
            sheet.setOrderResultListener(this);
            sheet.show(getChildFragmentManager(), "order");
        });

        view.findViewById(R.id.btn_sell).setOnClickListener(v -> {
            OrderBottomSheetFragment sheet = OrderBottomSheetFragment.newInstance(coinId, coinSymbol, coinName, "SELL");
            sheet.setOrderResultListener(this);
            sheet.show(getChildFragmentManager(), "order");
        });
    }

    @Override
    public void onBuySuccess(String symbol, double qty, double price, double fee, double total, String txId) {
        OrderResultDialog dialog = OrderResultDialog.buySuccess(symbol, qty, price, fee, total, txId);
        dialog.setOnResultActionListener(() -> {
            if (getView() != null) Navigation.findNavController(getView()).navigate(R.id.ordersFragment);
        });
        dialog.show(getChildFragmentManager(), "result");
        if (getView() != null) {
            FrxSnackbar.showSuccess(getView(), "Beli Berhasil", 
                "Berhasil membeli " + CurrencyFormatter.formatCoinQty(qty) + " " + symbol, 
                null, null);
        }
    }

    @Override
    public void onSellSuccess(String symbol, double qty, double price, double fee, double received, 
            double pnlPct, double pnlIdr, String txId) {
        OrderResultDialog dialog = OrderResultDialog.sellSuccess(symbol, qty, price, fee, received, pnlPct, pnlIdr, txId);
        dialog.setOnResultActionListener(() -> {
            if (getView() != null) Navigation.findNavController(getView()).navigate(R.id.ordersFragment);
        });
        dialog.show(getChildFragmentManager(), "result");
        if (getView() != null) {
            FrxSnackbar.showSuccess(getView(), "Jual Berhasil", 
                "Berhasil menjual " + CurrencyFormatter.formatCoinQty(qty) + " " + symbol, 
                null, null);
        }
    }

    @Override
    public void onBuyFailed(double needed, double available) {
        OrderResultDialog dialog = OrderResultDialog.buyFailed(needed, available);
        dialog.show(getChildFragmentManager(), "result");
    }

    private void applyPerf(TextView tv, double pct) {
        tv.setText(CurrencyFormatter.formatPercent(pct));
        tv.setTextColor(getResources().getColor(pct >= 0 ? R.color.frx_up : R.color.frx_down, null));
    }
}
