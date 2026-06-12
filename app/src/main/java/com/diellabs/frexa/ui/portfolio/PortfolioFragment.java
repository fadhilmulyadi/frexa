package com.diellabs.frexa.ui.portfolio;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.viewmodel.PortfolioViewModel;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.util.*;

public class PortfolioFragment extends Fragment {
    private PortfolioViewModel portfolioVM;
    private CryptoViewModel cryptoVM;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        portfolioVM = new ViewModelProvider(requireActivity()).get(PortfolioViewModel.class);
        cryptoVM = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);

        TextView tvCostBasis = view.findViewById(R.id.tv_cost_basis);
        TextView tvCurrentValue = view.findViewById(R.id.tv_current_value);
        TextView tvPnl = view.findViewById(R.id.tv_pnl);
        TextView tvAssetCount = view.findViewById(R.id.tv_asset_count);
        LinearLayout holdingsList = view.findViewById(R.id.holdings_list);
        TextView tvNoHoldings = view.findViewById(R.id.tv_no_holdings);

        cryptoVM.coinList.observe(getViewLifecycleOwner(), coins -> {
            refreshList(holdingsList, tvNoHoldings, tvAssetCount, tvCostBasis, tvCurrentValue, tvPnl);
        });

        portfolioVM.holdings.observe(getViewLifecycleOwner(), holdings -> {
            refreshList(holdingsList, tvNoHoldings, tvAssetCount, tvCostBasis, tvCurrentValue, tvPnl);
        });

        cryptoVM.fetchMarkets();
    }

    private void refreshList(LinearLayout holdingsList, TextView tvNoHoldings, TextView tvAssetCount,
                             TextView tvCostBasis, TextView tvCurrentValue, TextView tvPnl) {
        List<HoldingEntity> holdings = portfolioVM.holdings.getValue();
        List<CoinMarket> coinList = cryptoVM.coinList.getValue();

        holdingsList.removeAllViews();
        if (holdings == null || holdings.isEmpty()) {
            tvNoHoldings.setVisibility(View.VISIBLE);
            tvAssetCount.setText("Aset · 0");
            tvCostBasis.setText(CurrencyFormatter.formatIdr(0));
            tvCurrentValue.setText(CurrencyFormatter.formatIdr(0));
            return;
        }

        tvNoHoldings.setVisibility(View.GONE);
        tvAssetCount.setText("Aset · " + holdings.size());

        Map<String, Double> priceMap = new HashMap<>();
        Map<String, String> imageMap = new HashMap<>();
        if (coinList != null) {
            for (CoinMarket c : coinList) {
                priceMap.put(c.id, c.currentPrice);
                imageMap.put(c.id, c.image);
            }
        }

        double totalCost = 0;
        double totalValue = 0;

        for (HoldingEntity h : holdings) {
            totalCost += h.totalCostBasis;
            double livePrice = priceMap.containsKey(h.coinId) ? priceMap.get(h.coinId) : h.avgBuyPrice;
            double liveValue = h.quantity * livePrice;
            totalValue += liveValue;
            addHoldingRow(holdingsList, h, imageMap.get(h.coinId), liveValue);
        }

        tvCostBasis.setText(CurrencyFormatter.formatIdrFull(totalCost));
        tvCurrentValue.setText(CurrencyFormatter.formatIdrFull(totalValue));

        double pnl = totalValue - totalCost;
        double pnlPct = totalCost > 0 ? (pnl / totalCost) * 100 : 0;
        boolean isUp = pnl >= 0;
        tvPnl.setText(CurrencyFormatter.formatChangeIdr(pnl, pnlPct));
        tvPnl.setTextColor(getResources().getColor(isUp ? R.color.frx_up : R.color.frx_down, null));
    }

    private void addHoldingRow(LinearLayout parent, HoldingEntity h, @Nullable String imageUrl, double liveValue) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));

        FrameLayout iconFrame = new FrameLayout(requireContext());
        iconFrame.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));

        ImageView coinImg = new ImageView(requireContext());
        coinImg.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        coinImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iconFrame.addView(coinImg);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext()).load(imageUrl).circleCrop().into(coinImg);
        } else {
            coinImg.setImageDrawable(null);
        }
        row.addView(iconFrame);

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.setMargins(dp(12), 0, 0, 0);
        info.setLayoutParams(infoParams);

        TextView tvSym = new TextView(requireContext());
        tvSym.setText(h.symbol.toUpperCase());
        tvSym.setTextColor(getResources().getColor(R.color.frx_text, null));
        tvSym.setTextSize(13.5f);
        tvSym.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
        info.addView(tvSym);

        TextView tvDetail = new TextView(requireContext());
        tvDetail.setText(CurrencyFormatter.formatCoinQty(h.quantity) + " · avg " + CurrencyFormatter.formatCompact(h.avgBuyPrice));
        tvDetail.setTextColor(getResources().getColor(R.color.frx_text_2, null));
        tvDetail.setTextSize(10.5f);
        info.addView(tvDetail);
        row.addView(info);

        LinearLayout right = new LinearLayout(requireContext());
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);

        TextView tvVal = new TextView(requireContext());
        tvVal.setText(CurrencyFormatter.formatIdr(liveValue));
        tvVal.setTextColor(getResources().getColor(R.color.frx_text, null));
        tvVal.setTextSize(13.5f);
        right.addView(tvVal);
        row.addView(right);

        row.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("coinId", h.coinId);
            args.putString("coinSymbol", h.symbol);
            args.putString("coinName", h.name);
            Navigation.findNavController(v).navigate(R.id.coinDetailFragment, args);
        });

        parent.addView(row);
    }

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density); }
}
