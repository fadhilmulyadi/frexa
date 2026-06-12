package com.diellabs.frexa.ui.watchlist;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.local.entity.WatchlistEntity;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.ui.custom.MonogramView;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.WatchlistViewModel;
import java.util.List;

public class WatchlistFragment extends Fragment {
    private WatchlistViewModel watchlistVM;
    private CryptoViewModel cryptoVM;
    private LinearLayout watchlistList;
    private TextView tvCount, tvEmpty;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        watchlistVM = new ViewModelProvider(requireActivity()).get(WatchlistViewModel.class);
        cryptoVM = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);

        watchlistList = view.findViewById(R.id.watchlist_list);
        tvCount = view.findViewById(R.id.tv_watchlist_count);
        tvEmpty = view.findViewById(R.id.tv_empty);

        watchlistVM.watchlist.observe(getViewLifecycleOwner(), items -> rebuildList(items));
        cryptoVM.coinList.observe(getViewLifecycleOwner(), coins -> {
            List<WatchlistEntity> items = watchlistVM.watchlist.getValue();
            if (items != null) rebuildList(items);
        });

        view.findViewById(R.id.btn_add_coin).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.marketsFragment);
        });
    }

    private void rebuildList(List<WatchlistEntity> items) {
        watchlistList.removeAllViews();
        if (items == null || items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvCount.setText("0 KOIN · LIVE");
            return;
        }
        tvEmpty.setVisibility(View.GONE);
        tvCount.setText(items.size() + " KOIN · LIVE");

        List<CoinMarket> coinList = cryptoVM.coinList.getValue();
        for (WatchlistEntity w : items) {
            CoinMarket market = null;
            if (coinList != null) {
                for (CoinMarket c : coinList) {
                    if (c.id.equals(w.coinId)) { market = c; break; }
                }
            }
            addWatchlistRow(watchlistList, w, market);
        }
    }

    private void addWatchlistRow(LinearLayout parent, WatchlistEntity w, @Nullable CoinMarket market) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(14), 0, dp(14));

        // Coin icon (image + monogram fallback)
        FrameLayout iconContainer = new FrameLayout(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(36), dp(36));
        iconParams.setMarginEnd(dp(12));
        iconContainer.setLayoutParams(iconParams);

        MonogramView monogram = new MonogramView(requireContext());
        monogram.setSymbol(w.symbol.toUpperCase());
        monogram.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        iconContainer.addView(monogram);

        ImageView coinImg = new ImageView(requireContext());
        coinImg.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        coinImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coinImg.setVisibility(View.GONE);
        iconContainer.addView(coinImg);

        if (market != null && market.image != null && !market.image.isEmpty()) {
            Glide.with(requireContext()).load(market.image).circleCrop().into(coinImg);
            coinImg.setVisibility(View.VISIBLE);
            monogram.setVisibility(View.GONE);
        }

        row.addView(iconContainer);

        // Symbol + name
        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvSym = new TextView(requireContext());
        tvSym.setText(w.symbol.toUpperCase());
        tvSym.setTextColor(getResources().getColor(R.color.frx_text, null));
        tvSym.setTextSize(13.5f);
        tvSym.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
        info.addView(tvSym);

        TextView tvName = new TextView(requireContext());
        tvName.setText(w.name);
        tvName.setTextColor(getResources().getColor(R.color.frx_text_2, null));
        tvName.setTextSize(11);
        info.addView(tvName);
        row.addView(info);

        // Price + change
        if (market != null) {
            LinearLayout priceInfo = new LinearLayout(requireContext());
            priceInfo.setOrientation(LinearLayout.VERTICAL);
            priceInfo.setGravity(Gravity.END);
            LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            priceParams.setMarginEnd(dp(12));
            priceInfo.setLayoutParams(priceParams);

            TextView tvPrice = new TextView(requireContext());
            tvPrice.setText(CurrencyFormatter.formatCompact(market.currentPrice));
            tvPrice.setTextColor(getResources().getColor(R.color.frx_text, null));
            tvPrice.setTextSize(13.5f);
            tvPrice.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
            priceInfo.addView(tvPrice);

            boolean isUp = market.priceChangePercentage24h >= 0;
            TextView tvChange = new TextView(requireContext());
            tvChange.setText(CurrencyFormatter.formatPercent(market.priceChangePercentage24h));
            tvChange.setTextColor(getResources().getColor(isUp ? R.color.frx_up : R.color.frx_down, null));
            tvChange.setTextSize(11);
            priceInfo.addView(tvChange);
            row.addView(priceInfo);
        }

        // Buy button
        TextView btnBuy = new TextView(requireContext());
        btnBuy.setText("Beli");
        btnBuy.setTextColor(0xFF07130C);
        btnBuy.setTextSize(11.5f);
        btnBuy.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
        btnBuy.setBackgroundResource(R.drawable.bg_btn_buy);
        btnBuy.setPadding(dp(14), dp(8), dp(14), dp(8));
        btnBuy.setGravity(Gravity.CENTER);
        btnBuy.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("coinId", w.coinId);
            args.putString("coinSymbol", w.symbol);
            args.putString("coinName", w.name);
            Navigation.findNavController(v).navigate(R.id.coinDetailFragment, args);
        });
        row.addView(btnBuy);

        // Divider
        View divider = new View(requireContext());
        divider.setBackgroundColor(getResources().getColor(R.color.frx_hairline, null));
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divider.setLayoutParams(divParams);

        LinearLayout wrapper = new LinearLayout(requireContext());
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(row);
        wrapper.addView(divider);
        parent.addView(wrapper);
    }

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density); }
}
