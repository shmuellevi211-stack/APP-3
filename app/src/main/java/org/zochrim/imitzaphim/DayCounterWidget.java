package org.zochrim.imitzaphim;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.Locale;

/** Small home-screen widget; refreshed on date/time changes and by the platform periodically. */
public class DayCounterWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        updateIds(context, manager, appWidgetIds);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent == null ? null : intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)
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
        updateIds(context, manager, ids);
    }

    private static void updateIds(Context context, AppWidgetManager manager, int[] ids) {
        if (ids == null) return;
        long days = DayCounter.daysElapsed();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.day_counter_widget);
        views.setTextViewText(R.id.widget_count, String.format(Locale.getDefault(), "%,d", days));
        views.setTextViewText(R.id.widget_label, "ימים בלי בית מקדש");
        String[] verses = {
            "אם אשכחך ירושלים תשכח ימיני",
            "ותחזינה עינינו בשובך לציון ברחמים",
            "בונה ירושלים ה׳ נדחי ישראל יכנס",
            "כל המתאבל על ירושלים זוכה ורואה בשמחתה"
        };
        int verseIndex = context.getSharedPreferences("zochrim", Context.MODE_PRIVATE).getInt("quote", 0);
        views.setTextViewText(R.id.widget_quote, verses[Math.abs(verseIndex) % verses.length]);
        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(
            context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_root, pending);
        for (int id : ids) manager.updateAppWidget(id, views);
    }
}
