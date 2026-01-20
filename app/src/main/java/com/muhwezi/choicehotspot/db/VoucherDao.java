package com.muhwezi.choicehotspot.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.muhwezi.choicehotspot.models.voucher.Voucher;

import java.util.List;

@Dao
public interface VoucherDao {
    @Query("SELECT * FROM vouchers")
    List<Voucher> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Voucher> vouchers);

    @Query("DELETE FROM vouchers")
    void deleteAll();
}
