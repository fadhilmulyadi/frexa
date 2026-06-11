package com.diellabs.frexa.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        binding.tvUserId.setText("ID " + prefs.getUserEmail().hashCode()); // Mock ID

        binding.btnCopyId.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("User ID", binding.tvUserId.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(requireContext(), "ID disalin", android.widget.Toast.LENGTH_SHORT).show();
        });

        binding.btnSettings.setOnClickListener(v -> {
            // Placeholder for settings
            android.widget.Toast.makeText(requireContext(), "Pengaturan", android.widget.Toast.LENGTH_SHORT).show();
        });

        loadStats();
    }

    private void loadStats() {
        TradingRepository repo = new TradingRepository(requireContext());
        com.diellabs.frexa.util.AppExecutors.getInstance().diskIO().execute(() -> {
            int total = repo.getTotalTrades();
            int wins = repo.getWinCount();
            double best = repo.getBestProfit();
            requireActivity().runOnUiThread(() -> {
                binding.tvStatBest.setText(CurrencyFormatter.formatUsd(best));
                // Mocking profit for now or calculating if possible
                binding.tvStatProfit.setText(CurrencyFormatter.formatUsd(best * 0.8)); 
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
