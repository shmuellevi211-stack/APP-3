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

/** One resizable AppWidgetProvider; mirrors the original working single-provider structure. */
public class DayCounterWidget extends AppWidgetProvider {
    private static final int[][] PALETTES = {
        {0xFF101B30, 0xFF1B2B49, 0xFFFFC94D, 0xFFFFF4D8},
        {0xFF102820, 0xFF1B4034, 0xFFD8B968, 0xFFF4F0E4},
        {0xFFEAF4F8, 0xFFFFFFFF, 0xFF317A91, 0xFF172D38},
        {0xFF151515, 0xFF202020, 0xFFE0E0E0, 0xFFFFFFFF}
    };

    private static final String[] VERSES = {
        "אִם אֶשְׁכָּחֵךְ יְרוּשָׁלָ‍ִם תִּשְׁכַּח יְמִינִי",
        "וְתֶחֱזֶינָה עֵינֵינוּ בְּשׁוּבְךָ לְצִיּוֹן בְּרַחֲמִים",
        "בּוֹנֵה יְרוּשָׁלַ‍ִם ה׳, נִדְחֵי יִשְׂרָאֵל יְכַנֵּס",
        "כָּל הַמִּתְאַבֵּל עַל יְרוּשָׁלַיִם זוֹכֶה וְרוֹאֶה בְּשִׂמְחָתָהּ"
    };

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        updateIds(context, manager, ids);
    }

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager,
                                                    int appWidgetId, Bundle options) {
        updateIds(context, manager, new int[]{appWidgetId});
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            updateAll(context);
        }
    }

    /** There is deliberately one registered widget provider, as in the older working releases. */
    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(
            new ComponentName(context, DayCounterWidget.class));
        updateIds(context, manager, ids);
    }

    private static int chooseLayout(int widthDp, int heightDp) {
        if (widthDp > 0 && widthDp < 90) return R.layout.day_counter_widget_narrow;
        if (heightDp > 0 && heightDp < 70) return R.layout.day_counter_widget_compact;
        if (widthDp > 0 && widthDp < 150) return R.layout.day_counter_widget_square;
        if (heightDp > 0 && heightDp < 150) return R.layout.day_counter_widget_compact;
        return R.layout.day_counter_widget_large;
    }

    private static void updateIds(Context context, AppWidgetManager manager, int[] ids) {
        if (ids == null || ids.length == 0) return;

        long days = DayCounter.daysElapsed();
        android.content.SharedPreferences prefs =
            context.getSharedPreferences("zochrim", Context.MODE_PRIVATE);
        int quoteIndex = prefs.getInt("quote", 0);
        quoteIndex = ((quoteIndex % VERSES.length) + VERSES.length) % VERSES.length;
        int theme = prefs.getInt("theme", 0);
        theme = ((theme % PALETTES.length) + PALETTES.length) % PALETTES.length;
        int[] palette = PALETTES[theme];

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
                chooseLayout(widthDp, heightDp));

            // Only standard RemoteViews methods are used; theme colors are applied to TextViews.
            views.setTextColor(R.id.widget_count, palette[2]);
            views.setTextColor(R.id.widget_label, palette[3]);
            views.setTextColor(R.id.widget_quote, palette[3]);
            views.setTextViewText(R.id.widget_count,
                String.format(Locale.getDefault(), "%,d", days));
            views.setTextViewText(R.id.widget_label, "ימים בלי בית מקדש");
            views.setTextViewText(R.id.widget_quote, VERSES[quoteIndex]);
            views.setImageViewResource(R.id.widget_temple_image, R.drawable.widget_temple_scene);
            views.setOnClickPendingIntent(R.id.widget_root, pending);
            manager.updateAppWidget(id, views);
        }
    }
}
