package com.diellabs.frexa.ui.tradedetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.diellabs.frexa.MainActivity;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.local.entity.TradeEntity;
import com.diellabs.frexa.databinding.ActivityTradeDetailBinding;
import com.diellabs.frexa.util.CurrencyFormatter;
import java.text.SimpleDateFormat;
import java.util.*;

public class TradeDetailActivity extends AppCompatActivity {
    private ActivityTradeDetailBinding b;
    private TradeDetailViewModel vm;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTradeDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setSupportActionBar(b.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vm = new ViewModelProvider(this).get(TradeDetailViewModel.class);
        String tradeId = getIntent().getStringExtra("trade_id");
        if (tradeId != null) vm.loadTrade(tradeId);

        vm.tradeDetail.observe(this, this::bindTrade);
        vm.chartData.observe(this, chart -> {
            if (vm.tradeDetail.getValue() != null) {
                TradeEntity t = vm.tradeDetail.getValue();
                b.miniChart.setData(chart.prices, t.entryPrice, t.exitPrice, t.isWin);
            }
        });

        vm.aiAnalysis.observe(this, text -> {
            b.cardAi.setVisibility(View.VISIBLE);
            b.tvAiResult.setText(text);
        });
        vm.aiLoading.observe(this, loading -> b.btnAi.setEnabled(!loading));
    }

    private void bindTrade(TradeEntity t) {
        b.btnAi.setOnClickListener(x -> vm.analyzeWithAI(t));
        b.btnShowChart.setOnClickListener(x -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("open_terminal", t.coinId);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        String[][] rows = {
            {"Nominal", CurrencyFormatter.formatUsd(t.stakeAmount)},
            {"PnL", (t.isWin ? "+" : "") + CurrencyFormatter.formatUsd(t.pnl)},
            {"ID Transaksi", t.id},
            {"Status", t.isWin ? "Menang" : "Kalah"},
            {"Durasi", t.durationLabel},
            {"Waktu buka", sdf.format(new Date(t.openTime))},
            {"Waktu tutup", sdf.format(new Date(t.closeTime))},
            {"Kuotasi pembukaan", CurrencyFormatter.formatUsd(t.entryPrice)},
            {"Kuotasi penutupan", CurrencyFormatter.formatUsd(t.exitPrice)},
        };

        b.detailRows.removeAllViews();
        for (String[] row : rows) {
            View rowView = getLayoutInflater().inflate(R.layout.item_detail_row, b.detailRows, false);
            ((TextView) rowView.findViewById(R.id.tv_key)).setText(row[0]);
            TextView tvVal = rowView.findViewById(R.id.tv_value);
            tvVal.setText(row[1]);
            if (row[0].equals("PnL")) {
                tvVal.setTextColor(getColor(t.isWin ? R.color.frx_up : R.color.frx_down));
            }
            b.detailRows.addView(rowView);
        }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
