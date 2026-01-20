package com.muhwezi.choicehotspot.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.muhwezi.choicehotspot.MainActivity;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.utils.ApiUtils;

/**
 * Implementation of App Widget functionality.
 */
public class DashboardWidget extends AppWidgetProvider {

    private static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String PREF_REVENUE = "widget_revenue";
    public static final String PREF_ACTIVE_USERS = "widget_active_users";
    public static final String PREF_LAST_UPDATED = "widget_last_updated";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dashboard);

        // Read data from Shared Preferences (written by App/DashboardFragment)
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        String revenue = prefs.getString(PREF_REVENUE, "RW: --");
        String users = prefs.getString(PREF_ACTIVE_USERS, "Users: --");
        String lastUpdated = prefs.getString(PREF_LAST_UPDATED, "--:--");

        views.setTextViewText(R.id.widget_revenue, revenue);
        views.setTextViewText(R.id.widget_active_users, users);
        views.setTextViewText(R.id.widget_last_updated, lastUpdated);

        // Open App on click
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("action", "view_dashboard");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_revenue, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
