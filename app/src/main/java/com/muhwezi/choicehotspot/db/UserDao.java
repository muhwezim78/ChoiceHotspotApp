package com.muhwezi.choicehotspot.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.muhwezi.choicehotspot.models.auth.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    User getUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("DELETE FROM users")
    void delete();
}
