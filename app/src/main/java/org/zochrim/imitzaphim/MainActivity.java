package org.zochrim.imitzaphim;

import android.app.Activity;
import android.app.AlertDialog;
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

        content.addView(text("בצאתי מירושלים", 21, p[2], true),
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
        card.addView(text("ימים בלי בית מקדש", 13, p[3], true),
            new LinearLayout.LayoutParams(-1, -2));
        dateView = text("", 1, p[3], false); // Date is intentionally hidden.
        dateView.setVisibility(View.GONE);
        card.addView(dateView, new LinearLayout.LayoutParams(0, 0));

        quoteView = text("", 13, p[3], true);
        quoteView.setPadding(dp(3), dp(5), dp(3), dp(1));
        content.addView(quoteView, new LinearLayout.LayoutParams(-1, -2));
        sourceView = text("", 10, p[2], false);
        content.addView(sourceView, new LinearLayout.LayoutParams(-1, -2));

        quoteButton = button("4  •  משפט אחר מהמקורות", p[1], p[3]);
        quoteButton.setId(View.generateViewId());
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1, -2);
        qp.setMargins(0, dp(5), 0, dp(3));
        content.addView(quoteButton, qp);
        quoteButton.setOnClickListener(v -> advanceQuote());

        themeButton = button("6  •  החלפת עיצוב", p[1], p[3]);
        themeButton.setId(View.generateViewId());
        content.addView(themeButton, new LinearLayout.LayoutParams(-1, -2));
        themeButton.setOnClickListener(v -> advanceTheme());

        Button guideButton = button("מדריך הפעלה ואודות", p[1], p[3]);
        content.addView(guideButton, new LinearLayout.LayoutParams(-1, -2));
        guideButton.setOnClickListener(v -> showGuideAndAbout());

        quoteButton.setNextFocusDownId(themeButton.getId());
        themeButton.setNextFocusUpId(quoteButton.getId());
        TextView foot = text("ניווט: חצים ואישור  •  2/8 מעבר  •  4/6 פעולה  •  0 רענון", 9, p[3], false);
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
        dateView.setText("");
        updateQuote();
        DayCounterWidget.updateAll(this);
    }

    private void showGuideAndAbout() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(8), dp(6), dp(8), dp(6));
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        addAboutSection(page, "מדריך הפעלה",
            "• המספר הראשי מציג את מניין הימים מתאריך הייחוס 5 באוגוסט 70 לספירה.\n" +
            "• ״משפט אחר מהמקורות״ מחליף את הפסוק המוצג.\n" +
            "• ״החלפת עיצוב״ מחליפה בין ערכות הצבעים.\n" +
            "• מקשי 2/8 מעבירים מיקוד, 4 מחליף פסוק, 6 מחליף עיצוב, 5/אישור מפעיל כפתור ו־0 מרענן את המניין.\n" +
            "• להוספת ווידג׳ט: לחיצה ארוכה במסך הבית, פתיחת ווידג׳טים ובחירת הגודל הרצוי.");

        addAboutSection(page, "גדלי ווידג׳ט",
            "אפשר לבחור פריסה מראש: 1×3, 1×4, 2×2, 3×2, 3×3, 4×1, 4×2, 4×3 או 5×1. " +
            "אפשרויות השינוי בפועל תלויות במשגר מסך הבית של המכשיר.");

        addAboutSection(page, "חורבן בית המקדש השני",
            "לפי המסורת, בית המקדש השני חרב בט׳ באב בשנת ג׳תת״ל (3830 לבריאת העולם), בשנת 70 לספירה.\n" +
            "בהמרה חשבונית לאחור, התאריך הלועזי המקביל מוערך בקירוב כ־2 באוגוסט 70, אך תאריכים אזרחיים מתקופה זו אינם ודאיים.\n" +
            "לצורך מניין הימים, האפליקציה משתמשת בתאריך ייחוס קבוע: 5 באוגוסט 70 לספירה.\n" +
            "לפי המסורת, בית המקדש השני עמד על תילו 420 שנה. נכון לשנת 2026 חלפו בקירוב 1,956 שנים מאז החורבן.");

        addAboutSection(page, "אודות",
            "בצאתי מירושלים — גרסה 2.4\n" +
            "פותחה על ידי שמואל_770 מפורום מתמחים טופ.");

        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        scroller.addView(page);
        new AlertDialog.Builder(this)
            .setTitle("מדריך הפעלה ואודות")
            .setView(scroller)
            .setPositiveButton("סגירה", null)
            .show();
    }

    private void addAboutSection(LinearLayout parent, String heading, String details) {
        int[] p = palettes[theme];
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(8), dp(10), dp(8));
        card.setBackground(shape(p[1], 10, p[2]));

        TextView titleView = text(heading, 16, p[2], true);
        titleView.setGravity(Gravity.RIGHT);
        titleView.setTextDirection(View.TEXT_DIRECTION_RTL);
        card.addView(titleView, new LinearLayout.LayoutParams(-1, -2));

        TextView detailView = text(details, 14, p[3], false);
        detailView.setGravity(Gravity.RIGHT);
        detailView.setTextDirection(View.TEXT_DIRECTION_RTL);
        detailView.setLineSpacing(dp(4), 1.0f);
        detailView.setTextIsSelectable(true);
        card.addView(detailView, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(4), 0, dp(4));
        parent.addView(card, params);
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
        DayCounterWidget.updateAll(this);
    }

    private void advanceTheme() {
        theme = (theme + 1) % palettes.length;
        prefs.edit().putInt("theme", theme).apply();
        buildUi();
        refreshCounter();
        if (quoteButton != null) quoteButton.requestFocus();
    }

    private void moveButtonFocus(boolean down) {
        if (down) themeButton.requestFocus();
        else quoteButton.requestFocus();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Phones with numeric keypads often expose 2/8/4/6/5 as the easiest controls.
        if (keyCode == KeyEvent.KEYCODE_2) { moveButtonFocus(false); return true; }
        if (keyCode == KeyEvent.KEYCODE_8) { moveButtonFocus(true); return true; }
        if (keyCode == KeyEvent.KEYCODE_4) { advanceQuote(); return true; }
        if (keyCode == KeyEvent.KEYCODE_6) { advanceTheme(); return true; }
        if (keyCode == KeyEvent.KEYCODE_5
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            View current = getCurrentFocus();
            if (current != null && current.isClickable()) { current.performClick(); return true; }
            quoteButton.performClick();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_1 || keyCode == KeyEvent.KEYCODE_STAR) {
            advanceQuote(); return true;
        }
        if (keyCode == KeyEvent.KEYCODE_3 || keyCode == KeyEvent.KEYCODE_POUND) {
            advanceTheme(); return true;
        }
        if (keyCode == KeyEvent.KEYCODE_7) { quoteButton.requestFocus(); return true; }
        if (keyCode == KeyEvent.KEYCODE_9) { themeButton.requestFocus(); return true; }
        if (keyCode == KeyEvent.KEYCODE_0) { refreshCounter(); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { moveButtonFocus(false); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { moveButtonFocus(true); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            advanceQuote(); return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
