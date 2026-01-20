package com.muhwezi.choicehotspot.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface PricingDao {
    @Query("SELECT * FROM pricing_map WHERE id = 'current_rates' LIMIT 1")
    PricingEntity getRates();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRates(PricingEntity entry);
}
