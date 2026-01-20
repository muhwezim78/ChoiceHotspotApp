package com.muhwezi.choicehotspot.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.muhwezi.choicehotspot.models.user.HotspotUser;

import java.util.List;

@Dao
public interface HotspotUserDao {
    @Query("SELECT * FROM hotspot_users")
    List<HotspotUser> getAll();

    @Query("SELECT * FROM hotspot_users")
    androidx.lifecycle.LiveData<List<HotspotUser>> getAllLiveData();

    @Query("SELECT * FROM hotspot_users WHERE isActive = 1")
    List<HotspotUser> getActive();

    @Query("SELECT * FROM hotspot_users WHERE isActive = 1")
    androidx.lifecycle.LiveData<List<HotspotUser>> getActiveLiveData();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HotspotUser> users);

    @Query("DELETE FROM hotspot_users")
    void deleteAll();
}
