package com.diellabs.frexa.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.diellabs.frexa.databinding.FragmentProfileBinding;
import com.diellabs.frexa.util.UserPrefs;

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
        binding.tvUserId.setText("ID " + Math.abs(prefs.getUserEmail().hashCode())); // Mock ID

        binding.btnCopyId.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("User ID", binding.tvUserId.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(requireContext(), "ID disalin", android.widget.Toast.LENGTH_SHORT).show();
        });

        binding.btnSettings.setOnClickListener(v -> {
            android.widget.Toast.makeText(requireContext(), "Pengaturan", android.widget.Toast.LENGTH_SHORT).show();
        });

        binding.btnBack.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
