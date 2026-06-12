package com.diellabs.frexa.ui.coindetail;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.local.entity.HoldingEntity;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import com.diellabs.frexa.viewmodel.PortfolioViewModel;
import com.diellabs.frexa.viewmodel.TradingViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.List;

public class OrderBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String ARG_COIN_ID = "coinId";
    private static final String ARG_SYMBOL = "symbol";
    private static final String ARG_NAME = "name";
    private static final String ARG_TYPE = "type";

    private double currentPrice = 0;
    private OrderResultListener listener;

    public interface OrderResultListener {
        void onBuySuccess(String symbol, double qty, double price, double fee, double total, String txId);
        void onSellSuccess(String symbol, double qty, double price, double fee, double received,
                double pnlPct, double pnlIdr, String txId);
        void onBuyFailed(double needed, double available);
    }

    public void setOrderResultListener(OrderResultListener l) {
        this.listener = l;
    }

    public static OrderBottomSheetFragment newInstance(String coinId, String symbol, String name, String type) {
        OrderBottomSheetFragment f = new OrderBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COIN_ID, coinId);
        args.putString(ARG_SYMBOL, symbol);
        args.putString(ARG_NAME, name);
        args.putString(ARG_TYPE, type);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String coinId = getArguments().getString(ARG_COIN_ID, "");
        String symbol = getArguments().getString(ARG_SYMBOL, "");
        String name = getArguments().getString(ARG_NAME, "");
        String type = getArguments().getString(ARG_TYPE, "BUY");
        boolean isBuy = "BUY".equals(type);

        TextView tvTitle = view.findViewById(R.id.tv_order_title);
        TextView tvCash = view.findViewById(R.id.tv_cash);
        TextView tvAtPrice = view.findViewById(R.id.tv_at_price);
        TextView tvNominalLabel = view.findViewById(R.id.tv_nominal_label);
        EditText etNominal = view.findViewById(R.id.et_nominal);
        TextView tvConverted = view.findViewById(R.id.tv_converted);
        TextView tvOrderValue = view.findViewById(R.id.tv_order_value);
        TextView tvFee = view.findViewById(R.id.tv_fee);
        TextView tvTotal = view.findViewById(R.id.tv_total);
        TextView tvRemaining = view.findViewById(R.id.tv_remaining);
        TextView btnConfirm = view.findViewById(R.id.btn_confirm);

        tvTitle.setText((isBuy ? "Beli " : "Jual ") + symbol.toUpperCase());
        tvNominalLabel.setText(isBuy ? "Nominal beli (Rp)" : "Nilai koin dijual (Rp)");
        btnConfirm.setText(isBuy ? "Konfirmasi Beli" : "Konfirmasi Jual");
        btnConfirm.setBackgroundResource(isBuy ? R.drawable.bg_btn_buy : R.drawable.bg_btn_sell);
        btnConfirm.setTextColor(isBuy ? 0xFF07130C : getResources().getColor(R.color.frx_down, null));

        PortfolioViewModel portfolioVM = new ViewModelProvider(requireActivity()).get(PortfolioViewModel.class);
        TradingViewModel tradingVM = new ViewModelProvider(requireActivity()).get(TradingViewModel.class);
        CryptoViewModel cryptoVM = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);

        float cash = portfolioVM.cashBalance.getValue() != null ? portfolioVM.cashBalance.getValue() : 0f;
        tvCash.setText("Kas: " + CurrencyFormatter.formatIdrFull(cash));

        cryptoVM.coinList.observe(getViewLifecycleOwner(), coins -> {
            if (coins == null) return;
            for (var c : coins) {
                if (c.id.equals(coinId)) {
                    currentPrice = c.currentPrice;
                    tvAtPrice.setText("@" + CurrencyFormatter.formatCompact(currentPrice));
                    break;
                }
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                double nominal = parseNominal(s.toString());
                double fee = nominal * 0.0015;
                double qty = currentPrice > 0 ? nominal / currentPrice : 0;
                float currentCash = portfolioVM.cashBalance.getValue() != null ? portfolioVM.cashBalance.getValue() : 0f;

                tvConverted.setText("≈ " + CurrencyFormatter.formatCoinQty(qty) + " " + symbol.toUpperCase());
                tvOrderValue.setText(CurrencyFormatter.formatIdrFull(nominal));
                tvFee.setText(CurrencyFormatter.formatIdrFull(fee));

                if (isBuy) {
                    tvTotal.setText(CurrencyFormatter.formatIdrFull(nominal + fee));
                    tvRemaining.setText(CurrencyFormatter.formatIdrFull(currentCash - nominal - fee));
                } else {
                    tvTotal.setText(CurrencyFormatter.formatIdrFull(nominal - fee)); // user receives
                    tvRemaining.setText(CurrencyFormatter.formatIdrFull(currentCash + nominal - fee));
                }
            }
        };
        etNominal.addTextChangedListener(watcher);

        view.findViewById(R.id.quick_100rb).setOnClickListener(v -> etNominal.setText("100000"));
        view.findViewById(R.id.quick_1jt).setOnClickListener(v -> etNominal.setText("1000000"));
        view.findViewById(R.id.quick_5jt).setOnClickListener(v -> etNominal.setText("5000000"));
        view.findViewById(R.id.quick_max).setOnClickListener(v -> {
            float currentCash = portfolioVM.cashBalance.getValue() != null ? portfolioVM.cashBalance.getValue() : 0f;
            if (isBuy) {
                double maxNominal = currentCash / 1.0015;
                etNominal.setText(String.valueOf((long) maxNominal));
            } else {
                // Max sell = current holding qty * currentPrice
                List<HoldingEntity> holdings = portfolioVM.holdings.getValue();
                if (holdings != null && currentPrice > 0) {
                    for (HoldingEntity h : holdings) {
                        if (h.coinId.equals(coinId)) {
                            long maxNominal = (long) (h.quantity * currentPrice);
                            etNominal.setText(String.valueOf(maxNominal));
                            break;
                        }
                    }
                }
            }
        });

        btnConfirm.setOnClickListener(v -> {
            double nominal = parseNominal(etNominal.getText().toString());
            if (nominal <= 0 || currentPrice <= 0) {
                Toast.makeText(requireContext(), "Nominal tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            float currentCash = portfolioVM.cashBalance.getValue() != null ? portfolioVM.cashBalance.getValue() : 0f;
            double fee = nominal * 0.0015;
            String txId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            if (isBuy) {
                double total = nominal + fee;
                if (total > currentCash + 0.01) {
                    if (listener != null) {
                        listener.onBuyFailed(total, currentCash);
                    } else {
                        Toast.makeText(requireContext(), "Saldo tidak cukup", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                    return;
                }
                tradingVM.buyCoin(coinId, symbol.toUpperCase(), name, nominal, currentPrice);
                if (listener != null) {
                    double qty = nominal / currentPrice;
                    listener.onBuySuccess(symbol.toUpperCase(), qty, currentPrice, fee, total, txId);
                }
            } else {
                double qty = nominal / currentPrice;
                double received = nominal - fee;
                List<HoldingEntity> holdings = portfolioVM.holdings.getValue();
                HoldingEntity target = null;
                if (holdings != null) {
                    for (HoldingEntity h : holdings) {
                        if (h.coinId.equals(coinId)) {
                            target = h;
                            break;
                        }
                    }
                }

                if (target == null || target.quantity < qty - 1e-10) {
                    Toast.makeText(requireContext(), "Koin tidak cukup untuk dijual", Toast.LENGTH_SHORT).show();
                    return;
                }

                tradingVM.sellCoin(coinId, symbol.toUpperCase(), name, qty, currentPrice);
                if (listener != null) {
                    double pnlIdr = (currentPrice - target.avgBuyPrice) * qty;
                    double pnlPct = target.avgBuyPrice > 0 ? (pnlIdr / (target.avgBuyPrice * qty)) * 100 : 0;
                    listener.onSellSuccess(symbol.toUpperCase(), qty, currentPrice, fee, received, pnlPct, pnlIdr, txId);
                }
            }

            portfolioVM.refreshBalance();
            dismiss();
        });
    }

    private double parseNominal(String text) {
        try {
            return Double.parseDouble(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
