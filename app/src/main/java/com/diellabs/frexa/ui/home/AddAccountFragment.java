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
import com.diellabs.frexa.R;
import com.diellabs.frexa.databinding.FragmentAddAccountBinding;

public class AddAccountFragment extends Fragment {

    private FragmentAddAccountBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        binding.btnNext.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null
                    ? binding.etEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                binding.tilEmail.setError("Email tidak boleh kosong");
                return;
            }
            binding.tilEmail.setError(null);
            Bundle args = new Bundle();
            args.putString("email", email);
            Navigation.findNavController(v).navigate(R.id.action_add_account_to_name, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
