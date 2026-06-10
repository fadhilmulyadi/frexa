package com.diellabs.frexa.ui.transaksi;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.util.CurrencyFormatter;
import java.util.*;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
    private List<TradeEntity> items = new ArrayList<>();
    private OnItemClick listener;
    public interface OnItemClick { void onClick(TradeEntity t); }
    public void setData(List<TradeEntity> d) { items = d; notifyDataSetChanged(); }
    public void setListener(OnItemClick l) { listener = l; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_history_trade, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int i) { h.bind(items.get(i)); }
    @Override public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView iv; TextView tvSymbol, tvDirection, tvDur, tvPnl;
        VH(View v) {
            super(v);
            iv = v.findViewById(R.id.iv_coin); tvSymbol = v.findViewById(R.id.tv_symbol);
            tvDirection = v.findViewById(R.id.tv_direction);
            tvDur = v.findViewById(R.id.tv_dur); tvPnl = v.findViewById(R.id.tv_pnl);
        }
        void bind(TradeEntity t) {
            Glide.with(iv).load(t.coinImageUrl).circleCrop().into(iv);
            tvSymbol.setText(t.coinSymbol.toUpperCase());
            tvDirection.setText(t.direction.equals("UP") ? "↑" : "↓");
            tvDur.setText(t.durationLabel);
            tvPnl.setText(t.isWin ? "+" + CurrencyFormatter.formatUsd(t.pnl) : CurrencyFormatter.formatUsd(t.pnl));
            tvPnl.setTextColor(itemView.getContext().getColor(t.isWin ? R.color.frx_up : R.color.frx_down));
            itemView.setOnClickListener(x -> { if (listener != null) listener.onClick(t); });
        }
    }
}
