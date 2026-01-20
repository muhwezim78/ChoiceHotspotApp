package com.muhwezi.choicehotspot.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.api.ApiCallback;
import com.muhwezi.choicehotspot.models.pricing.PricingRate;
import com.muhwezi.choicehotspot.models.system.SystemInfo;
import com.muhwezi.choicehotspot.models.system.SystemHealth;
import com.muhwezi.choicehotspot.MainActivity;
import com.muhwezi.choicehotspot.repository.ApiRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private TextView tvDayRate, tvWeekRate, tvMonthRate;
    private TextView tvStatus, tvDatabase, tvRouterConnection, tvApiVersion;
    private TextView tvRouterName, tvRouterModel, tvUptime, tvCpuLoad, tvCpuCount, tvMemoryUsage, tvArchitecture,
            tvPlatform, tvSerialNumber, tvFirmware, tvVersion;
    private TextView tvLastUpdated;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Pricing Views
        View layoutDay = view.findViewById(R.id.layout_day_rate);
        tvDayRate = layoutDay.findViewById(R.id.tv_value);
        ((TextView) layoutDay.findViewById(R.id.tv_label)).setText("Daily Rate");
        ((com.google.android.material.imageview.ShapeableImageView) layoutDay.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_today);

        View layoutWeek = view.findViewById(R.id.layout_week_rate);
        tvWeekRate = layoutWeek.findViewById(R.id.tv_value);
        ((TextView) layoutWeek.findViewById(R.id.tv_label)).setText("Weekly Rate");
        ((com.google.android.material.imageview.ShapeableImageView) layoutWeek.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_week);

        View layoutMonth = view.findViewById(R.id.layout_month_rate);
        tvMonthRate = layoutMonth.findViewById(R.id.tv_value);
        ((TextView) layoutMonth.findViewById(R.id.tv_label)).setText("Monthly Rate");
        ((com.google.android.material.imageview.ShapeableImageView) layoutMonth.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_month);

        // Initialize System Status Views
        View layoutStatus = view.findViewById(R.id.layout_status);
        tvStatus = layoutStatus.findViewById(R.id.tv_value);
        ((TextView) layoutStatus.findViewById(R.id.tv_label)).setText("Status");
        ((com.google.android.material.imageview.ShapeableImageView) layoutStatus.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.presence_online);

        View layoutDb = view.findViewById(R.id.layout_database);
        tvDatabase = layoutDb.findViewById(R.id.tv_value);
        ((TextView) layoutDb.findViewById(R.id.tv_label)).setText("Database");
        ((com.google.android.material.imageview.ShapeableImageView) layoutDb.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_save);

        View layoutRouterConn = view.findViewById(R.id.layout_router_connection);
        tvRouterConnection = layoutRouterConn.findViewById(R.id.tv_value);
        ((TextView) layoutRouterConn.findViewById(R.id.tv_label)).setText("Router Connection");
        ((com.google.android.material.imageview.ShapeableImageView) layoutRouterConn.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.stat_sys_data_bluetooth);

        View layoutApi = view.findViewById(R.id.layout_api_version);
        tvApiVersion = layoutApi.findViewById(R.id.tv_value);
        ((TextView) layoutApi.findViewById(R.id.tv_label)).setText("API Version");
        ((com.google.android.material.imageview.ShapeableImageView) layoutApi.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_info_details);

        // Initialize System Info Views
        View layoutRouter = view.findViewById(R.id.layout_router_name);
        tvRouterName = layoutRouter.findViewById(R.id.tv_value);
        ((TextView) layoutRouter.findViewById(R.id.tv_label)).setText("Router Name");
        ((com.google.android.material.imageview.ShapeableImageView) layoutRouter.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.stat_sys_data_bluetooth);

        View layoutModel = view.findViewById(R.id.layout_router_model);
        tvRouterModel = layoutModel.findViewById(R.id.tv_value);
        ((TextView) layoutModel.findViewById(R.id.tv_label)).setText("Model");
        ((com.google.android.material.imageview.ShapeableImageView) layoutModel.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_manage);

        View layoutUptime = view.findViewById(R.id.layout_uptime);
        tvUptime = layoutUptime.findViewById(R.id.tv_value);
        ((TextView) layoutUptime.findViewById(R.id.tv_label)).setText("Uptime");
        ((com.google.android.material.imageview.ShapeableImageView) layoutUptime.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_recent_history);

        View layoutCpu = view.findViewById(R.id.layout_cpu_load);
        tvCpuLoad = layoutCpu.findViewById(R.id.tv_value);
        ((TextView) layoutCpu.findViewById(R.id.tv_label)).setText("CPU Load");
        ((com.google.android.material.imageview.ShapeableImageView) layoutCpu.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_sort_by_size);

        View layoutCpuCount = view.findViewById(R.id.layout_cpu_count);
        tvCpuCount = layoutCpuCount.findViewById(R.id.tv_value);
        ((TextView) layoutCpuCount.findViewById(R.id.tv_label)).setText("CPU Count");
        ((com.google.android.material.imageview.ShapeableImageView) layoutCpuCount.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_sort_by_size);

        View layoutMem = view.findViewById(R.id.layout_memory_usage);
        tvMemoryUsage = layoutMem.findViewById(R.id.tv_value);
        ((TextView) layoutMem.findViewById(R.id.tv_label)).setText("Memory Usage");
        ((com.google.android.material.imageview.ShapeableImageView) layoutMem.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_save);

        View layoutArch = view.findViewById(R.id.layout_architecture);
        tvArchitecture = layoutArch.findViewById(R.id.tv_value);
        ((TextView) layoutArch.findViewById(R.id.tv_label)).setText("Architecture");
        ((com.google.android.material.imageview.ShapeableImageView) layoutArch.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_info_details);

        View layoutPlat = view.findViewById(R.id.layout_platform);
        tvPlatform = layoutPlat.findViewById(R.id.tv_value);
        ((TextView) layoutPlat.findViewById(R.id.tv_label)).setText("Platform");
        ((com.google.android.material.imageview.ShapeableImageView) layoutPlat.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_info_details);

        View layoutSerial = view.findViewById(R.id.layout_serial_number);
        tvSerialNumber = layoutSerial.findViewById(R.id.tv_value);
        ((TextView) layoutSerial.findViewById(R.id.tv_label)).setText("Serial Number");
        ((com.google.android.material.imageview.ShapeableImageView) layoutSerial.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_info_details);

        View layoutFirm = view.findViewById(R.id.layout_firmware);
        tvFirmware = layoutFirm.findViewById(R.id.tv_value);
        ((TextView) layoutFirm.findViewById(R.id.tv_label)).setText("Firmware");
        ((com.google.android.material.imageview.ShapeableImageView) layoutFirm.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_info_details);

        View layoutVersion = view.findViewById(R.id.layout_version);
        tvVersion = layoutVersion.findViewById(R.id.tv_value);
        ((TextView) layoutVersion.findViewById(R.id.tv_label)).setText("OS Version");
        ((com.google.android.material.imageview.ShapeableImageView) layoutVersion.findViewById(R.id.iv_icon))
                .setImageResource(android.R.drawable.ic_menu_info_details);

        tvLastUpdated = view.findViewById(R.id.tv_last_updated);

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> logout());

        setupThemeToggle(view);

        loadData();

        return view;
    }

    private void setupThemeToggle(View view) {
        com.google.android.material.button.MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggle_theme);
        com.muhwezi.choicehotspot.utils.ThemeManager themeManager = new com.muhwezi.choicehotspot.utils.ThemeManager(
                requireContext());

        int currentMode = themeManager.getThemeMode();
        if (currentMode == com.muhwezi.choicehotspot.utils.ThemeManager.THEME_LIGHT) {
            toggleGroup.check(R.id.btn_theme_light);
        } else if (currentMode == com.muhwezi.choicehotspot.utils.ThemeManager.THEME_DARK) {
            toggleGroup.check(R.id.btn_theme_dark);
        } else {
            toggleGroup.check(R.id.btn_theme_system);
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_theme_light) {
                    themeManager.setThemeMode(com.muhwezi.choicehotspot.utils.ThemeManager.THEME_LIGHT);
                } else if (checkedId == R.id.btn_theme_dark) {
                    themeManager.setThemeMode(com.muhwezi.choicehotspot.utils.ThemeManager.THEME_DARK);
                } else if (checkedId == R.id.btn_theme_system) {
                    themeManager.setThemeMode(com.muhwezi.choicehotspot.utils.ThemeManager.THEME_SYSTEM);
                }
            }
        });
    }

    private void loadData() {
        ApiRepository.getInstance().getSystemHealth(new ApiCallback<SystemHealth>() {
            @Override
            public void onSuccess(SystemHealth data) {
                if (!isAdded())
                    return;
                tvStatus.setText(data.getStatus());
                tvDatabase.setText(data.getDatabase());
                tvRouterConnection.setText(data.getRouterConnection());
                tvApiVersion.setText(data.getApiVersion());

                // Colorize status
                if ("Online".equalsIgnoreCase(data.getStatus())) {
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onError(String message, Throwable t) {
                if (!isAdded())
                    return;
                tvStatus.setText("Offline");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });

        ApiRepository.getInstance().getPricingRates(true, new ApiCallback<Map<String, Double>>() {
            @Override
            public void onSuccess(Map<String, Double> data) {
                if (!isAdded())
                    return;

                if (data.containsKey("daily") || data.containsKey("day")) {
                    Double rate = data.containsKey("daily") ? data.get("daily") : data.get("day");
                    tvDayRate.setText(String.format(Locale.US, "%,.0f UGX", rate));
                }

                if (data.containsKey("weekly") || data.containsKey("week")) {
                    Double rate = data.containsKey("weekly") ? data.get("weekly") : data.get("week");
                    tvWeekRate.setText(String.format(Locale.US, "%,.0f UGX", rate));
                }

                if (data.containsKey("monthly") || data.containsKey("month")) {
                    Double rate = data.containsKey("monthly") ? data.get("monthly") : data.get("month");
                    tvMonthRate.setText(String.format(Locale.US, "%,.0f UGX", rate));
                }

                updateLastUpdated();
            }

            @Override
            public void onError(String message, Throwable t) {
                if (!isAdded())
                    return;
                Toast.makeText(getContext(), "Failed to load pricing", Toast.LENGTH_SHORT).show();
            }
        });

        ApiRepository.getInstance().getSystemInfo(true, new ApiCallback<SystemInfo>() {
            @Override
            public void onSuccess(SystemInfo data) {
                if (!isAdded())
                    return;
                tvRouterName.setText(data.getRouterName());
                tvRouterModel.setText(data.getRouterModel() != null ? data.getRouterModel() : "Unknown");
                tvUptime.setText(data.getUptime());
                tvCpuLoad.setText(data.getCpuLoad());
                tvCpuCount.setText(data.getCpuCount());
                tvMemoryUsage.setText(data.getMemoryUsage());
                tvArchitecture.setText(data.getArchitecture() != null ? data.getArchitecture() : "Unknown");
                tvPlatform.setText(data.getPlatform() != null ? data.getPlatform() : "Unknown");
                tvSerialNumber.setText(data.getSerialNumber() != null ? data.getSerialNumber() : "Unknown");
                tvFirmware.setText(data.getFirmware() != null ? data.getFirmware() : "Unknown");
                tvVersion.setText(data.getVersion());
                updateLastUpdated();
            }

            @Override
            public void onError(String message, Throwable t) {
                if (!isAdded())
                    return;
                Toast.makeText(getContext(), "Failed to load system info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLastUpdated() {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        tvLastUpdated.setText("Last updated: " + time);
    }

    private void logout() {
        ApiRepository.getInstance().logout(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded())
                    return;
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }

            @Override
            public void onError(String message, Throwable t) {
                if (!isAdded())
                    return;
                Toast.makeText(getContext(), "Logout failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
