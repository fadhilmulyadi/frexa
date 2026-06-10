package com.diellabs.frexa.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.diellabs.frexa.R;
import com.diellabs.frexa.data.repository.TradingRepository;
import com.diellabs.frexa.databinding.FragmentProfileBinding;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.util.UserPrefs;
import com.diellabs.frexa.viewmodel.UserViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserPrefs prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new UserPrefs(requireContext());

        binding.tvUserName.setText(prefs.getUserName());
        binding.tvUserId.setText(prefs.getUserEmail());

        boolean isDark = "dark".equals(prefs.getThemeMode());
        binding.switchTheme.setChecked(isDark);
        binding.switchTheme.setOnCheckedChangeListener((btn, checked) -> {
            String mode = checked ? "dark" : "light";
            prefs.setThemeMode(mode);
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        loadStats();
    }

    private void loadStats() {
        TradingRepository repo = new TradingRepository(requireContext());
        com.diellabs.frexa.util.AppExecutors.getInstance().diskIO().execute(() -> {
            int total = repo.getTotalTrades();
            int wins = repo.getWinCount();
            double best = repo.getBestProfit();
            requireActivity().runOnUiThread(() -> {
                binding.tvStatTotal.setText(String.valueOf(total));
                String wr = total > 0 ? (wins * 100 / total) + "%" : "0%";
                binding.tvStatWinrate.setText(wr);
                binding.tvStatBest.setText(CurrencyFormatter.formatUsd(best));
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
