package com.diellabs.frexa;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.diellabs.frexa.databinding.ActivityMainBinding;
import com.diellabs.frexa.util.UserPrefs;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private static final List<Integer> BOTTOM_NAV_IDS = Arrays.asList(
        R.id.homeFragment, R.id.marketsFragment, R.id.portfolioFragment,
        R.id.ordersFragment, R.id.watchlistFragment
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserPrefs prefs = new UserPrefs(this);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment host = (NavHostFragment)
            getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = host.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        navController.addOnDestinationChangedListener((c, dest, a) ->
            binding.bottomNav.setVisibility(
                BOTTOM_NAV_IDS.contains(dest.getId()) ? View.VISIBLE : View.GONE));
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
