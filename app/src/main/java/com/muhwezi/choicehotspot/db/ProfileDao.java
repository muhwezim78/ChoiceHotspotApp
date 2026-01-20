package com.muhwezi.choicehotspot.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.muhwezi.choicehotspot.models.profile.Profile;

import java.util.List;

@Dao
public interface ProfileDao {
    @Query("SELECT * FROM profiles")
    List<Profile> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Profile> profiles);

    @Query("DELETE FROM profiles")
    void deleteAll();
}
