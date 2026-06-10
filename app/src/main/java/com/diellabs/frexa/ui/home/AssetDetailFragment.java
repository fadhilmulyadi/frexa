package com.diellabs.frexa.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.remote.model.CoinMarket;
import com.diellabs.frexa.databinding.FragmentAssetDetailBinding;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.util.UserPrefs;
import com.diellabs.frexa.viewmodel.CryptoViewModel;
import java.util.List;

public class AssetDetailFragment extends Fragment {

    private FragmentAssetDetailBinding binding;
    private CryptoViewModel cryptoVm;
    private String coinId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAssetDetailBinding.inflate(inflater, container, false);
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

        binding.btnViewChart.setOnClickListener(v -> {
            UserPrefs prefs = new UserPrefs(requireContext());
            prefs.setActiveCoinId(coinId);
            Navigation.findNavController(v).navigate(R.id.terminalFragment);
        });

        binding.btnQuoteHistory.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("coinId", coinId);
            Navigation.findNavController(v).navigate(R.id.quoteHistoryFragment, args);
        });

        cryptoVm.coinList.observe(getViewLifecycleOwner(), this::bindCoin);
    }

    private void bindCoin(List<CoinMarket> coins) {
        if (coins == null || coinId == null) return;
        for (CoinMarket c : coins) {
            if (coinId.equals(c.getId())) {
                binding.tvSymbol.setText(c.getSymbol().toUpperCase());
                binding.tvName.setText(c.getName());
                binding.tvProfit.setText("Profit " + (int) c.getProfitPercent() + "%");
                Glide.with(this).load(c.getImage()).circleCrop().into(binding.ivCoin);
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
