package org.zochrim.imitzaphim;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import java.util.Locale;

/** One resizable widget with independent color and persistent size-class selection. */
public class DayCounterWidget extends AppWidgetProvider {
    private static final String ACTION_BOUNDARY =
        "org.zochrim.imitzaphim.ACTION_COUNTER_BOUNDARY";
    private static final String SIZE_KEY_PREFIX = "widget_size_";
    private static final int SIZE_NARROW = 0;
    private static final int SIZE_COMPACT = 1;
    private static final int SIZE_LARGE = 2;

    private static final String[] VERSES = {
        "אִם אֶשְׁכָּחֵךְ יְרוּשָׁלָ‍ִם תִּשְׁכַּח יְמִינִי",
        "וְתֶחֱזֶינָה עֵינֵינוּ בְּשׁוּבְךָ לְצִיּוֹן בְּרַחֲמִים",
        "בּוֹנֵה יְרוּשָׁלַ‍ִם ה׳, נִדְחֵי יִשְׂרָאֵל יְכַנֵּס",
        "כָּל הַמִּתְאַבֵּל עַל יְרוּשָׁלַיִם זוֹכֶה וְרוֹאֶה בְּשִׂמְחָתָהּ"
    };

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        updateIds(context, manager, appWidgetIds);
        if (appWidgetIds != null && appWidgetIds.length > 0) scheduleNextUpdate(context);
    }

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager,
                                                    int appWidgetId, Bundle newOptions) {
        // The launcher calls this after a resize. Persist the selected layout class so a reboot
        // or the launcher's first refresh cannot silently change the widget's appearance.
        rememberSizeClass(context, appWidgetId, newOptions);
        updateIds(context, manager, new int[]{appWidgetId});
        scheduleNextUpdate(context);
    }

    @Override public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        if (appWidgetIds == null) return;
        SharedPreferences.Editor editor =
            context.getSharedPreferences("zochrim", Context.MODE_PRIVATE).edit();
        for (int id : appWidgetIds) editor.remove(SIZE_KEY_PREFIX + id);
        editor.apply();
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

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(
            new ComponentName(context, DayCounterWidget.class));
        if (ids != null && ids.length > 0) {
            updateIds(context, manager, ids);
            scheduleNextUpdate(context);
        }
    }

    /** Schedules the next midnight or estimated Jerusalem nightfall boundary. */
    public static void scheduleNextUpdate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("zochrim", Context.MODE_PRIVATE);
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

    private static int inferSizeClass(Bundle options) {
        int widthDp = options == null ? 0
            : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int heightDp = options == null ? 0
            : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        if (widthDp <= 0 && options != null) {
            widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        }
        if (heightDp <= 0 && options != null) {
            heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        }

        if (heightDp > 0 && heightDp < 70 && widthDp >= 90) return SIZE_COMPACT;
        if (widthDp > 0 && widthDp < 90) return SIZE_NARROW;
        if (heightDp > 0 && heightDp < 70) return SIZE_COMPACT;
        return SIZE_LARGE;
    }

    private static void rememberSizeClass(Context context, int appWidgetId, Bundle options) {
        int sizeClass = inferSizeClass(options);
        context.getSharedPreferences("zochrim", Context.MODE_PRIVATE)
            .edit().putInt(SIZE_KEY_PREFIX + appWidgetId, sizeClass).apply();
    }

    private static int savedSizeClass(Context context, int appWidgetId, Bundle options) {
        SharedPreferences prefs = context.getSharedPreferences("zochrim", Context.MODE_PRIVATE);
        String key = SIZE_KEY_PREFIX + appWidgetId;
        int saved = prefs.getInt(key, -1);
        if (saved >= SIZE_NARROW && saved <= SIZE_LARGE) return saved;

        int inferred = inferSizeClass(options);
        prefs.edit().putInt(key, inferred).apply();
        return inferred;
    }

    private static int chooseLayout(int sizeClass, int theme) {
        switch (theme) {
            case 1:
                if (sizeClass == SIZE_NARROW) return R.layout.day_counter_widget_narrow_green;
                if (sizeClass == SIZE_COMPACT) return R.layout.day_counter_widget_compact_green;
                return R.layout.day_counter_widget_large_green;
            case 2:
                if (sizeClass == SIZE_NARROW) return R.layout.day_counter_widget_narrow_light;
                if (sizeClass == SIZE_COMPACT) return R.layout.day_counter_widget_compact_light;
                return R.layout.day_counter_widget_large_light;
            case 3:
                if (sizeClass == SIZE_NARROW) return R.layout.day_counter_widget_narrow_dark;
                if (sizeClass == SIZE_COMPACT) return R.layout.day_counter_widget_compact_dark;
                return R.layout.day_counter_widget_large_dark;
            default:
                if (sizeClass == SIZE_NARROW) return R.layout.day_counter_widget_narrow;
                if (sizeClass == SIZE_COMPACT) return R.layout.day_counter_widget_compact;
                return R.layout.day_counter_widget_large;
        }
    }

    private static void updateIds(Context context, AppWidgetManager manager, int[] ids) {
        if (ids == null || ids.length == 0) return;

        SharedPreferences prefs =
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

        for (int id : ids) {
            Bundle options = manager.getAppWidgetOptions(id);
            int sizeClass = savedSizeClass(context, id, options);
            RemoteViews views = new RemoteViews(
                context.getPackageName(), chooseLayout(sizeClass, widgetTheme));
            views.setTextViewText(R.id.widget_count,
                String.format(Locale.getDefault(), "%,d", days));
            views.setTextViewText(R.id.widget_label, "ימים בלי בית מקדש");
            views.setTextViewText(R.id.widget_quote, VERSES[quoteIndex]);
            views.setOnClickPendingIntent(R.id.widget_root, pending);
            manager.updateAppWidget(id, views);
        }
    }
}
