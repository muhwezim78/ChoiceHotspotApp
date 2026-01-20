package com.muhwezi.choicehotspot.models.financial;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response wrapper for revenue data.
 */
public class RevenueResponse {

    @SerializedName("revenue_data")
    private List<RevenueData> revenueData;

    public List<RevenueData> getRevenueData() {
        return revenueData;
    }

    public void setRevenueData(List<RevenueData> revenueData) {
        this.revenueData = revenueData;
    }
}
