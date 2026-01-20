package com.muhwezi.choicehotspot.models.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Subscription code model.
 */
public class SubscriptionCode {

    @SerializedName("code")
    private String code;

    @SerializedName("duration")
    private String duration;

    @SerializedName("package_type")
    private String packageType;

    @SerializedName("is_used")
    private boolean isUsed;

    @SerializedName("used_by")
    private String usedBy;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("expires_at")
    private String expiresAt;

    public SubscriptionCode() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
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
}
