package com.muhwezi.choicehotspot.models.financial;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Revenue data for charts.
 */
public class RevenueData {

    @SerializedName("date")
    private String date;

    @SerializedName("revenue")
    private double revenue;

    @SerializedName("voucher_count")
    private int voucherCount;

    public RevenueData() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public int getVoucherCount() {
        return voucherCount;
    }

    public void setVoucherCount(int voucherCount) {
        this.voucherCount = voucherCount;
    }
}
