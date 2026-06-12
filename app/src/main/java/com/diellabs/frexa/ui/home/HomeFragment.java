package com.diellabs.frexa.ui.home;

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
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import com.diellabs.frexa.data.local.entity.OrderEntity;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.util.ThemeManager;
import com.diellabs.frexa.util.UserPrefs;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.PortfolioViewModel;
import com.diellabs.frexa.viewmodel.TradingViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {
    private CryptoViewModel cryptoVM;
    private PortfolioViewModel portfolioVM;
    private TradingViewModel tradingVM;
    private UserPrefs userPrefs;

    private TextView tvPortfolioValue, tvChange24h, tvCash, tvCryptoValue;
    private LinearLayout allocationList, layoutAllocation, recentOrdersList, strip7day;
    private TextView tvCoinCount, tvNoOrders, tv7daySummary;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cryptoVM = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);
        portfolioVM = new ViewModelProvider(requireActivity()).get(PortfolioViewModel.class);
        tradingVM = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);
        userPrefs = new UserPrefs(requireContext());

        ImageView btnTheme = view.findViewById(R.id.btn_theme);
        btnTheme.setImageResource(userPrefs.isDarkMode() ? R.drawable.ic_sun : R.drawable.ic_moon);
        btnTheme.setOnClickListener(v -> {
            ThemeManager.toggle(userPrefs);
            requireActivity().recreate();
        });

        tvPortfolioValue = view.findViewById(R.id.tv_portfolio_value);
        tvChange24h = view.findViewById(R.id.tv_change_24h);
        tvCash = view.findViewById(R.id.tv_cash);
        tvCryptoValue = view.findViewById(R.id.tv_crypto_value);
        allocationList = view.findViewById(R.id.allocation_list);
        layoutAllocation = view.findViewById(R.id.layout_allocation);
        tvCoinCount = view.findViewById(R.id.tv_coin_count);
        recentOrdersList = view.findViewById(R.id.recent_orders_list);
        tvNoOrders = view.findViewById(R.id.tv_no_orders);
        strip7day = view.findViewById(R.id.strip_7day);
        tv7daySummary = view.findViewById(R.id.tv_7day_summary);

        view.findViewById(R.id.btn_see_all).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.ordersFragment));

        portfolioVM.cashBalance.observe(getViewLifecycleOwner(), balance ->
            tvCash.setText(CurrencyFormatter.formatIdrFull(balance)));

        portfolioVM.holdings.observe(getViewLifecycleOwner(), holdings ->
            updatePortfolioDisplay(holdings, cryptoVM.coinList.getValue()));

        cryptoVM.coinList.observe(getViewLifecycleOwner(), coins -> {
            List<HoldingEntity> holdings = portfolioVM.holdings.getValue();
            if (holdings != null && !holdings.isEmpty()) {
                updatePortfolioDisplay(holdings, coins);
            }
        });

        portfolioVM.unrealizedPnl.observe(getViewLifecycleOwner(), pnl -> {
            double pct = portfolioVM.pnlPercent.getValue() != null ? portfolioVM.pnlPercent.getValue() : 0;
            tvChange24h.setText(CurrencyFormatter.formatChangeIdr(pnl, pct));
            boolean isUp = pnl >= 0;
            tvChange24h.setTextColor(getResources().getColor(isUp ? R.color.frx_up : R.color.frx_down, null));
            tvChange24h.setBackgroundResource(isUp ? R.drawable.bg_chip_up : R.drawable.bg_chip_down);
        });

        tradingVM.orders.observe(getViewLifecycleOwner(), orders -> {
            recentOrdersList.removeAllViews();
            if (orders == null || orders.isEmpty()) {
                tvNoOrders.setVisibility(View.VISIBLE);
                return;
            }
            tvNoOrders.setVisibility(View.GONE);
            int limit = Math.min(3, orders.size());
            for (int i = 0; i < limit; i++) {
                addOrderRow(recentOrdersList, orders.get(i));
            }
        });

        render7DayStrip();
    }

    private void updatePortfolioDisplay(List<HoldingEntity> holdings, List<CoinMarket> coinList) {
        if (holdings == null || holdings.isEmpty()) {
            layoutAllocation.setVisibility(View.GONE);
            tvCryptoValue.setText(CurrencyFormatter.formatIdrFull(0));
            double cash = portfolioVM.cashBalance.getValue() != null ? portfolioVM.cashBalance.getValue() : 0;
            tvPortfolioValue.setText(CurrencyFormatter.formatIdrFull(cash));
            saveDailySnapshot(cash);
            return;
        }

        // Build price map from live data
        Map<String, Double> priceMap = new HashMap<>();
        Map<String, String> imageMap = new HashMap<>();
        if (coinList != null) {
            for (CoinMarket c : coinList) {
                priceMap.put(c.id, c.currentPrice);
                if (c.image != null) imageMap.put(c.id, c.image);
            }
        }

        double totalCrypto = 0;
        double totalCost = 0;
        Map<String, double[]> allocationMap = new LinkedHashMap<>(); // symbol → [value, pct placeholder]

        for (HoldingEntity h : holdings) {
            totalCost += h.totalCostBasis;
            double livePrice = priceMap.containsKey(h.coinId) ? priceMap.get(h.coinId) : h.avgBuyPrice;
            double liveValue = h.quantity * livePrice;
            totalCrypto += liveValue;
            allocationMap.put(h.symbol, new double[]{liveValue, h.coinId.hashCode()});
        }

        layoutAllocation.setVisibility(View.VISIBLE);
        tvCoinCount.setText(holdings.size() + " KOIN");
        allocationList.removeAllViews();

        for (Map.Entry<String, double[]> entry : allocationMap.entrySet()) {
            double pct = totalCrypto > 0 ? (entry.getValue()[0] / totalCrypto) * 100 : 0;
            // Find coinId for this symbol
            String coinId = "";
            for (HoldingEntity h : holdings) {
                if (h.symbol.equalsIgnoreCase(entry.getKey())) { coinId = h.coinId; break; }
            }
            String imgUrl = imageMap.get(coinId);
            addAllocationRow(allocationList, entry.getKey(), pct, imgUrl);
        }

        portfolioVM.updatePortfolioValue(totalCrypto, totalCost);
        tvCryptoValue.setText(CurrencyFormatter.formatIdrFull(totalCrypto));
        double cash = portfolioVM.cashBalance.getValue() != null ? portfolioVM.cashBalance.getValue() : 0;
        double total = totalCrypto + cash;
        tvPortfolioValue.setText(CurrencyFormatter.formatIdrFull(total));
        saveDailySnapshot(total);
    }

    private void saveDailySnapshot(double total) {
        userPrefs.saveTodaySnapshot(total);
        render7DayStrip();
    }

    private void render7DayStrip() {
        if (strip7day == null) return;
        strip7day.removeAllViews();

        Map<String, Double> snapshots = userPrefs.getDailySnapshots();

        // Build last 7 calendar days
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEE", new Locale("id", "ID"));
        SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        List<String> dayKeys = new ArrayList<>();
        List<String> dayLabels = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -i);
            dayKeys.add(keyFmt.format(c.getTime()));
            dayLabels.add(dayFmt.format(c.getTime()).toUpperCase(Locale.US).replace(".", ""));
        }

        double totalChangeSum = 0;
        int validDays = 0;

        for (int i = 0; i < 7; i++) {
            String key = dayKeys.get(i);
            String prevKey = i > 0 ? dayKeys.get(i - 1) : null;
            Double val = snapshots.get(key);
            Double prevVal = prevKey != null ? snapshots.get(prevKey) : null;

            double changePct = 0;
            if (val != null && prevVal != null && prevVal > 0) {
                changePct = (val - prevVal) / prevVal * 100;
                totalChangeSum += changePct;
                validDays++;
            }

            boolean isUp = changePct >= 0;
            boolean isToday = i == 6;
            String label = dayLabels.get(i);

            LinearLayout cell = new LinearLayout(requireContext());
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            cellParams.setMargins(dp(3), 0, dp(3), 0);
            cell.setLayoutParams(cellParams);

            int bgColor = getResources().getColor(isUp ? R.color.frx_up_12 : R.color.frx_down_12, null);
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(dp(10));
            bg.setColor(bgColor);
            if (isToday) bg.setStroke(dp(1), getResources().getColor(R.color.frx_amber_22, null));
            cell.setBackground(bg);
            cell.setPadding(dp(4), dp(9), dp(4), dp(9));

            TextView tvDay = new TextView(requireContext());
            tvDay.setText(label);
            tvDay.setTextSize(9);
            tvDay.setGravity(Gravity.CENTER);
            tvDay.setTextColor(isToday
                ? getResources().getColor(R.color.frx_amber, null)
                : getResources().getColor(R.color.frx_text_3, null));
            tvDay.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
            cell.addView(tvDay);

            TextView tvVal = new TextView(requireContext());
            tvVal.setGravity(Gravity.CENTER);
            tvVal.setTextSize(10.5f);
            tvVal.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
            if (val == null && prevVal == null) {
                tvVal.setText("--");
                tvVal.setTextColor(getResources().getColor(R.color.frx_text_3, null));
            } else {
                tvVal.setText(String.format(Locale.US, "%+.1f", changePct));
                tvVal.setTextColor(getResources().getColor(isUp ? R.color.frx_up : R.color.frx_down, null));
            }
            tvVal.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams valParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            valParams.topMargin = dp(3);
            tvVal.setLayoutParams(valParams);
            cell.addView(tvVal);

            strip7day.addView(cell);
        }

        // Summary
        if (validDays > 0) {
            boolean sumUp = totalChangeSum >= 0;
            tv7daySummary.setText(String.format(Locale.US, "%+.1f%% / minggu", totalChangeSum));
            tv7daySummary.setTextColor(getResources().getColor(sumUp ? R.color.frx_up : R.color.frx_down, null));
        } else {
            tv7daySummary.setText("");
        }
    }

    private void addAllocationRow(LinearLayout parent, String symbol, double pct, @Nullable String imageUrl) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));

        // Header row: icon + symbol + pct
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Coin icon
        FrameLayout iconFrame = new FrameLayout(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        iconParams.setMarginEnd(dp(8));
        iconFrame.setLayoutParams(iconParams);

        ImageView coinImg = new ImageView(requireContext());
        coinImg.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        coinImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iconFrame.addView(coinImg);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext()).load(imageUrl).circleCrop().into(coinImg);
        } else {
            coinImg.setImageDrawable(null);
        }
        header.addView(iconFrame);

        TextView tvSymbol = new TextView(requireContext());
        tvSymbol.setText(symbol.toUpperCase());
        tvSymbol.setTextColor(getResources().getColor(R.color.frx_text, null));
        tvSymbol.setTextSize(12);
        tvSymbol.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
        header.addView(tvSymbol, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvPct = new TextView(requireContext());
        tvPct.setText(String.format(Locale.US, "%.0f%%", pct));
        tvPct.setTextColor(getResources().getColor(R.color.frx_text_2, null));
        tvPct.setTextSize(11.5f);
        header.addView(tvPct);

        row.addView(header);

        // Progress bar using horizontal LinearLayout with weights
        LinearLayout barLayout = new LinearLayout(requireContext());
        barLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams barLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(4));
        barLayoutParams.topMargin = dp(6);
        barLayout.setLayoutParams(barLayoutParams);

        // Round the bar container
        android.graphics.drawable.GradientDrawable barBgDrawable = new android.graphics.drawable.GradientDrawable();
        barBgDrawable.setCornerRadius(dp(99));
        barBgDrawable.setColor(getResources().getColor(R.color.frx_surface, null));
        barLayout.setBackground(barBgDrawable);
        barLayout.setClipToOutline(true);

        int barColor = getBarColor(symbol);
        View barFilled = new View(requireContext());
        barFilled.setBackgroundColor(barColor);
        float filledWeight = (float) Math.max(pct, 1);
        float emptyWeight = (float) Math.max(100 - pct, 0);
        barLayout.addView(barFilled, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, filledWeight));

        if (emptyWeight > 0) {
            View barEmpty = new View(requireContext());
            barEmpty.setBackgroundColor(getResources().getColor(R.color.frx_surface, null));
            barLayout.addView(barEmpty, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, emptyWeight));
        }

        row.addView(barLayout);
        parent.addView(row);
    }

    private int getBarColor(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTC":  return 0xFFE8B25A;
            case "ETH":  return 0xFF8AA7C4;
            case "SOL":  return 0xFF3DD68C;
            case "BNB":  return 0xFFF3BA2F;
            case "AVAX": return 0xFFE84142;
            default:     return 0xFF5C666F;
        }
    }

    private void addOrderRow(LinearLayout parent, OrderEntity order) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));

        TextView chip = new TextView(requireContext());
        boolean isBuy = "BUY".equals(order.type);
        chip.setText(isBuy ? "BELI" : "JUAL");
        chip.setTextSize(10);
        chip.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
        chip.setTextColor(getResources().getColor(isBuy ? R.color.frx_up : R.color.frx_down, null));
        chip.setBackgroundResource(isBuy ? R.drawable.bg_chip_up : R.drawable.bg_chip_down);
        chip.setPadding(dp(7), dp(4), dp(7), dp(4));
        row.addView(chip);

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.HORIZONTAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.setMargins(dp(12), 0, 0, 0);
        info.setLayoutParams(infoParams);

        TextView tvMain = new TextView(requireContext());
        tvMain.setText(order.symbol + " · " + CurrencyFormatter.formatCoinQty(order.quantity) + " @ " + CurrencyFormatter.formatCompact(order.pricePerCoin));
        tvMain.setTextColor(getResources().getColor(R.color.frx_text, null));
        tvMain.setTextSize(13);
        info.addView(tvMain);
        row.addView(info);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", new Locale("id", "ID"));
        TextView tvTime = new TextView(requireContext());
        tvTime.setText(sdf.format(new Date(order.timestamp)));
        tvTime.setTextColor(getResources().getColor(R.color.frx_text_3, null));
        tvTime.setTextSize(10.5f);
        row.addView(tvTime);

        // Divider
        View divider = new View(requireContext());
        divider.setBackgroundColor(getResources().getColor(R.color.frx_hairline, null));

        LinearLayout wrapper = new LinearLayout(requireContext());
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(row);
        wrapper.addView(divider, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        parent.addView(wrapper);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
