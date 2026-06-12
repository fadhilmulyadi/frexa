package com.diellabs.frexa.ui.markets;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.ui.custom.MonogramView;
import com.diellabs.frexa.ui.custom.RangeMeterView;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class MarketsFragment extends Fragment {
    private CryptoViewModel cryptoVM;
    private CoinMarketAdapter adapter;
    private String currentTab = "gainers";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cryptoVM = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_coins);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CoinMarketAdapter();
        rv.setAdapter(adapter);

        TextView tvBtcDom = view.findViewById(R.id.tv_btc_dominance);
        TextView tvFearGreed = view.findViewById(R.id.tv_fear_greed);
        TextView tvFearGreedLabel = view.findViewById(R.id.tv_fear_greed_label);
        TextView tvGlobalMcap = view.findViewById(R.id.tv_global_mcap);
        TextView tvTimestamp = view.findViewById(R.id.tv_timestamp);

        TextView tabGainers = view.findViewById(R.id.tab_gainers);
        TextView tabLosers = view.findViewById(R.id.tab_losers);
        TextView tabVolume = view.findViewById(R.id.tab_volume);

        tabGainers.setOnClickListener(v -> switchTab("gainers", tabGainers, tabLosers, tabVolume));
        tabLosers.setOnClickListener(v -> switchTab("losers", tabGainers, tabLosers, tabVolume));
        tabVolume.setOnClickListener(v -> switchTab("volume", tabGainers, tabLosers, tabVolume));

        // Initial tab UI setup
        switchTab(currentTab, tabGainers, tabLosers, tabVolume);

        cryptoVM.fetchMarkets();
        cryptoVM.fetchGlobalData();
        cryptoVM.fetchFearGreed();

        cryptoVM.coinList.observe(getViewLifecycleOwner(), coins -> {
            cryptoVM.computeTopMovers();
            tvTimestamp.setText("DATA PER " + new SimpleDateFormat("HH:mm:ss", new Locale("id", "ID")).format(new Date()) + " WIB");
            if ("volume".equals(currentTab)) {
                switchTab(currentTab, tabGainers, tabLosers, tabVolume);
            }
        });

        cryptoVM.topGainers.observe(getViewLifecycleOwner(), gainers -> {
            if ("gainers".equals(currentTab)) adapter.submit(gainers);
        });

        cryptoVM.topLosers.observe(getViewLifecycleOwner(), losers -> {
            if ("losers".equals(currentTab)) adapter.submit(losers);
        });

        cryptoVM.globalData.observe(getViewLifecycleOwner(), gd -> {
            if (gd != null && gd.data != null) {
                Double btcPct = gd.data.marketCapPercentage != null ? gd.data.marketCapPercentage.get("btc") : null;
                if (btcPct != null) tvBtcDom.setText(String.format(Locale.US, "%.1f%%", btcPct));

                Double mcap = gd.data.totalMarketCap != null ? gd.data.totalMarketCap.get("idr") : null;
                if (mcap != null) tvGlobalMcap.setText(CurrencyFormatter.formatCompact(mcap));
            }
        });

        cryptoVM.fearGreedValue.observe(getViewLifecycleOwner(), val -> {
            tvFearGreed.setText(String.valueOf(val));
        });

        cryptoVM.fearGreedLabel.observe(getViewLifecycleOwner(), label -> {
            tvFearGreedLabel.setText(label);
        });
    }

    private void switchTab(String tab, TextView g, TextView l, TextView v) {
        currentTab = tab;
        int active = getResources().getColor(R.color.frx_text, null);
        int inactive = getResources().getColor(R.color.frx_text_3, null);

        g.setTextColor("gainers".equals(tab) ? active : inactive);
        l.setTextColor("losers".equals(tab) ? active : inactive);
        v.setTextColor("volume".equals(tab) ? active : inactive);

        if ("gainers".equals(tab) && cryptoVM.topGainers.getValue() != null) {
            adapter.submit(cryptoVM.topGainers.getValue());
        } else if ("losers".equals(tab) && cryptoVM.topLosers.getValue() != null) {
            adapter.submit(cryptoVM.topLosers.getValue());
        } else if ("volume".equals(tab) && cryptoVM.coinList.getValue() != null) {
            List<CoinMarket> sorted = new ArrayList<>(cryptoVM.coinList.getValue());
            Collections.sort(sorted, (a, b) -> Double.compare(b.totalVolume, a.totalVolume));
            adapter.submit(sorted.subList(0, Math.min(20, sorted.size())));
        }
    }

    static class CoinMarketAdapter extends RecyclerView.Adapter<CoinMarketAdapter.VH> {
        private List<CoinMarket> items = new ArrayList<>();

        void submit(List<CoinMarket> list) {
            items = list != null ? new ArrayList<>(list) : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coin_market, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            CoinMarket c = items.get(position);
            h.monogram.setSymbol(c.symbol.toUpperCase());

            if (c.image != null && !c.image.isEmpty()) {
                h.coinImage.setVisibility(View.VISIBLE);
                Glide.with(h.itemView.getContext())
                    .load(c.image)
                    .circleCrop()
                    .into(h.coinImage);
                h.monogram.setVisibility(View.GONE);
            } else {
                h.coinImage.setVisibility(View.GONE);
                h.monogram.setVisibility(View.VISIBLE);
            }

            h.tvSymbol.setText(c.symbol.toUpperCase());
            h.tvName.setText(c.name);
            h.tvPrice.setText(CurrencyFormatter.formatCompact(c.currentPrice));

            boolean isUp = c.priceChangePercentage24h >= 0;
            h.tvChange.setText(CurrencyFormatter.formatPercent(c.priceChangePercentage24h));
            h.tvChange.setTextColor(h.itemView.getContext().getResources().getColor(
                isUp ? R.color.frx_up : R.color.frx_down, null));

            h.rangeMeter.setData(c.low24h, c.high24h, c.currentPrice);

            h.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("coinId", c.id);
                args.putString("coinSymbol", c.symbol);
                args.putString("coinName", c.name);
                Navigation.findNavController(v).navigate(R.id.coinDetailFragment, args);
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            MonogramView monogram;
            ImageView coinImage;
            TextView tvSymbol, tvName, tvPrice, tvChange;
            RangeMeterView rangeMeter;

            VH(View v) {
                super(v);
                monogram = v.findViewById(R.id.monogram);
                coinImage = v.findViewById(R.id.coin_image);
                tvSymbol = v.findViewById(R.id.tv_symbol);
                tvName = v.findViewById(R.id.tv_name);
                tvPrice = v.findViewById(R.id.tv_price);
                tvChange = v.findViewById(R.id.tv_change);
                rangeMeter = v.findViewById(R.id.range_meter);
            }
        }
    }
}
