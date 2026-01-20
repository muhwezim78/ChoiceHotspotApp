package com.muhwezi.choicehotspot.ui.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.api.ApiCallback;
import com.muhwezi.choicehotspot.models.financial.FinancialStats;
import com.muhwezi.choicehotspot.models.system.SystemHealth;
import com.muhwezi.choicehotspot.repository.ApiRepository;
import com.muhwezi.choicehotspot.utils.ApiUtils;

import java.util.Date;

public class DashboardFragment extends Fragment {

    private TextView totalRevenueText;
    private TextView todayRevenueText;
    private TextView activeUsersText;
    private TextView vouchersCountText;
    private TextView systemStatusText;
    private TextView lastUpdatedText;
    private Button generateVouchersButton;
    private Button manageUsersButton;

    // Auto-refresh handler
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadDashboardData();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        totalRevenueText = view.findViewById(R.id.total_revenue_text);
        todayRevenueText = view.findViewById(R.id.today_revenue_text);
        activeUsersText = view.findViewById(R.id.active_users_text);
        vouchersCountText = view.findViewById(R.id.vouchers_count_text);
        systemStatusText = view.findViewById(R.id.system_status_text);
        lastUpdatedText = view.findViewById(R.id.last_updated_text);
        generateVouchersButton = view.findViewById(R.id.generate_vouchers_button);
        manageUsersButton = view.findViewById(R.id.manage_users_button);

        setupListeners();
    }

    private void setupListeners() {
        generateVouchersButton.setOnClickListener(v -> {
            com.muhwezi.choicehotspot.ui.vouchers.GenerateVoucherBottomSheet bottomSheet = new com.muhwezi.choicehotspot.ui.vouchers.GenerateVoucherBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "GenerateVoucherBottomSheet");
        });

        manageUsersButton.setOnClickListener(v -> {
            // Navigate to Users Fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.muhwezi.choicehotspot.ui.users.UsersFragment())
                    .addToBackStack(null)
                    .commit();
        });

        Button manageProfilesButton = getView().findViewById(R.id.manage_profiles_button);
        if (manageProfilesButton != null) {
            manageProfilesButton.setOnClickListener(v -> {
                // Navigate to Profiles Fragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.muhwezi.choicehotspot.ui.profiles.ProfilesFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void loadDashboardData() {
        loadFinancialStats();
        loadActiveRevenue();
        checkSystemHealth();
    }

    private void loadFinancialStats() {
        ApiRepository.getInstance().getFinancialStats(true, new ApiCallback<FinancialStats>() {
            @Override
            public void onSuccess(FinancialStats data) {
                if (!isAdded())
                    return;

                totalRevenueText.setText(ApiUtils.formatCurrency(data.getTotalRevenue()));
                todayRevenueText.setText(ApiUtils.formatCurrency(data.getTodayRevenue()));

                // Fallback for active users if specific call hasn't returned yet
                if (activeUsersText.getText().toString().equals("0")
                        || activeUsersText.getText().toString().isEmpty()) {
                    activeUsersText.setText(String.valueOf(data.getActiveVouchers()));
                }
                vouchersCountText.setText(String.valueOf(data.getActiveVouchers()));

                String time = ApiUtils.formatDateTime(new Date());
                lastUpdatedText.setText("Last updated: " + time);

                // Update Widget Data
                if (getContext() != null) {
                    android.content.SharedPreferences prefs = getContext().getSharedPreferences("widget_prefs",
                            android.content.Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("widget_revenue", ApiUtils.formatCurrency(data.getTotalRevenue()))
                            .putString("widget_last_updated", time)
                            .apply();

                    // Trigger Update
                    android.appwidget.AppWidgetManager man = android.appwidget.AppWidgetManager
                            .getInstance(getContext());
                    int[] ids = man.getAppWidgetIds(new android.content.ComponentName(getContext(),
                            com.muhwezi.choicehotspot.widget.DashboardWidget.class));
                    if (ids.length > 0) {
                        android.content.Intent updateIntent = new android.content.Intent(getContext(),
                                com.muhwezi.choicehotspot.widget.DashboardWidget.class);
                        updateIntent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        updateIntent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                        getContext().sendBroadcast(updateIntent);
                    }
                }
            }

            @Override
            public void onError(String message, Throwable error) {
            }
        });
    }

    private void loadActiveRevenue() {
        // Use active users list size for accuracy, similar to Web App Quick Overview
        ApiRepository.getInstance()
                .getActiveUsers(new ApiCallback<java.util.List<com.muhwezi.choicehotspot.models.user.HotspotUser>>() {
                    @Override
                    public void onSuccess(java.util.List<com.muhwezi.choicehotspot.models.user.HotspotUser> users) {
                        if (!isAdded())
                            return;
                        if (users != null) {
                            activeUsersText.setText(String.valueOf(users.size()));

                            if (getContext() != null) {
                                android.content.SharedPreferences prefs = getContext().getSharedPreferences(
                                        "widget_prefs",
                                        android.content.Context.MODE_PRIVATE);
                                prefs.edit()
                                        .putString("widget_active_users", "Users: " + users.size())
                                        .apply();
                            }
                        }
                    }

                    @Override
                    public void onError(String message, Throwable t) {
                        // Fallback to active revenue count if list fetch fails
                        ApiRepository.getInstance()
                                .getActiveRevenue(
                                        new ApiCallback<com.muhwezi.choicehotspot.models.financial.ActiveRevenue>() {
                                            @Override
                                            public void onSuccess(
                                                    com.muhwezi.choicehotspot.models.financial.ActiveRevenue data) {
                                                if (!isAdded())
                                                    return;

                                                // Only update if we have a valid count, or if the field is currently
                                                // empty/zero
                                                // This prevents overwriting the "Active Vouchers" fallback with a "0"
                                                // active users count
                                                if (data.getActiveUsersCount() > 0) {
                                                    activeUsersText.setText(String.valueOf(data.getActiveUsersCount()));
                                                }

                                                if (getContext() != null) {
                                                    android.content.SharedPreferences prefs = getContext()
                                                            .getSharedPreferences("widget_prefs",
                                                                    android.content.Context.MODE_PRIVATE);
                                                    prefs.edit()
                                                            .putString("widget_active_users",
                                                                    "Users: " + data.getActiveUsersCount())
                                                            .apply();
                                                }
                                            }

                                            @Override
                                            public void onError(String message, Throwable error) {
                                            }
                                        });
                    }
                });
    }

    private void checkSystemHealth() {
        ApiRepository.getInstance().getSystemHealth(new ApiCallback<SystemHealth>() {
            @Override
            public void onSuccess(SystemHealth data) {
                if (!isAdded())
                    return;
                systemStatusText.setText("System Status: " + data.getStatus());
            }

            @Override
            public void onError(String message, Throwable error) {
                if (isAdded()) {
                    systemStatusText.setText("System Status: Offline or Error");
                }
            }
        });
    }
}
