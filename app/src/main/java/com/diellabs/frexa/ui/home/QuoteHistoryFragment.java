package com.diellabs.frexa.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.databinding.FragmentQuoteHistoryBinding;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class QuoteHistoryFragment extends Fragment {

    private FragmentQuoteHistoryBinding binding;
    private CryptoViewModel cryptoVm;
    private String coinId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQuoteHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cryptoVm = new ViewModelProvider(requireActivity()).get(CryptoViewModel.class);

        if (getArguments() != null) {
            coinId = getArguments().getString("coinId", "bitcoin");
        }

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        cryptoVm.coinList.observe(getViewLifecycleOwner(), this::buildQuotes);
    }

    private void buildQuotes(List<CoinMarket> coins) {
        if (coins == null || coinId == null) return;
        double basePrice = 0;
        for (CoinMarket c : coins) {
            if (coinId.equals(c.getId())) {
                basePrice = c.getCurrentPrice();
                break;
            }
        }
        if (basePrice == 0) return;

        List<double[]> quotes = generateQuotes(basePrice, 20);
        QuoteAdapter adapter = new QuoteAdapter(quotes);
        binding.rvQuotes.setAdapter(adapter);
    }

    private List<double[]> generateQuotes(double base, int count) {
        List<double[]> list = new ArrayList<>();
        Random rng = new Random();
        long now = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            double noise = (rng.nextDouble() - 0.5) * 2 * 0.001 * base;
            double price = base + noise;
            long ts = now - (long) i * 60_000;
            list.add(new double[]{ts, price});
        }
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.VH> {
        private final List<double[]> data;
        private final SimpleDateFormat sdf =
                new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        QuoteAdapter(List<double[]> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_quote, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            double[] row = data.get(position);
            holder.tvTime.setText(sdf.format(new Date((long) row[0])));
            holder.tvPrice.setText(CurrencyFormatter.formatUsd(row[1]));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTime, tvPrice;

            VH(View v) {
                super(v);
                tvTime = v.findViewById(R.id.tv_time);
                tvPrice = v.findViewById(R.id.tv_price);
            }
        }
    }
}
