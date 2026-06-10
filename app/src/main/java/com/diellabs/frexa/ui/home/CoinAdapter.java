package com.diellabs.frexa.ui.home;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.util.CurrencyFormatter;
import java.util.*;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.VH> {
    private List<CoinMarket> items = new ArrayList<>();
    private final OnCoinAction listener;

    public interface OnCoinAction {
        void onRowClick(CoinMarket coin);
        void onInfoClick(CoinMarket coin);
    }
    public CoinAdapter(OnCoinAction l) { listener = l; }
    public void setData(List<CoinMarket> d) { items = d; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_coin, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int i) { h.bind(items.get(i)); }
    @Override public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView iv; TextView tvName, tvSymbol, tvPrice, tvChange; ImageButton btnInfo;
        VH(View v) {
            super(v);
            iv = v.findViewById(R.id.iv_coin);
            tvName = v.findViewById(R.id.tv_name); tvSymbol = v.findViewById(R.id.tv_symbol);
            tvPrice = v.findViewById(R.id.tv_price); tvChange = v.findViewById(R.id.tv_change);
            btnInfo = v.findViewById(R.id.btn_info);
        }
        void bind(CoinMarket c) {
            Glide.with(iv).load(c.image).circleCrop().into(iv);
            tvName.setText(c.name); tvSymbol.setText(c.symbol.toUpperCase());
            tvPrice.setText(CurrencyFormatter.formatUsd(c.currentPrice));
            double chg = c.priceChangePercentage24h;
            tvChange.setText(CurrencyFormatter.formatPercent(chg));
            tvChange.setTextColor(itemView.getContext().getColor(chg >= 0 ? R.color.frx_up : R.color.frx_down));
            itemView.setOnClickListener(x -> listener.onRowClick(c));
            btnInfo.setOnClickListener(x -> listener.onInfoClick(c));
        }
    }
}
