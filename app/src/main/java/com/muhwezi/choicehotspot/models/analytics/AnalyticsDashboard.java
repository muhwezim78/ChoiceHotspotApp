package com.muhwezi.choicehotspot.models.analytics;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Analytics dashboard model.
 */
public class AnalyticsDashboard {

    @SerializedName("total_users")
    private int totalUsers;

    @SerializedName("active_users")
    private int activeUsers;

    @SerializedName("new_users_today")
    private int newUsersToday;

    @SerializedName("total_revenue")
    private double totalRevenue;

    @SerializedName("revenue_today")
    private double revenueToday;

    @SerializedName("top_profiles")
    private List<ProfileStat> topProfiles;

    @SerializedName("profile_stats")
    private List<ProfileStat> profileStats;

    @SerializedName("revenue_trend")
    private List<DailyRevenue> revenueTrend;

    @SerializedName("revenue_data")
    private List<DailyRevenue> revenueData;

    @SerializedName("vouchers_count")
    private int vouchersCount;

    @SerializedName("user_growth")
    private List<DailyGrowth> userGrowth;

    public static class DailyGrowth {
        @SerializedName("date")
        public String date;
        @SerializedName("count")
        public int count;
    }

    public static class ProfileStat {
        @SerializedName("profile_name")
        public String profileName;
        @SerializedName("total_revenue")
        public double totalRevenue;
        @SerializedName("total_sold")
        public int totalSold;
        @SerializedName("used_count")
        public int usedCount;
    }

    public static class DailyRevenue {
        @SerializedName("date")
        public String date;
        @SerializedName("revenue")
        public double revenue;
        @SerializedName("voucher_count")
        public int voucherCount;
    }

    public AnalyticsDashboard() {
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getNewUsersToday() {
        return newUsersToday;
    }

    public void setNewUsersToday(int newUsersToday) {
        this.newUsersToday = newUsersToday;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getRevenueToday() {
        return revenueToday;
    }

    public void setRevenueToday(double revenueToday) {
        this.revenueToday = revenueToday;
    }

    public List<ProfileStat> getTopProfiles() {
        return topProfiles;
    }

    public List<DailyGrowth> getUserGrowth() {
        return userGrowth;
    }

    public List<DailyRevenue> getRevenueTrend() {
        return revenueTrend;
    }

    public List<ProfileStat> getProfileStats() {
        return profileStats;
    }

    public void setRevenueTrend(List<DailyRevenue> revenueTrend) {
        this.revenueTrend = revenueTrend;
    }

    public void setRevenueData(List<DailyRevenue> revenueData) {
        this.revenueData = revenueData;
    }

    public List<DailyRevenue> getRevenueData() {
        return revenueData;
    }

    public void setUserGrowth(List<DailyGrowth> userGrowth) {
        this.userGrowth = userGrowth;
    }

    public void setProfileStats(List<ProfileStat> profileStats) {
        this.profileStats = profileStats;
    }

    public int getVouchersCount() {
        return vouchersCount;
    }

    public void setVouchersCount(int vouchersCount) {
        this.vouchersCount = vouchersCount;
    }
}
