package com.diellabs.frexa.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.util.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;

public class MoverAdapter extends RecyclerView.Adapter<MoverAdapter.VH> {
    private List<CoinMarket> items = new ArrayList<>();
    private final OnCoinClick listener;

    public interface OnCoinClick { void onClick(CoinMarket coin); }
    public MoverAdapter(OnCoinClick l) { listener = l; }

    public void setData(List<CoinMarket> data) { items = data; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_mover, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) { h.bind(items.get(i)); }
    @Override public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView ivCoin; TextView tvSymbol, tvPrice, tvChange;
        VH(View v) {
            super(v);
            ivCoin = v.findViewById(R.id.iv_coin);
            tvSymbol = v.findViewById(R.id.tv_symbol);
            tvPrice = v.findViewById(R.id.tv_price);
            tvChange = v.findViewById(R.id.tv_change);
        }
        void bind(CoinMarket c) {
            Glide.with(ivCoin).load(c.image).circleCrop().into(ivCoin);
            tvSymbol.setText(c.symbol.toUpperCase());
            tvPrice.setText(CurrencyFormatter.formatUsd(c.currentPrice));
            double chg = c.priceChangePercentage24h;
            tvChange.setText(CurrencyFormatter.formatPercent(chg));
            tvChange.setTextColor(itemView.getContext().getColor(chg >= 0 ? R.color.frx_up : R.color.frx_down));
            itemView.setOnClickListener(x -> listener.onClick(c));
        }
    }
}
