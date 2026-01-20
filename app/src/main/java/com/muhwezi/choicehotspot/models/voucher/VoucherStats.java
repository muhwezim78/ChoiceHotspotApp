package com.muhwezi.choicehotspot.models.voucher;

import com.google.gson.annotations.SerializedName;

/**
 * Voucher statistics model.
 */
public class VoucherStats {

    @SerializedName("total")
    private int total;

    @SerializedName("used")
    private int used;

    @SerializedName("unused")
    private int unused;

    @SerializedName("expired")
    private int expired;

    @SerializedName("total_revenue")
    private double totalRevenue;

    public VoucherStats() {
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public int getUnused() {
        return unused;
    }

    public void setUnused(int unused) {
        this.unused = unused;
    }

    public int getExpired() {
        return expired;
    }

    public void setExpired(int expired) {
        this.expired = expired;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
