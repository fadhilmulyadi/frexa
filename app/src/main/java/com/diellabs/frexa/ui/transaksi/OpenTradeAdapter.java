package com.diellabs.frexa.ui.transaksi;

import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.util.CurrencyFormatter;
import java.util.*;

public class OpenTradeAdapter extends RecyclerView.Adapter<OpenTradeAdapter.VH> {
    private List<TradeEntity> items = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void setData(List<TradeEntity> d) { items = d; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_open_trade, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int i) { h.bind(items.get(i)); }
    @Override public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView iv; TextView tvSymbol, tvDirection, tvCountdown, tvStake;
        Runnable countdownTask;
        VH(View v) {
            super(v);
            iv = v.findViewById(R.id.iv_coin); tvSymbol = v.findViewById(R.id.tv_symbol);
            tvDirection = v.findViewById(R.id.tv_direction);
            tvCountdown = v.findViewById(R.id.tv_countdown); tvStake = v.findViewById(R.id.tv_stake);
        }
        void bind(TradeEntity t) {
            Glide.with(iv).load(t.coinImageUrl).circleCrop().into(iv);
            tvSymbol.setText(t.coinSymbol.toUpperCase());
            tvDirection.setText(t.direction.equals("UP") ? "↑ NAIK" : "↓ TURUN");
            tvDirection.setTextColor(itemView.getContext().getColor(
                t.direction.equals("UP") ? R.color.frx_up : R.color.frx_down));
            tvStake.setText(CurrencyFormatter.formatUsd(t.stakeAmount));
            startCountdown(t);
        }
        void startCountdown(TradeEntity t) {
            if (countdownTask != null) handler.removeCallbacks(countdownTask);
            countdownTask = new Runnable() {
                @Override public void run() {
                    long rem = Math.max(0, t.closeTime - System.currentTimeMillis()) / 1000;
                    tvCountdown.setText(String.format(java.util.Locale.US, "%02d:%02d", rem/60, rem%60));
                    if (rem > 0) handler.postDelayed(this, 500);
                }
            };
            handler.post(countdownTask);
        }
    }
}
