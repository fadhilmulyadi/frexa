package com.diellabs.frexa.ui.deposit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.diellabs.frexa.databinding.BottomSheetDepositBinding;
import com.diellabs.frexa.util.UserPrefs;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DepositBottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetDepositBinding binding;
    private int step = 0;
    private static final float DEPOSIT_AMOUNT = 50000f;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetDepositBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showStep(0);

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnBack.setOnClickListener(v -> showStep(step - 1));
        binding.btnNext.setOnClickListener(v -> {
            if (step < 3) {
                showStep(step + 1);
            } else {
                completeDeposit();
            }
        });
    }

    private void showStep(int s) {
        step = s;
        binding.vgMain.setVisibility(s == 0 ? View.VISIBLE : View.GONE);
        binding.vgNominal.setVisibility(s == 1 ? View.VISIBLE : View.GONE);
        binding.vgConfirm.setVisibility(s == 2 ? View.VISIBLE : View.GONE);
        binding.vgQr.setVisibility(s == 3 ? View.VISIBLE : View.GONE);
        binding.btnBack.setVisibility(s > 0 ? View.VISIBLE : View.GONE);

        if (s == 3) {
            binding.btnNext.setText("Selesai");
        } else {
            binding.btnNext.setText("Selanjutnya");
        }
    }

    private void completeDeposit() {
        UserPrefs prefs = new UserPrefs(requireContext());
        prefs.setBalance(prefs.getBalance() + DEPOSIT_AMOUNT);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
