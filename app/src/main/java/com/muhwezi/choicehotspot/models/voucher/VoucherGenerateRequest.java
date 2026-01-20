package com.muhwezi.choicehotspot.models.voucher;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for voucher generation.
 */
public class VoucherGenerateRequest {

    @SerializedName("profile_name")
    private String profileName;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("customer_contact")
    private String customerContact;

    @SerializedName("password_type")
    private String passwordType; // "blank", "same", "custom"

    public VoucherGenerateRequest() {
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public String getPasswordType() {
        return passwordType;
    }

    public void setPasswordType(String passwordType) {
        this.passwordType = passwordType;
    }
}
