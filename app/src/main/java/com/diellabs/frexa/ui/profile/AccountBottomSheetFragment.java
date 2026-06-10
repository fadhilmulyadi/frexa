package com.diellabs.frexa.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.diellabs.frexa.databinding.BottomSheetAccountBinding;
import com.diellabs.frexa.util.CurrencyFormatter;
import com.diellabs.frexa.util.UserPrefs;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AccountBottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetAccountBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UserPrefs prefs = new UserPrefs(requireContext());
        binding.tvAccountName.setText(prefs.getUserName());
        binding.tvBalance.setText(CurrencyFormatter.formatBalance(prefs.getBalance()));
        binding.tvCredential.setText(prefs.getUserEmail());
        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
