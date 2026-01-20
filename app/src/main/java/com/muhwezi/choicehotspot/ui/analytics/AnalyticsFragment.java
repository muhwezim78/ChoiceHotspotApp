package com.muhwezi.choicehotspot.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.api.ApiCallback;
import com.muhwezi.choicehotspot.models.analytics.AnalyticsDashboard;
import com.muhwezi.choicehotspot.repository.ApiRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private TextView tvTotalRevenue, tvTodayRevenue;
    private TextView tvTotalUsers, tvActiveUsers, tvNewUsers;
    private TextView tvLastUpdated;
    private RecyclerView rvTopProfiles;
    private ProfileStatAdapter adapter;
    private LineChart revenueChart, userGrowthChart;
    private com.github.mikephil.charting.charts.BarChart voucherUsageChart;
    private com.github.mikephil.charting.charts.PieChart userDistributionChart;

    public AnalyticsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        tvTodayRevenue = view.findViewById(R.id.tv_today_revenue);
        tvTotalUsers = view.findViewById(R.id.tv_total_users);
        tvActiveUsers = view.findViewById(R.id.tv_active_users);
        tvNewUsers = view.findViewById(R.id.tv_new_users);
        tvLastUpdated = view.findViewById(R.id.tv_last_updated);

        rvTopProfiles = view.findViewById(R.id.rv_top_profiles);
        rvTopProfiles.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfileStatAdapter();
        rvTopProfiles.setAdapter(adapter);

        revenueChart = view.findViewById(R.id.revenue_chart);
        if (revenueChart != null) {
            setupChart(revenueChart);
        }

        userGrowthChart = view.findViewById(R.id.user_growth_chart);
        if (userGrowthChart != null) {
            setupChart(userGrowthChart);
        }

        voucherUsageChart = view.findViewById(R.id.voucher_usage_chart);
        if (voucherUsageChart != null) {
            setupBarChart(voucherUsageChart);
        }

        userDistributionChart = view.findViewById(R.id.user_distribution_chart);
        if (userDistributionChart != null) {
            setupPieChart(userDistributionChart);
        }

        loadData();

        return view;
    }

    private void loadData() {
        ApiRepository.getInstance().getAnalyticsDashboard(true, new ApiCallback<AnalyticsDashboard>() {
            @Override
            public void onSuccess(AnalyticsDashboard data) {
                if (!isAdded())
                    return;

                tvTotalRevenue.setText(String.format(Locale.US, "%,.0f UGX", data.getTotalRevenue()));
                tvTodayRevenue.setText(String.format(Locale.US, "%,.0f UGX", data.getRevenueToday()));
                tvTotalUsers.setText(String.valueOf(data.getTotalUsers()));
                tvActiveUsers.setText(String.valueOf(data.getActiveUsers()));
                tvNewUsers.setText(String.valueOf(data.getNewUsersToday()));

                if (data.getProfileStats() != null) {
                    adapter.setProfileStats(data.getProfileStats());
                } else if (data.getTopProfiles() != null) {
                    adapter.setProfileStats(data.getTopProfiles());
                }

                if (revenueChart != null && data.getRevenueTrend() != null) {
                    updateRevenueChart(data.getRevenueTrend());
                }

                if (userGrowthChart != null && data.getUserGrowth() != null) {
                    updateUserGrowthChart(data.getUserGrowth());
                }

                if (voucherUsageChart != null && (data.getRevenueTrend() != null || data.getRevenueData() != null)) {
                    updateVoucherUsageChart(
                            data.getRevenueTrend() != null ? data.getRevenueTrend() : data.getRevenueData());
                }

                if (userDistributionChart != null) {
                    updateUserDistributionChart(data);
                }

                updateLastUpdated();
            }

            @Override
            public void onError(String message, Throwable t) {
                if (!isAdded())
                    return;
                Toast.makeText(getContext(), "Failed to load analytics: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLastUpdated() {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        tvLastUpdated.setText("Last updated: " + time);
    }

    private void setupChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setExtraOffsets(10, 10, 10, 10);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setTextColor(getResources().getColor(R.color.md_theme_onSurfaceVariant, null));
        xAxis.setDrawAxisLine(false);

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(0x1A000000);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setTextColor(getResources().getColor(R.color.md_theme_onSurfaceVariant, null));

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setForm(com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE);
        chart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutCubic);
    }

    private void updateRevenueChart(List<AnalyticsDashboard.DailyRevenue> trend) {
        if (trend == null || trend.isEmpty())
            return;

        List<Entry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        for (int i = 0; i < trend.size(); i++) {
            AnalyticsDashboard.DailyRevenue day = trend.get(i);
            entries.add(new Entry(i, (float) day.revenue));
            labels.add(day.date != null && day.date.length() >= 10 ? day.date.substring(5, 10) : ""); // MM-DD
        }

        LineDataSet dataSet = new LineDataSet(entries, "Revenue");
        int color = getResources().getColor(R.color.gradient_blue_start, null);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setCircleHoleColor(getResources().getColor(R.color.white, null));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);

        // Use gradient for fill
        android.graphics.drawable.Drawable drawable = getResources().getDrawable(R.drawable.bg_gradient_blue, null);
        dataSet.setFillDrawable(drawable);
        dataSet.setFillAlpha(80);
        dataSet.setDrawValues(false);

        revenueChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < labels.size()) ? labels.get(index) : "";
            }
        });

        LineData lineData = new LineData(dataSet);
        revenueChart.setData(lineData);
        revenueChart.invalidate();
    }

    private void updateUserGrowthChart(List<AnalyticsDashboard.DailyGrowth> trend) {
        if (trend == null || trend.isEmpty())
            return;

        List<Entry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        for (int i = 0; i < trend.size(); i++) {
            AnalyticsDashboard.DailyGrowth day = trend.get(i);
            entries.add(new Entry(i, (float) day.count));
            labels.add(day.date != null && day.date.length() >= 10 ? day.date.substring(5, 10) : ""); // MM-DD
        }

        LineDataSet dataSet = new LineDataSet(entries, "New Users");
        int color = getResources().getColor(R.color.gradient_purple_start, null);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setCircleHoleColor(getResources().getColor(R.color.white, null));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);

        android.graphics.drawable.Drawable drawable = getResources().getDrawable(R.drawable.bg_gradient_purple, null);
        dataSet.setFillDrawable(drawable);
        dataSet.setFillAlpha(80);
        dataSet.setDrawValues(false);

        userGrowthChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < labels.size()) ? labels.get(index) : "";
            }
        });

        LineData lineData = new LineData(dataSet);
        userGrowthChart.setData(lineData);
        userGrowthChart.invalidate();
    }

    private void setupBarChart(com.github.mikephil.charting.charts.BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setTextColor(getResources().getColor(R.color.md_theme_onSurfaceVariant, null));

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(0x1A000000);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.animateY(1000);
    }

    private void setupPieChart(com.github.mikephil.charting.charts.PieChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(true);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(android.R.color.transparent);
        chart.setTransparentCircleAlpha(0);
        chart.setCenterText("Users");
        chart.setCenterTextSize(18f);
        chart.setDrawCenterText(true);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);
        chart.getLegend().setEnabled(true);
        chart.getLegend()
                .setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        chart.getLegend().setHorizontalAlignment(
                com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        chart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
    }

    private void updateVoucherUsageChart(List<AnalyticsDashboard.DailyRevenue> trend) {
        if (trend == null || trend.isEmpty())
            return;

        List<com.github.mikephil.charting.data.BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        for (int i = 0; i < trend.size(); i++) {
            AnalyticsDashboard.DailyRevenue day = trend.get(i);
            entries.add(new com.github.mikephil.charting.data.BarEntry(i, (float) day.voucherCount));
            labels.add(day.date != null && day.date.length() >= 10 ? day.date.substring(5, 10) : "");
        }

        com.github.mikephil.charting.data.BarDataSet dataSet = new com.github.mikephil.charting.data.BarDataSet(entries,
                "Vouchers Sold");
        dataSet.setColor(getResources().getColor(R.color.md_theme_primary, null));
        dataSet.setValueTextColor(getResources().getColor(R.color.md_theme_onSurface, null));
        dataSet.setValueTextSize(10f);

        voucherUsageChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < labels.size()) ? labels.get(index) : "";
            }
        });

        com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(dataSet);
        barData.setBarWidth(0.6f);
        voucherUsageChart.setData(barData);
        voucherUsageChart.invalidate();
    }

    private void updateUserDistributionChart(AnalyticsDashboard data) {
        List<com.github.mikephil.charting.data.PieEntry> entries = new ArrayList<>();
        int vouchers = data.getVouchersCount();
        int regulars = Math.max(0, data.getTotalUsers() - vouchers);

        if (vouchers > 0)
            entries.add(new com.github.mikephil.charting.data.PieEntry(vouchers, "Voucher Users"));
        if (regulars > 0)
            entries.add(new com.github.mikephil.charting.data.PieEntry(regulars, "Regular Users"));

        if (entries.isEmpty())
            return;

        com.github.mikephil.charting.data.PieDataSet dataSet = new com.github.mikephil.charting.data.PieDataSet(entries,
                "User Distribution");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.md_theme_primary, null));
        colors.add(getResources().getColor(R.color.md_theme_secondary, null));
        dataSet.setColors(colors);

        com.github.mikephil.charting.data.PieData pieData = new com.github.mikephil.charting.data.PieData(dataSet);
        pieData.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(userDistributionChart));
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(getResources().getColor(R.color.white, null));

        userDistributionChart.setData(pieData);
        userDistributionChart.invalidate();
    }
}
