package com.diellabs.frexa.ui.bantuan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.diellabs.frexa.R;
import com.diellabs.frexa.databinding.FragmentBantuanBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BantuanFragment extends Fragment {

    private FragmentBantuanBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBantuanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSupport.setOnClickListener(v -> showDialog(
                "Dukungan 24/7",
                "Tim kami siap membantu Anda 24 jam sehari, 7 hari seminggu.\nHubungi: support@frexa.app"));

        binding.btnHelp.setOnClickListener(v -> showDialog(
                "Pusat Bantuan",
                "Temukan jawaban atas pertanyaan umum tentang cara menggunakan Frexa, staking, dan lebih banyak lagi."));

        binding.btnEducation.setOnClickListener(v -> showDialog(
                "Edukasi Trading",
                "Pelajari dasar-dasar trading kripto, analisis teknikal, dan strategi manajemen risiko."));

        binding.btnTutorial.setOnClickListener(v -> showDialog(
                "Tutorial Trading",
                "Ikuti panduan langkah demi langkah untuk menempatkan perdagangan pertama Anda di Frexa."));
    }

    private void showDialog(String title, String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
