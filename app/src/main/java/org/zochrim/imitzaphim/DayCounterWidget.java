package org.zochrim.imitzaphim;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import java.util.Locale;

/** Resizable Temple-day widget with size-aware layouts for narrow, compact and large placements. */
public class DayCounterWidget extends AppWidgetProvider {
    private static final String[] VERSES = {
        "אִם אֶשְׁכָּחֵךְ יְרוּשָׁלָ‍ִם תִּשְׁכַּח יְמִינִי",
        "וְתֶחֱזֶינָה עֵינֵינוּ בְּשׁוּבְךָ לְצִיּוֹן בְּרַחֲמִים",
        "בּוֹנֵה יְרוּשָׁלַ‍ִם ה׳, נִדְחֵי יִשְׂרָאֵל יְכַנֵּס",
        "כָּל הַמִּתְאַבֵּל עַל יְרוּשָׁלַיִם זוֹכֶה וְרוֹאֶה בְּשִׂמְחָתָהּ"
    };

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        updateIds(context, manager, appWidgetIds);
    }

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager,
                                                    int appWidgetId, Bundle newOptions) {
        updateIds(context, manager, new int[]{appWidgetId});
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
        int quoteIndex = context.getSharedPreferences("zochrim", Context.MODE_PRIVATE)
            .getInt("quote", 0);
        String verse = VERSES[Math.abs(quoteIndex) % VERSES.length];

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(
            context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT);

        for (int id : ids) {
            Bundle options = manager.getAppWidgetOptions(id);
            int widthDp = options == null ? 0 :
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int heightDp = options == null ? 0 :
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

            int layout;
            if (heightDp > 0 && heightDp < 70) {
                layout = R.layout.day_counter_widget_compact;
            } else if (widthDp > 0 && widthDp < 90) {
                layout = R.layout.day_counter_widget_narrow;
            } else {
                layout = R.layout.day_counter_widget_large;
            }

            RemoteViews views = new RemoteViews(context.getPackageName(), layout);
            views.setTextViewText(R.id.widget_count,
                String.format(Locale.getDefault(), "%,d", days));
            views.setTextViewText(R.id.widget_label, "ימים בלי בית מקדש");
            views.setTextViewText(R.id.widget_quote, verse);
            views.setImageViewResource(R.id.widget_temple_image, R.drawable.ic_launcher);
            views.setOnClickPendingIntent(R.id.widget_root, pending);
            manager.updateAppWidget(id, views);
        }
    }
}
