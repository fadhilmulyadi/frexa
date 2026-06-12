package com.diellabs.frexa.ui.orders;

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
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.local.entity.OrderEntity;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.viewmodel.TradingViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrdersFragment extends Fragment {
    private TradingViewModel tradingVM;
    private String currentFilter = "all";
    private List<OrderEntity> allOrders = new ArrayList<>();
    private LinearLayout ordersList;
    private TextView tvTotalOrders, tvWinRate, tvBestTrade;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tradingVM = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);

        ordersList = view.findViewById(R.id.orders_list);
        tvTotalOrders = view.findViewById(R.id.tv_total_orders);
        tvWinRate = view.findViewById(R.id.tv_win_rate);
        tvBestTrade = view.findViewById(R.id.tv_best_trade);

        TextView filterAll = view.findViewById(R.id.filter_all);
        TextView filterBuy = view.findViewById(R.id.filter_buy);
        TextView filterSell = view.findViewById(R.id.filter_sell);

        filterAll.setOnClickListener(v -> { currentFilter = "all"; updateFilterPills(filterAll, filterBuy, filterSell); renderList(); });
        filterBuy.setOnClickListener(v -> { currentFilter = "buy"; updateFilterPills(filterAll, filterBuy, filterSell); renderList(); });
        filterSell.setOnClickListener(v -> { currentFilter = "sell"; updateFilterPills(filterAll, filterBuy, filterSell); renderList(); });

        tradingVM.orders.observe(getViewLifecycleOwner(), orders -> {
            allOrders = orders != null ? orders : new ArrayList<>();
            renderList();
            tradingVM.loadStats((total, winRate, bestTrade) -> {
                tvWinRate.setText(winRate > 0 ? String.format(Locale.US, "%.0f%%", winRate) : "--");
                tvWinRate.setTextColor(getResources().getColor(winRate >= 50 ? R.color.frx_up : R.color.frx_down, null));
                tvBestTrade.setText(bestTrade > 0 ? String.format(Locale.US, "+%.1f%%", bestTrade) : "--");
                tvBestTrade.setTextColor(getResources().getColor(bestTrade > 0 ? R.color.frx_up : R.color.frx_text, null));
            });
        });
    }

    private void renderList() {
        ordersList.removeAllViews();

        List<OrderEntity> filtered = new ArrayList<>();
        for (OrderEntity o : allOrders) {
            if ("all".equals(currentFilter) ||
                ("buy".equals(currentFilter) && "BUY".equals(o.type)) ||
                ("sell".equals(currentFilter) && "SELL".equals(o.type))) {
                filtered.add(o);
            }
        }

        tvTotalOrders.setText(String.valueOf(allOrders.size()));

        String lastDate = "";
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM", new Locale("id", "ID"));
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss", new Locale("id", "ID"));

        for (OrderEntity o : filtered) {
            String dateStr = dateFmt.format(new Date(o.timestamp)).toUpperCase(Locale.US);
            if (!dateStr.equals(lastDate)) {
                lastDate = dateStr;
                TextView header = new TextView(requireContext());
                header.setText(dateStr);
                header.setTextColor(getResources().getColor(R.color.frx_text_3, null));
                header.setTextSize(10);
                header.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
                header.setPadding(0, dp(14), 0, dp(4));
                ordersList.addView(header);
            }
            addOrderRow(ordersList, o, timeFmt);
        }
    }

    private void updateFilterPills(TextView all, TextView buy, TextView sell) {
        all.setBackgroundResource("all".equals(currentFilter) ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        all.setTextColor("all".equals(currentFilter) ? 0xFF1A1205 : getResources().getColor(R.color.frx_text_2, null));
        buy.setBackgroundResource("buy".equals(currentFilter) ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        buy.setTextColor("buy".equals(currentFilter) ? 0xFF1A1205 : getResources().getColor(R.color.frx_text_2, null));
        sell.setBackgroundResource("sell".equals(currentFilter) ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        sell.setTextColor("sell".equals(currentFilter) ? 0xFF1A1205 : getResources().getColor(R.color.frx_text_2, null));
    }

    private void addOrderRow(LinearLayout parent, OrderEntity o, SimpleDateFormat timeFmt) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));

        TextView chip = new TextView(requireContext());
        boolean isBuy = "BUY".equals(o.type);
        chip.setText(isBuy ? "BELI" : "JUAL");
        chip.setTextSize(10);
        chip.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
        chip.setTextColor(getResources().getColor(isBuy ? R.color.frx_up : R.color.frx_down, null));
        chip.setBackgroundResource(isBuy ? R.drawable.bg_chip_up : R.drawable.bg_chip_down);
        chip.setPadding(dp(7), dp(4), dp(7), dp(4));
        row.addView(chip);

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.setMargins(dp(12), 0, 0, 0);
        info.setLayoutParams(infoParams);

        TextView tvSym = new TextView(requireContext());
        tvSym.setText(o.symbol.toUpperCase());
        tvSym.setTextColor(getResources().getColor(R.color.frx_text, null));
        tvSym.setTextSize(13.5f);
        tvSym.setTypeface(android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD));
        info.addView(tvSym);

        TextView tvDetail = new TextView(requireContext());
        tvDetail.setText(CurrencyFormatter.formatCoinQty(o.quantity) + " @ " + CurrencyFormatter.formatCompact(o.pricePerCoin) + " · fee " + CurrencyFormatter.formatCompact(o.fee));
        tvDetail.setTextColor(getResources().getColor(R.color.frx_text_2, null));
        tvDetail.setTextSize(10.5f);
        info.addView(tvDetail);
        row.addView(info);

        LinearLayout right = new LinearLayout(requireContext());
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);

        TextView tvTotal = new TextView(requireContext());
        tvTotal.setText(CurrencyFormatter.formatIdr(o.totalIdr));
        tvTotal.setTextColor(getResources().getColor(R.color.frx_text, null));
        tvTotal.setTextSize(13);
        right.addView(tvTotal);

        TextView tvTime = new TextView(requireContext());
        tvTime.setText(timeFmt.format(new Date(o.timestamp)));
        tvTime.setTextColor(getResources().getColor(R.color.frx_text_3, null));
        tvTime.setTextSize(10);
        right.addView(tvTime);
        row.addView(right);

        parent.addView(row);
    }

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density); }
}
