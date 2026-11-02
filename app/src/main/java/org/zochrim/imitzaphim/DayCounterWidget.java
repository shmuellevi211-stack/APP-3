package org.zochrim.imitzaphim;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import java.util.Locale;

/**
 * Medium widget provider. A separate Large provider is listed in the Android widget picker,
 * so choosing a design does not depend on unreliable launcher-specific resize thresholds.
 */
public class DayCounterWidget extends AppWidgetProvider {
    private static final String ACTION_BOUNDARY =
        "org.zochrim.imitzaphim.ACTION_COUNTER_BOUNDARY";
    protected static final int SIZE_MEDIUM = 0;
    protected static final int SIZE_LARGE = 1;

    private static final String[] VERSES = {
        "אִם אֶשְׁכָּחֵךְ יְרוּשָׁלָ‍ִם תִּשְׁכַּח יְמִינִי",
        "וְתֶחֱזֶינָה עֵינֵינוּ בְּשׁוּבְךָ לְצִיּוֹן בְּרַחֲמִים",
        "בּוֹנֵה יְרוּשָׁלַ‍ִם ה׳, נִדְחֵי יִשְׂרָאֵל יְכַנֵּס",
        "כָּל הַמִּתְאַבֵּל עַל יְרוּשָׁלַיִם זוֹכֶה וְרוֹאֶה בְּשִׂמְחָתָהּ"
    };

    /** The base provider is the medium layout. */
    protected int getLayoutSizeClass() {
        return SIZE_MEDIUM;
    }

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        updateIds(context, manager, appWidgetIds, getLayoutSizeClass());
        if (appWidgetIds != null && appWidgetIds.length > 0) scheduleNextUpdate(context);
    }

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager,
                                                    int appWidgetId, Bundle newOptions) {
        // Keep this provider's layout identity when the launcher changes its outer bounds.
        updateIds(context, manager, new int[]{appWidgetId}, getLayoutSizeClass());
        scheduleNextUpdate(context);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent == null ? null : intent.getAction();
        if (ACTION_BOUNDARY.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            updateAll(context);
        }
    }

    /** Updates all installed widget types from one shared set of preferences. */
    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] mediumIds = manager.getAppWidgetIds(
            new ComponentName(context, DayCounterWidget.class));
        int[] largeIds = manager.getAppWidgetIds(
            new ComponentName(context, DayCounterLargeWidget.class));

        boolean hasWidgets = (mediumIds != null && mediumIds.length > 0)
            || (largeIds != null && largeIds.length > 0);
        if (!hasWidgets) return;

        updateIds(context, manager, mediumIds, SIZE_MEDIUM);
        updateIds(context, manager, largeIds, SIZE_LARGE);
        scheduleNextUpdate(context);
    }

    /** Schedules the next midnight or estimated Jerusalem nightfall boundary. */
    public static void scheduleNextUpdate(Context context) {
        android.content.SharedPreferences prefs =
            context.getSharedPreferences("zochrim", Context.MODE_PRIVATE);
        boolean nightfallMode = prefs.getBoolean("nightfall_mode", false);
        long triggerAt = DayCounter.nextBoundaryMillis(nightfallMode);

        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, DayCounterWidget.class);
        intent.setAction(ACTION_BOUNDARY);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent operation = PendingIntent.getBroadcast(context, 7701, intent, flags);

        alarmManager.cancel(operation);
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, operation);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, operation);
        }
    }

    private static int normalized(int value, int length) {
        return ((value % length) + length) % length;
    }

    private static int chooseLayout(int sizeClass, int theme) {
        if (sizeClass == SIZE_LARGE) {
            switch (theme) {
                case 1: return R.layout.day_counter_widget_large_green;
                case 2: return R.layout.day_counter_widget_large_light;
                case 3: return R.layout.day_counter_widget_large_dark;
                default: return R.layout.day_counter_widget_large;
            }
        }
        switch (theme) {
            case 1: return R.layout.day_counter_widget_compact_green;
            case 2: return R.layout.day_counter_widget_compact_light;
            case 3: return R.layout.day_counter_widget_compact_dark;
            default: return R.layout.day_counter_widget_compact;
        }
    }

    private static void updateIds(Context context, AppWidgetManager manager, int[] ids,
                                  int sizeClass) {
        if (ids == null || ids.length == 0) return;

        android.content.SharedPreferences prefs =
            context.getSharedPreferences("zochrim", Context.MODE_PRIVATE);
        long days = DayCounter.daysElapsed(prefs.getBoolean("nightfall_mode", false));
        int quoteIndex = normalized(prefs.getInt("quote", 0), VERSES.length);
        int appTheme = normalized(prefs.getInt("theme", 0), 4);
        int selectedWidgetTheme = prefs.getInt("widget_theme", -1);
        int widgetTheme = selectedWidgetTheme < 0
            ? appTheme : normalized(selectedWidgetTheme, 4);

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending = PendingIntent.getActivity(context, 0, launch, pendingFlags);

        RemoteViews views = new RemoteViews(
            context.getPackageName(), chooseLayout(sizeClass, widgetTheme));
        views.setTextViewText(R.id.widget_count,
            String.format(Locale.getDefault(), "%,d", days));
        views.setTextViewText(R.id.widget_label, "ימים בלי בית מקדש");
        views.setTextViewText(R.id.widget_quote, VERSES[quoteIndex]);
        views.setOnClickPendingIntent(R.id.widget_root, pending);

        for (int id : ids) manager.updateAppWidget(id, views);
    }
}
