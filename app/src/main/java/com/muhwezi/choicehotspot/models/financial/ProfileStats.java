package com.muhwezi.choicehotspot.models.financial;

import com.google.gson.annotations.SerializedName;

/**
 * Profile-level statistics.
 */
public class ProfileStats {

    @SerializedName("profile_name")
    private String profileName;

    @SerializedName("total_users")
    private int totalUsers;

    @SerializedName("active_users")
    private int activeUsers;

    @SerializedName("revenue")
    private double revenue;

    @SerializedName("percentage")
    private double percentage;

    public ProfileStats() {
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
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

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
