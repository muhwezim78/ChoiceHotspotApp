package com.muhwezi.choicehotspot.models.voucher;

import com.google.gson.annotations.SerializedName;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Voucher model.
 */
@Entity(tableName = "vouchers")
public class Voucher implements Serializable {

    @SerializedName("id")
    private String id = "";

    @PrimaryKey
    @NonNull
    @SerializedName("code")
    private String code = "";

    @SerializedName("profile")
    private String profile;

    @SerializedName("price")
    private double price;

    @SerializedName("duration")
    private String duration;

    @SerializedName("data_limit")
    private String dataLimit;

    @SerializedName("is_used")
    private boolean isUsed;

    @SerializedName("used_by")
    private String usedBy;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("comment")
    private String comment;

    @SerializedName("status")
    private String status;

    @SerializedName("password")
    private String password;

    @SerializedName("uptime_limit")
    private String uptimeLimit;

    @SerializedName("bytes_used")
    private long bytesUsed;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("customer_contact")
    private String customerContact;

    @SerializedName("activated_at")
    private String activatedAt;

    public Voucher() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDataLimit() {
        return dataLimit;
    }

    public void setDataLimit(String dataLimit) {
        this.dataLimit = dataLimit;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public String getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(String usedBy) {
        this.usedBy = usedBy;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUptimeLimit() {
        return uptimeLimit;
    }

    public void setUptimeLimit(String uptimeLimit) {
        this.uptimeLimit = uptimeLimit;
    }

    public long getBytesUsed() {
        return bytesUsed;
    }

    public void setBytesUsed(long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(String activatedAt) {
        this.activatedAt = activatedAt;
    }

    public boolean isExpired() {
        if (expiresAt == null || expiresAt.isEmpty())
            return false;
        try {
            // Logic to compare current time with expiresAt
            // For now, simple string comparison or assume backend handles most,
            // but we can parse if we know the format (likely ISO 8601)
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    java.util.Locale.US);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date expiryDate = sdf.parse(expiresAt);
            return new java.util.Date().after(expiryDate);
        } catch (Exception e) {
            return false;
        }
    }
}
