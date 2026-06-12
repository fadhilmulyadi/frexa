package com.diellabs.frexa.ui.coindetail;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.diellabs.frexa.R;
import com.diellabs.frexa.util.CurrencyFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderResultDialog extends DialogFragment {

    public static final String TYPE_BUY_SUCCESS = "BUY_SUCCESS";
    public static final String TYPE_SELL_SUCCESS = "SELL_SUCCESS";
    public static final String TYPE_FAILED = "FAILED";

    private static final String ARG_TYPE = "type";
    private static final String ARG_SYMBOL = "symbol";
    private static final String ARG_QTY = "qty";
    private static final String ARG_PRICE = "price";
    private static final String ARG_FEE = "fee";
    private static final String ARG_TOTAL = "total";
    private static final String ARG_PNL_PCT = "pnl_pct";
    private static final String ARG_PNL_IDR = "pnl_idr";
    private static final String ARG_TX_ID = "tx_id";
    private static final String ARG_NEEDED = "needed";
    private static final String ARG_AVAILABLE = "available";

    public interface OnResultActionListener {
        void onViewHistory();
    }

    private OnResultActionListener listener;

    public void setOnResultActionListener(OnResultActionListener l) {
        this.listener = l;
    }

    public static OrderResultDialog buySuccess(String symbol, double qty, double price,
            double fee, double total, String txId) {
        OrderResultDialog d = new OrderResultDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, TYPE_BUY_SUCCESS);
        args.putString(ARG_SYMBOL, symbol);
        args.putDouble(ARG_QTY, qty);
        args.putDouble(ARG_PRICE, price);
        args.putDouble(ARG_FEE, fee);
        args.putDouble(ARG_TOTAL, total);
        args.putString(ARG_TX_ID, txId);
        d.setArguments(args);
        return d;
    }

    public static OrderResultDialog sellSuccess(String symbol, double qty, double price,
            double fee, double received, double pnlPct, double pnlIdr, String txId) {
        OrderResultDialog d = new OrderResultDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, TYPE_SELL_SUCCESS);
        args.putString(ARG_SYMBOL, symbol);
        args.putDouble(ARG_QTY, qty);
        args.putDouble(ARG_PRICE, price);
        args.putDouble(ARG_FEE, fee);
        args.putDouble(ARG_TOTAL, received);
        args.putDouble(ARG_PNL_PCT, pnlPct);
        args.putDouble(ARG_PNL_IDR, pnlIdr);
        args.putString(ARG_TX_ID, txId);
        d.setArguments(args);
        return d;
    }

    public static OrderResultDialog buyFailed(double needed, double available) {
        OrderResultDialog d = new OrderResultDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, TYPE_FAILED);
        args.putDouble(ARG_NEEDED, needed);
        args.putDouble(ARG_AVAILABLE, available);
        d.setArguments(args);
        return d;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_order_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        String type = args.getString(ARG_TYPE, TYPE_BUY_SUCCESS);

        View summarySuccess = view.findViewById(R.id.summary_success);
        View summaryFailed = view.findViewById(R.id.summary_failed);
        View errorNote = view.findViewById(R.id.error_note);
        View rowTx = view.findViewById(R.id.row_tx);
        View badgeBg = view.findViewById(R.id.badge_bg);
        ImageView badgeIcon = view.findViewById(R.id.badge_icon);
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_dialog_subtitle);
        TextView btnPrimary = view.findViewById(R.id.btn_dialog_primary);
        TextView tvSecondary = view.findViewById(R.id.tv_secondary_action);

        if (TYPE_BUY_SUCCESS.equals(type) || TYPE_SELL_SUCCESS.equals(type)) {
            boolean isBuy = TYPE_BUY_SUCCESS.equals(type);
            String symbol = args.getString(ARG_SYMBOL, "");
            double qty = args.getDouble(ARG_QTY, 0);
            double price = args.getDouble(ARG_PRICE, 0);
            double fee = args.getDouble(ARG_FEE, 0);
            double total = args.getDouble(ARG_TOTAL, 0);
            String txId = args.getString(ARG_TX_ID, "");

            view.findViewById(R.id.dialog_card).setBackgroundResource(R.drawable.bg_dialog_success);
            badgeBg.setBackgroundResource(R.drawable.bg_badge_success);
            badgeIcon.setImageResource(R.drawable.ic_check_order);

            tvTitle.setText(isBuy ? "Beli Berhasil" : "Jual Berhasil");
            tvSubtitle.setText(isBuy
                ? "Order market " + symbol + " telah dieksekusi dan ditambahkan ke portofolio."
                : CurrencyFormatter.formatCoinQty(qty) + " " + symbol
                    + " telah terjual. Dana masuk ke kas tersedia Anda.");

            summarySuccess.setVisibility(View.VISIBLE);
            summaryFailed.setVisibility(View.GONE);
            errorNote.setVisibility(View.GONE);
            rowTx.setVisibility(View.VISIBLE);

            ((TextView) view.findViewById(R.id.tv_qty_label)).setText(isBuy ? "Jumlah" : "Terjual");
            ((TextView) view.findViewById(R.id.tv_qty_value))
                .setText(CurrencyFormatter.formatCoinQty(qty) + " " + symbol);
            ((TextView) view.findViewById(R.id.tv_exec_price))
                .setText(CurrencyFormatter.formatIdrFull(price));
            ((TextView) view.findViewById(R.id.tv_dialog_fee))
                .setText(CurrencyFormatter.formatIdrFull(fee));
            ((TextView) view.findViewById(R.id.tv_total_label))
                .setText(isBuy ? "Total dibayar" : "Dana diterima");
            ((TextView) view.findViewById(R.id.tv_dialog_total))
                .setText(CurrencyFormatter.formatIdrFull(total));

            if (!isBuy) {
                double pnlPct = args.getDouble(ARG_PNL_PCT, 0);
                double pnlIdr = args.getDouble(ARG_PNL_IDR, 0);
                view.findViewById(R.id.row_pnl).setVisibility(View.VISIBLE);
                TextView tvPnl = view.findViewById(R.id.tv_pnl);
                String sign = pnlIdr >= 0 ? "+" : "−";
                tvPnl.setText(sign + CurrencyFormatter.formatIdrFull(Math.abs(pnlIdr))
                    + " · " + CurrencyFormatter.formatPercent(pnlPct));
                tvPnl.setTextColor(getResources().getColor(
                    pnlIdr >= 0 ? R.color.frx_up : R.color.frx_down, null));
            }

            ((TextView) view.findViewById(R.id.tv_tx_id)).setText("ID #" + txId);
            String time = new SimpleDateFormat("dd MMM · HH:mm z", new Locale("id", "ID"))
                .format(new Date());
            ((TextView) view.findViewById(R.id.tv_tx_time)).setText(time);

            btnPrimary.setText("Selesai");
            btnPrimary.setBackgroundResource(R.drawable.bg_btn_buy);
            btnPrimary.setTextColor(0xFF07130C);
            tvSecondary.setText("Lihat di Riwayat");
            tvSecondary.setTextColor(getResources().getColor(R.color.frx_amber, null));

            btnPrimary.setOnClickListener(v -> dismiss());
            tvSecondary.setOnClickListener(v -> {
                dismiss();
                if (listener != null) listener.onViewHistory();
            });

        } else { // FAILED
            double needed = args.getDouble(ARG_NEEDED, 0);
            double available = args.getDouble(ARG_AVAILABLE, 0);

            view.findViewById(R.id.dialog_card).setBackgroundResource(R.drawable.bg_dialog_failed);
            badgeBg.setBackgroundResource(R.drawable.bg_badge_failed);
            badgeIcon.setImageResource(R.drawable.ic_x_order);

            tvTitle.setText("Transaksi Gagal");
            tvSubtitle.setText("Saldo kas tidak mencukupi untuk menyelesaikan order beli ini.");

            summarySuccess.setVisibility(View.GONE);
            summaryFailed.setVisibility(View.VISIBLE);
            errorNote.setVisibility(View.VISIBLE);
            rowTx.setVisibility(View.GONE);

            ((TextView) view.findViewById(R.id.tv_needed))
                .setText(CurrencyFormatter.formatIdrFull(needed));
            ((TextView) view.findViewById(R.id.tv_available))
                .setText(CurrencyFormatter.formatIdrFull(available));
            ((TextView) view.findViewById(R.id.tv_shortage))
                .setText(CurrencyFormatter.formatIdrFull(needed - available));
            ((TextView) view.findViewById(R.id.tv_error_note)).setText(
                "Kurangi nominal order atau jual sebagian aset untuk menambah kas."
                    + " Kode: ERR_INSUFFICIENT_FUNDS");

            btnPrimary.setText("Coba Lagi");
            btnPrimary.setBackgroundResource(R.drawable.bg_card_amber);
            btnPrimary.setTextColor(0xFF1A1205);
            tvSecondary.setText("Tutup");
            tvSecondary.setTextColor(getResources().getColor(R.color.frx_text_2, null));

            btnPrimary.setOnClickListener(v -> dismiss());
            tvSecondary.setOnClickListener(v -> dismiss());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setDimAmount(0.74f);
        }
    }
}
