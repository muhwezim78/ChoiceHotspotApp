package com.muhwezi.choicehotspot;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.muhwezi.choicehotspot.api.ApiClient;
import com.muhwezi.choicehotspot.repository.ApiRepository;
import com.muhwezi.choicehotspot.ui.analytics.AnalyticsFragment;
import com.muhwezi.choicehotspot.ui.auth.LoginFragment;
import com.muhwezi.choicehotspot.ui.dashboard.DashboardFragment;
import com.muhwezi.choicehotspot.ui.profiles.ProfilesFragment;
import com.muhwezi.choicehotspot.ui.settings.SettingsFragment;
import com.muhwezi.choicehotspot.ui.users.UsersFragment;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener {

    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Don't
            // pad bottom for nav view?
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        // Initialize API Repository
        ApiRepository.init(getApplicationContext());

        // Check for existing session
        if (savedInstanceState == null) {
            checkAuthAndNavigate();
            handleShortcutIntent();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                replaceFragment(new DashboardFragment(), false);
                return true;
            } else if (itemId == R.id.navigation_profiles) {
                replaceFragment(new ProfilesFragment(), false);
                return true;
            } else if (itemId == R.id.navigation_analytics) {
                replaceFragment(new AnalyticsFragment(), false);
                return true;
            } else if (itemId == R.id.navigation_users) {
                replaceFragment(new UsersFragment(), false);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                replaceFragment(new SettingsFragment(), false);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleShortcutIntent();
    }

    private void handleShortcutIntent() {
        if (getIntent() != null && getIntent().hasExtra("action")) {
            String action = getIntent().getStringExtra("action");
            if ("generate_voucher".equals(action)) {
                if (ApiClient.getInstance().isAuthenticated()) {
                    navigateToVouchers();
                }
            } else if ("view_dashboard".equals(action)) {
                if (ApiClient.getInstance().isAuthenticated()) {
                    navigateToDashboard();
                }
            }
        }
    }

    private void navigateToVouchers() {
        // Vouchers is likely accessed via Dashboard or specialized list now.
        // Or we can map "Users" tab to include Vouchers, or have a separate path.
        // For now, let's open VouchersFragment on top of whatever.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new com.muhwezi.choicehotspot.ui.vouchers.VouchersFragment())
                .addToBackStack(null)
                .commit();
    }

    private void checkAuthAndNavigate() {
        if (ApiClient.getInstance().isAuthenticated()) {
            Log.d(TAG, "User is authenticated, navigating to Dashboard");
            navigateToDashboard();
        } else {
            Log.d(TAG, "User not authenticated, navigating to Login");
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        bottomNavigationView.setVisibility(android.view.View.GONE);
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setLoginListener(this);
        replaceFragment(loginFragment, false);
    }

    private void navigateToDashboard() {
        bottomNavigationView.setVisibility(android.view.View.VISIBLE);
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
        // setSelectedItemId triggers the listener, so replaceFragment(new
        // DashboardFragment()) is called there.
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public void onLoginSuccess() {
        navigateToDashboard();
    }
}