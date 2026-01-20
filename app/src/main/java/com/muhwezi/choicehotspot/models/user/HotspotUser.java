package com.muhwezi.choicehotspot.models.user;

import com.google.gson.annotations.SerializedName;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Hotspot user model.
 */
@Entity(tableName = "hotspot_users")
public class HotspotUser implements Serializable {

    @PrimaryKey
    @NonNull
    @SerializedName(value = "id", alternate = { "user" })
    private String id = "";

    @SerializedName(value = "username", alternate = { "name" })
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("password_type")
    private String passwordType;

    @SerializedName(value = "profile", alternate = { "profile_name" })
    private String profile;

    @SerializedName("mac_address")
    private String macAddress;

    @SerializedName("ip_address")
    private String ipAddress;

    @SerializedName(value = "uptime", alternate = { "current_uptime" })
    private String uptime;

    @SerializedName(value = "bytes_in", alternate = { "bytes-in", "bytesIn" })
    private long bytesIn;

    @SerializedName(value = "bytes_out", alternate = { "bytes-out", "bytesOut" })
    private long bytesOut;

    @SerializedName(value = "data_limit", alternate = { "uptime_limit" })
    private String dataLimit;

    @SerializedName(value = "data_used", alternate = { "bytes_used" })
    private long dataUsed;

    @SerializedName("comment")
    private String comment;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("last_seen")
    private String lastSeen;

    @SerializedName("server")
    private String server;

    @SerializedName("is_voucher")
    private boolean isVoucher;

    @SerializedName("is_expired")
    private boolean isExpired;

    @androidx.room.Ignore
    @SerializedName("current_usage")
    private CurrentUsage currentUsage;

    public static class CurrentUsage implements Serializable {
        @SerializedName("uptime")
        public String uptime;
        @SerializedName("bytes_in")
        public long bytesIn;
        @SerializedName("bytes_out")
        public long bytesOut;
        @SerializedName("comment")
        public String comment;
        @SerializedName("limit_uptime")
        public String limitUptime;
        @SerializedName("disabled")
        public String disabled;
    }

    public HotspotUser() {
    }

    public String getId() {
        if (id == null || id.isEmpty())
            return username;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUptime() {
        if ((uptime == null || uptime.isEmpty()) && currentUsage != null)
            return currentUsage.uptime;
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public long getBytesIn() {
        if (bytesIn == 0 && currentUsage != null)
            return currentUsage.bytesIn;
        return bytesIn;
    }

    public void setBytesIn(long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public long getBytesOut() {
        if (bytesOut == 0 && currentUsage != null)
            return currentUsage.bytesOut;
        return bytesOut;
    }

    public void setBytesOut(long bytesOut) {
        this.bytesOut = bytesOut;
    }

    public String getDataLimit() {
        if ((dataLimit == null || dataLimit.isEmpty()) && currentUsage != null)
            return currentUsage.limitUptime;
        return dataLimit;
    }

    public void setDataLimit(String dataLimit) {
        this.dataLimit = dataLimit;
    }

    public long getDataUsed() {
        return dataUsed;
    }

    public void setDataUsed(long dataUsed) {
        this.dataUsed = dataUsed;
    }

    public String getComment() {
        if ((comment == null || comment.isEmpty()) && currentUsage != null)
            return currentUsage.comment;
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getPasswordType() {
        return passwordType;
    }

    public void setPasswordType(String passwordType) {
        this.passwordType = passwordType;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isVoucher() {
        if (isVoucher)
            return true;
        // Check for common voucher patterns if flag is missing
        return (username != null
                && (username.startsWith("VOUCHER") || username.length() == 6 && username.matches("[A-Z0-9]{6}")));
    }

    public void setVoucher(boolean voucher) {
        isVoucher = voucher;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public CurrentUsage getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(CurrentUsage currentUsage) {
        this.currentUsage = currentUsage;
    }

    @SerializedName("details")
    private String details;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
