package com.muhwezi.choicehotspot.models.financial;

import com.google.gson.annotations.SerializedName;

/**
 * Model for active revenue and user count.
 */
public class ActiveRevenue {
    @SerializedName("active_users_count")
    private int activeUsersCount;

    @SerializedName("daily_revenue")
    private double dailyRevenue;

    @SerializedName("note")
    private String note;

    public int getActiveUsersCount() {
        return activeUsersCount;
    }

    public void setActiveUsersCount(int activeUsersCount) {
        this.activeUsersCount = activeUsersCount;
    }

    public double getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(double dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
