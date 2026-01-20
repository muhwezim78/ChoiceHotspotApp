package com.muhwezi.choicehotspot.models.pricing;

import com.google.gson.annotations.SerializedName;

/**
 * Pricing rate model.
 */
public class PricingRate {

    @SerializedName("id")
    private String id;

    @SerializedName("profile_name")
    private String profileName;

    @SerializedName("duration")
    private String duration;

    @SerializedName("price")
    private double price;

    @SerializedName("currency")
    private String currency;

    @SerializedName("is_active")
    private boolean isActive;

    public PricingRate() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getIdentifier() {
        return duration != null ? duration : id;
    }

    public double getAmount() {
        return price;
    }
}
