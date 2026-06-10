package com.diellabs.frexa.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.diellabs.frexa.databinding.FragmentAddAccountNameBinding;
import com.diellabs.frexa.util.UserPrefs;

public class AddAccountNameFragment extends Fragment {

    private FragmentAddAccountNameBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddAccountNameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText() != null
                    ? binding.etName.getText().toString().trim() : "";
            if (TextUtils.isEmpty(name)) {
                binding.tilName.setError("Nama tidak boleh kosong");
                return;
            }
            binding.tilName.setError(null);

            String email = "";
            if (getArguments() != null) {
                email = getArguments().getString("email", "");
            }

            UserPrefs prefs = new UserPrefs(requireContext());
            prefs.setUserName(name);
            prefs.setUserEmail(email);

            Navigation.findNavController(v).navigate(
                    com.diellabs.frexa.R.id.homeFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
