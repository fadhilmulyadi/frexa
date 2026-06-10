package com.diellabs.frexa.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.diellabs.frexa.R;
import com.diellabs.frexa.databinding.FragmentLoginBinding;
import com.diellabs.frexa.viewmodel.UserViewModel;
import com.google.android.material.tabs.TabLayout;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding b;
    private UserViewModel vm;
    private boolean isRegister = false;
    private boolean isEmail = true;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup parent, Bundle saved) {
        b = FragmentLoginBinding.inflate(inf, parent, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        vm = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        b.tabLayout.addTab(b.tabLayout.newTab().setText("Masuk"));
        b.tabLayout.addTab(b.tabLayout.newTab().setText("Daftar"));
        b.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                isRegister = tab.getPosition() == 1;
                b.tilName.setVisibility(isRegister ? View.VISIBLE : View.GONE);
                b.btnSubmit.setText(isRegister ? "Daftar" : "Masuk");
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        b.btnEmail.setOnClickListener(x -> setMethod(true));
        b.btnPhone.setOnClickListener(x -> setMethod(false));
        b.btnSubmit.setOnClickListener(x -> submit());
    }

    private void setMethod(boolean email) {
        isEmail = email;
        b.tilCredential.setHint(email ? "Email" : "Nomor HP");
        b.etCredential.setInputType(email
            ? android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            : android.text.InputType.TYPE_CLASS_PHONE);
        b.btnEmail.setStrokeWidth(email ? 4 : 1);
        b.btnPhone.setStrokeWidth(email ? 1 : 4);
    }

    private void submit() {
        String cred = b.etCredential.getText() != null ? b.etCredential.getText().toString().trim() : "";
        String pass = b.etPassword.getText() != null ? b.etPassword.getText().toString() : "";
        String name = b.etName.getText() != null ? b.etName.getText().toString().trim() : "User";

        if (cred.isEmpty() || pass.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isRegister && name.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon isi nama", Toast.LENGTH_SHORT).show();
            return;
        }

        String displayName = isRegister ? name : (cred.contains("@") ? cred.split("@")[0] : cred);
        vm.login(displayName, cred, isEmail);
        Navigation.findNavController(requireView()).navigate(R.id.action_login_to_home);
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
