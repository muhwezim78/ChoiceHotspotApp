package com.muhwezi.choicehotspot.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.annotation.NonNull;

@Entity(tableName = "pricing_map")
public class PricingEntity {
    @PrimaryKey
    @NonNull
    private String id = "current_rates";

    private String ratesJson;

    public PricingEntity() {
    }

    @Ignore
    public PricingEntity(String ratesJson) {
        this.ratesJson = ratesJson;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getRatesJson() {
        return ratesJson;
    }

    public void setRatesJson(String ratesJson) {
        this.ratesJson = ratesJson;
    }
}
