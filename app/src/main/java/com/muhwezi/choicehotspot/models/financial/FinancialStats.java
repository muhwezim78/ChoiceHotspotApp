package com.muhwezi.choicehotspot.models.financial;

import com.google.gson.annotations.SerializedName;

/**
 * Financial statistics model.
 */
public class FinancialStats {

    @SerializedName("total_revenue")
    private double totalRevenue;

    @SerializedName(value = "today_revenue", alternate = { "daily_revenue", "revenue_today" })
    private double todayRevenue;

    @SerializedName("revenue")
    private double revenue;

    @SerializedName("voucher_count")
    private int voucherCount;

    @SerializedName("weekly_revenue")
    private double weeklyRevenue;

    @SerializedName("monthly_revenue")
    private double monthlyRevenue;

    @SerializedName("active_users")
    private int activeUsers;

    @SerializedName("total_vouchers")
    private int totalVouchers;

    @SerializedName("active_vouchers")
    private int activeVouchers;

    @SerializedName("used_vouchers_today")
    private int usedVouchersToday;

    @SerializedName("expired_vouchers")
    private int expiredVouchers;

    @SerializedName("active_users_count") // From active-revenue endpoint
    private int activeUsersCount;

    @SerializedName("currency")
    private String currency;

    public FinancialStats() {
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public double getWeeklyRevenue() {
        return weeklyRevenue;
    }

    public void setWeeklyRevenue(double weeklyRevenue) {
        this.weeklyRevenue = weeklyRevenue;
    }

    public double getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(double monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getTotalVouchers() {
        return totalVouchers;
    }

    public void setTotalVouchers(int totalVouchers) {
        this.totalVouchers = totalVouchers;
    }

    public int getActiveVouchers() {
        return activeVouchers;
    }

    public void setActiveVouchers(int activeVouchers) {
        this.activeVouchers = activeVouchers;
    }

    public int getExpiredVouchers() {
        return expiredVouchers;
    }

    public void setExpiredVouchers(int expiredVouchers) {
        this.expiredVouchers = expiredVouchers;
    }

    public int getUsedVouchersToday() {
        return usedVouchersToday;
    }

    public void setUsedVouchersToday(int usedVouchersToday) {
        this.usedVouchersToday = usedVouchersToday;
    }

    public int getActiveUsersCount() {
        return activeUsersCount;
    }

    public void setActiveUsersCount(int activeUsersCount) {
        this.activeUsersCount = activeUsersCount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
