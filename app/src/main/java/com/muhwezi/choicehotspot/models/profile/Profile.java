package com.muhwezi.choicehotspot.models.profile;

import com.google.gson.annotations.SerializedName;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Hotspot profile model.
 */
@Entity(tableName = "profiles")
public class Profile {

    @PrimaryKey
    @NonNull
    @SerializedName("name")
    private String name = "";

    @SerializedName("shared_users")
    private int sharedUsers;

    @SerializedName("data_limit")
    private String dataLimit;

    @SerializedName("time_limit")
    private String timeLimit;

    @SerializedName("uptime_limit")
    private String uptimeLimit;

    @SerializedName("rate_limit")
    private String rateLimit;

    @SerializedName("price")
    private double price;

    @SerializedName("validity_period")
    private int validityPeriod;

    @SerializedName("user_count")
    private int userCount;

    @SerializedName("active_users")
    private int activeUsers;

    public Profile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSharedUsers() {
        return sharedUsers;
    }

    public void setSharedUsers(int sharedUsers) {
        this.sharedUsers = sharedUsers;
    }

    public String getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(String rateLimit) {
        this.rateLimit = rateLimit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDataLimit() {
        return dataLimit;
    }

    public void setDataLimit(String dataLimit) {
        this.dataLimit = dataLimit;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getUptimeLimit() {
        return uptimeLimit;
    }

    public void setUptimeLimit(String uptimeLimit) {
        this.uptimeLimit = uptimeLimit;
    }

    public int getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(int validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }
}
