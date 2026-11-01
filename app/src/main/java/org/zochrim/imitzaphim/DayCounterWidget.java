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

/** One resizable widget, with static layouts matching each app color theme. */
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
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            updateAll(context);
        }
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(
            new ComponentName(context, DayCounterWidget.class));
        updateIds(context, manager, ids);
    }

    private static int normalized(int value, int length) {
        return ((value % length) + length) % length;
    }

    private static int chooseLayout(int widthDp, int heightDp, int theme) {
        // Keep the three stable size classes from the original single-provider widget.
        final int size;
        if (heightDp > 0 && heightDp < 70) {
            size = 1; // Medium: compact horizontal layout.
        } else if (widthDp > 0 && widthDp < 90) {
            size = 0; // Narrow: tall compact layout.
        } else {
            size = 2; // Large: Temple picture above the text.
        }
        switch (theme) {
            case 1:
                if (size == 0) return R.layout.day_counter_widget_narrow_green;
                if (size == 1) return R.layout.day_counter_widget_compact_green;
                return R.layout.day_counter_widget_large_green;
            case 2:
                if (size == 0) return R.layout.day_counter_widget_narrow_light;
                if (size == 1) return R.layout.day_counter_widget_compact_light;
                return R.layout.day_counter_widget_large_light;
            case 3:
                if (size == 0) return R.layout.day_counter_widget_narrow_dark;
                if (size == 1) return R.layout.day_counter_widget_compact_dark;
                return R.layout.day_counter_widget_large_dark;
            default:
                if (size == 0) return R.layout.day_counter_widget_narrow;
                if (size == 1) return R.layout.day_counter_widget_compact;
                return R.layout.day_counter_widget_large;
        }
    }

    private static void updateIds(Context context, AppWidgetManager manager, int[] ids) {
        if (ids == null || ids.length == 0) return;

        long days = DayCounter.daysElapsed();
        android.content.SharedPreferences prefs =
            context.getSharedPreferences("zochrim", Context.MODE_PRIVATE);
        int quoteIndex = normalized(prefs.getInt("quote", 0), VERSES.length);
        int theme = normalized(prefs.getInt("theme", 0), 4);

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

            RemoteViews views = new RemoteViews(context.getPackageName(),
                chooseLayout(widthDp, heightDp, theme));
            views.setTextViewText(R.id.widget_count,
                String.format(Locale.getDefault(), "%,d", days));
            views.setTextViewText(R.id.widget_label, "ימים בלי בית מקדש");
            views.setTextViewText(R.id.widget_quote, VERSES[quoteIndex]);
            views.setOnClickPendingIntent(R.id.widget_root, pending);
            manager.updateAppWidget(id, views);
        }
    }
}
