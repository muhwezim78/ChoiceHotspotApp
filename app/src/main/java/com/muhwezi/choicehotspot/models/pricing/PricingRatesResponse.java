package com.muhwezi.choicehotspot.models.pricing;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class PricingRatesResponse {
    @SerializedName("base_rates")
    private Map<String, Double> baseRates;

    public Map<String, Double> getBaseRates() {
        return baseRates;
    }

    public void setBaseRates(Map<String, Double> baseRates) {
        this.baseRates = baseRates;
    }
}
