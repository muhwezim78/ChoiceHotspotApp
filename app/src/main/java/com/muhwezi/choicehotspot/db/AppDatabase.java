package com.muhwezi.choicehotspot.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.muhwezi.choicehotspot.models.auth.User;
import com.muhwezi.choicehotspot.models.profile.Profile;
import com.muhwezi.choicehotspot.models.user.HotspotUser;
import com.muhwezi.choicehotspot.models.voucher.Voucher;

@Database(entities = { Voucher.class, Profile.class, User.class, PricingEntity.class,
        HotspotUser.class }, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract VoucherDao voucherDao();

    public abstract ProfileDao profileDao();

    public abstract UserDao userDao(); // Added UserDao

    public abstract PricingDao pricingDao(); // Added PricingDao

    public abstract HotspotUserDao hotspotUserDao(); // Added HotspotUserDao

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "choice_hotspot_db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // Only for the sake of simplicity in repository
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
