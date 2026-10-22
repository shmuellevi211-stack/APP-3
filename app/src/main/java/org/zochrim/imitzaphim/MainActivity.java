package org.zochrim.imitzaphim;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Lightweight offline-first UI using platform widgets for API 19 compatibility. */
public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private TextView countView, dateView, quoteView, sourceView;
    private int theme = 0, quoteIndex = 0;
    private final String[] quotes = {
        "אִם אֶשְׁכָּחֵךְ יְרוּשָׁלָ‍ִם תִּשְׁכַּח יְמִינִי",
        "וְתֶחֱזֶינָה עֵינֵינוּ בְּשׁוּבְךָ לְצִיּוֹן בְּרַחֲמִים",
        "בּוֹנֵה יְרוּשָׁלַ‍ִם ה׳, נִדְחֵי יִשְׂרָאֵל יְכַנֵּס",
        "כָּל הַמִּתְאַבֵּל עַל יְרוּשָׁלַיִם זוֹכֶה וְרוֹאֶה בְּשִׂמְחָתָהּ"
    };
    private final String[] sources = {"תהילים קל״ז, ה׳", "תפילת העמידה", "תהילים קמ״ז, ב׳", "תלמוד בבלי, תענית ל ע״ב"};
    private final int[][] palettes = {
        {0xFF101318, 0xFF202631, 0xFFD6B56D, 0xFFF7F3E8},
        {0xFF102820, 0xFF1B4034, 0xFFD8B968, 0xFFF4F0E4},
        {0xFFEAF4F8, 0xFFFFFFFF, 0xFF317A91, 0xFF172D38},
        {0xFF151515, 0xFF202020, 0xFFE0E0E0, 0xFFFFFFFF}
    };
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("zochrim", MODE_PRIVATE);
        theme = prefs.getInt("theme", 0);
        quoteIndex = prefs.getInt("quote", 0) % quotes.length;
        buildUi();
        refreshCounter();
    }

    private GradientDrawable shape(int color, int radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        if (strokeColor != 0) d.setStroke(dp(1), strokeColor);
        return d;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER); t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setPadding(dp(8), dp(5), dp(8), dp(5)); return t;
    }
    private Button button(String label, int accent, int foreground) {
        Button b = new Button(this); b.setText(label); b.setTextColor(foreground); b.setTextSize(14);
        b.setAllCaps(false); b.setMinHeight(dp(48)); b.setBackground(shape(accent, 14, 0));
        b.setPadding(dp(10), dp(6), dp(10), dp(6)); return b;
    }
    private void buildUi() {
        final int[] p = palettes[theme];
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(p[0]); root.setPadding(dp(16), dp(12), dp(16), dp(12));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(text("זוכרים ומצפים", 24, p[2], true), new LinearLayout.LayoutParams(-1, -2));
        content.addView(text("זיכרון ירושלים • תפילה לבניין הבית", 13, p[3], false), new LinearLayout.LayoutParams(-1, -2));
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER); card.setPadding(dp(12), dp(18), dp(12), dp(18));
        card.setBackground(shape(p[1], 22, p[2]));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, dp(18), 0, dp(14)); content.addView(card, cp);
        card.addView(text("✦  ירושלים  ✦", 16, p[2], true), new LinearLayout.LayoutParams(-1, -2));
        countView = text("0", 58, p[2], true);
        card.addView(countView, new LinearLayout.LayoutParams(-1, -2));
        card.addView(text("ימים מאז תאריך ההתחלה", 17, p[3], true), new LinearLayout.LayoutParams(-1, -2));
        dateView = text("תאריך התחלה", 13, p[3], false);
        card.addView(dateView, new LinearLayout.LayoutParams(-1, -2));
        Button dateButton = button("בחירת תאריך התחלה", p[2], p[0]);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(0, dp(4), 0, dp(4)); content.addView(dateButton, bp);
        dateButton.setOnClickListener(v -> chooseDate());
        quoteView = text("", 17, p[3], true); quoteView.setPadding(dp(8), dp(18), dp(8), dp(4));
        content.addView(quoteView, new LinearLayout.LayoutParams(-1, -2));
        sourceView = text("", 12, p[2], false);
        content.addView(sourceView, new LinearLayout.LayoutParams(-1, -2));
        Button quoteButton = button("משפט אחר מהמקורות", p[1], p[3]);
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1, -2);
        qp.setMargins(0, dp(8), 0, dp(6)); content.addView(quoteButton, qp);
        quoteButton.setOnClickListener(v -> {
            quoteIndex = (quoteIndex + 1) % quotes.length;
            prefs.edit().putInt("quote", quoteIndex).apply(); updateQuote();
        });
        Button themeButton = button("החלפת עיצוב", p[1], p[3]);
        content.addView(themeButton, new LinearLayout.LayoutParams(-1, -2));
        themeButton.setOnClickListener(v -> {
            theme = (theme + 1) % palettes.length; prefs.edit().putInt("theme", theme).apply();
            buildUi(); refreshCounter();
        });
        TextView foot = text("פועל ללא אינטרנט • ההגדרות נשמרות במכשיר", 11, p[3], false);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.setMargins(0, dp(14), 0, 0); content.addView(foot, fp);
        scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root); updateQuote();
    }
    private Calendar dayStart(Calendar source) {
        Calendar c = (Calendar) source.clone(); c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); return c;
    }
    private Calendar startDate() {
        Calendar today = dayStart(Calendar.getInstance());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(prefs.getLong("start", today.getTimeInMillis()));
        return dayStart(c);
    }
    private void refreshCounter() {
        if (countView == null) return;
        Calendar now = dayStart(Calendar.getInstance()), start = startDate();
        long days = (now.getTimeInMillis() - start.getTimeInMillis()) / 86400000L;
        if (days < 0) { countView.setText("—"); dateView.setText("תאריך ההתחלה בעתיד"); }
        else {
            countView.setText(String.format(Locale.getDefault(), "%d", days));
            dateView.setText("מתאריך " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(start.getTime()));
        }
        updateQuote();
    }
    private void chooseDate() {
        Calendar c = startDate();
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chosen = Calendar.getInstance(); chosen.clear(); chosen.set(year, month, day);
            prefs.edit().putLong("start", chosen.getTimeInMillis()).apply(); refreshCounter();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void updateQuote() {
        if (quoteView != null) { quoteView.setText("״" + quotes[quoteIndex] + "״"); sourceView.setText(sources[quoteIndex]); }
    }
    @Override protected void onResume() { super.onResume(); if (prefs != null) refreshCounter(); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            quoteIndex = (quoteIndex + 1) % quotes.length; prefs.edit().putInt("quote", quoteIndex).apply();
            updateQuote(); return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            theme = (theme + 1) % palettes.length; prefs.edit().putInt("theme", theme).apply();
            buildUi(); refreshCounter(); return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
