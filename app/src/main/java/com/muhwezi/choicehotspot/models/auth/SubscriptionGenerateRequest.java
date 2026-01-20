package com.muhwezi.choicehotspot.models.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for subscription generation.
 */
public class SubscriptionGenerateRequest {

    @SerializedName("duration")
    private String duration;

    @SerializedName("package_type")
    private String packageType;

    @SerializedName("quantity")
    private int quantity;

    public SubscriptionGenerateRequest() {
    }

    public SubscriptionGenerateRequest(String duration, String packageType, int quantity) {
        this.duration = duration;
        this.packageType = packageType;
        this.quantity = quantity;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
