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

/** Home-screen counter widget; resize-aware and shared by all preset-size providers. */
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
        DayCounterWidget[] providers = new DayCounterWidget[] {
            new DayCounterWidget(),
            new Widget1x3Provider(), new Widget1x4Provider(),
            new Widget2x2Provider(), new Widget3x2Provider(), new Widget3x3Provider(),
            new Widget4x1Provider(), new Widget4x2Provider(), new Widget4x3Provider(),
            new Widget5x1Provider()
        };
        for (DayCounterWidget provider : providers) {
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, provider.getClass()));
            provider.updateIds(context, manager, ids);
        }
    }

    protected int chooseLayout(int widthDp, int heightDp) {
        if (widthDp > 0 && widthDp < 90) {
            return R.layout.day_counter_widget_narrow;
        }
        if (widthDp > 0 && widthDp < 150 && heightDp > 0 && heightDp < 150) {
            return R.layout.day_counter_widget_square;
        }
        if (heightDp > 0 && heightDp < 150) {
            return R.layout.day_counter_widget_compact;
        }
        return R.layout.day_counter_widget_large;
    }

    private void updateIds(Context context, AppWidgetManager manager, int[] ids) {
        if (ids == null || ids.length == 0) return;
        long days = DayCounter.daysElapsed();
        int quoteIndex = context.getSharedPreferences("zochrim", Context.MODE_PRIVATE)
            .getInt("quote", 0);
        quoteIndex = ((quoteIndex % VERSES.length) + VERSES.length) % VERSES.length;
        String verse = VERSES[quoteIndex];

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
