package com.muhwezi.choicehotspot;

import android.app.Application;
import com.muhwezi.choicehotspot.repository.ApiRepository;
import com.muhwezi.choicehotspot.utils.ThemeManager;

public class ChoiceHotspotApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Theme
        new ThemeManager(this).applySavedTheme();

        // Initialize API Repository
        ApiRepository.init(this);
    }
}
