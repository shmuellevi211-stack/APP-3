package org.zochrim.imitzaphim;

import android.app.Activity;
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
import java.util.Locale;

/** Compact Hebrew UI designed for small screens and physical-key navigation (API 19+). */
public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private TextView countView, dateView, quoteView, sourceView;
    private Button quoteButton, themeButton;
    private int theme = 0, quoteIndex = 0;

    private final String[] quotes = {
        "אִם אֶשְׁכָּחֵךְ יְרוּשָׁלָ‍ִם תִּשְׁכַּח יְמִינִי",
        "וְתֶחֱזֶינָה עֵינֵינוּ בְּשׁוּבְךָ לְצִיּוֹן בְּרַחֲמִים",
        "בּוֹנֵה יְרוּשָׁלַ‍ִם ה׳, נִדְחֵי יִשְׂרָאֵל יְכַנֵּס",
        "כָּל הַמִּתְאַבֵּל עַל יְרוּשָׁלַיִם זוֹכֶה וְרוֹאֶה בְּשִׂמְחָתָהּ"
    };
    private final String[] sources = {
        "תהילים קל״ז, ה׳", "תפילת העמידה", "תהילים קמ״ז, ב׳", "תלמוד בבלי, תענית ל ע״ב"
    };
    private final int[][] palettes = {
        {0xFF101B30, 0xFF1B2B49, 0xFFFFC94D, 0xFFFFF4D8},
        {0xFF102820, 0xFF1B4034, 0xFFD8B968, 0xFFF4F0E4},
        {0xFFEAF4F8, 0xFFFFFFFF, 0xFF317A91, 0xFF172D38},
        {0xFF151515, 0xFF202020, 0xFFE0E0E0, 0xFFFFFFFF}
    };

    private int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("zochrim", MODE_PRIVATE);
        theme = prefs.getInt("theme", 0) % palettes.length;
        quoteIndex = prefs.getInt("quote", 0) % quotes.length;
        buildUi();
        refreshCounter();
    }

    private GradientDrawable shape(int color, int radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (strokeColor != 0) d.setStroke(dp(2), strokeColor);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setPadding(dp(5), dp(2), dp(5), dp(2));
        return t;
    }

    private Button button(String label, final int background, final int foreground) {
        final Button b = new Button(this);
        b.setText(label);
        b.setTextColor(foreground);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setMinHeight(dp(42));
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        b.setBackground(shape(background, 10, 0));
        b.setPadding(dp(7), dp(3), dp(7), dp(3));
        b.setOnFocusChangeListener((v, focused) ->
            b.setBackground(shape(background, 10, focused ? foreground : 0)));
        return b;
    }

    private void buildUi() {
        final int[] p = palettes[theme];
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(p[0]);
        root.setPadding(dp(8), dp(4), dp(8), dp(4));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setFocusable(false);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);

        content.addView(text("זוכרים ומצפים", 21, p[2], true),
            new LinearLayout.LayoutParams(-1, -2));
        content.addView(text("זיכרון ירושלים • תפילה לבניין הבית", 11, p[3], false),
            new LinearLayout.LayoutParams(-1, -2));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(5), dp(6), dp(5), dp(6));
        card.setBackground(shape(p[1], 14, p[2]));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, dp(7), 0, dp(5));
        content.addView(card, cp);

        card.addView(text("✦  ירושלים  ✦", 13, p[2], true),
            new LinearLayout.LayoutParams(-1, -2));
        countView = text("0", 42, p[2], true);
        countView.setSingleLine(true);
        card.addView(countView, new LinearLayout.LayoutParams(-1, -2));
        card.addView(text("ימים מאז", 13, p[3], true),
            new LinearLayout.LayoutParams(-1, -2));
        dateView = text("5 באוגוסט 70 לספירה", 12, p[3], false);
        card.addView(dateView, new LinearLayout.LayoutParams(-1, -2));

        quoteView = text("", 13, p[3], true);
        quoteView.setPadding(dp(3), dp(5), dp(3), dp(1));
        content.addView(quoteView, new LinearLayout.LayoutParams(-1, -2));
        sourceView = text("", 10, p[2], false);
        content.addView(sourceView, new LinearLayout.LayoutParams(-1, -2));

        quoteButton = button("4  •  משפט אחר מהמקורות", p[1], p[3]);
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1, -2);
        qp.setMargins(0, dp(5), 0, dp(3));
        content.addView(quoteButton, qp);
        quoteButton.setOnClickListener(v -> advanceQuote());

        themeButton = button("6  •  החלפת עיצוב", p[1], p[3]);
        content.addView(themeButton, new LinearLayout.LayoutParams(-1, -2));
        themeButton.setOnClickListener(v -> advanceTheme());

        quoteButton.setNextFocusDownId(themeButton.getId());
        themeButton.setNextFocusUpId(quoteButton.getId());
        TextView foot = text("ניווט: ↑ ↓ ואישור  •  מקשים 2/8/4/6", 9, p[3], false);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.setMargins(0, dp(3), 0, 0);
        content.addView(foot, fp);

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        updateQuote();
    }

    private void refreshCounter() {
        if (countView == null) return;
        long days = DayCounter.daysElapsed();
        countView.setText(String.format(Locale.getDefault(), "%,d", days));
        dateView.setText("5 באוגוסט 70 לספירה");
        updateQuote();
        DayCounterWidget.updateAll(this);
    }

    private void updateQuote() {
        if (quoteView != null) {
            quoteView.setText("״" + quotes[quoteIndex] + "״");
            sourceView.setText(sources[quoteIndex]);
        }
    }

    private void advanceQuote() {
        quoteIndex = (quoteIndex + 1) % quotes.length;
        prefs.edit().putInt("quote", quoteIndex).apply();
        updateQuote();
    }

    private void advanceTheme() {
        theme = (theme + 1) % palettes.length;
        prefs.edit().putInt("theme", theme).apply();
        buildUi();
        refreshCounter();
        if (quoteButton != null) quoteButton.requestFocus();
    }

    private void moveButtonFocus(boolean down) {
        View current = getCurrentFocus();
        if (current == quoteButton) themeButton.requestFocus();
        else quoteButton.requestFocus();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Phones with numeric keypads often expose 2/8/4/6/5 as the easiest controls.
        if (keyCode == KeyEvent.KEYCODE_2) { moveButtonFocus(false); return true; }
        if (keyCode == KeyEvent.KEYCODE_8) { moveButtonFocus(true); return true; }
        if (keyCode == KeyEvent.KEYCODE_4) { advanceQuote(); return true; }
        if (keyCode == KeyEvent.KEYCODE_6) { advanceTheme(); return true; }
        if (keyCode == KeyEvent.KEYCODE_5) {
            View current = getCurrentFocus();
            if (current != null && current.isClickable()) { current.performClick(); return true; }
            quoteButton.performClick();
            return true;
        }
        // DPAD and Enter are left to Android's native focus and button handling.
        return super.onKeyDown(keyCode, event);
    }
}
