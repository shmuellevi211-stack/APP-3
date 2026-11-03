package org.zochrim.imitzaphim;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/** Compact Hebrew UI designed for small screens and physical-key navigation (API 19+). */
public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private TextView countView, dateView, quoteView, sourceView;
    private Button quoteButton, themeButton, widgetThemeButton, dayChangeButton;
    private Button cornerRadiusButton, guideButton;
    private ScrollView mainScroll;
    private int theme = 0, quoteIndex = 0;
    private int pendingScrollY = 0;
    private int pendingFocusIndex = 0;
    private final Handler counterHandler = new Handler(Looper.getMainLooper());
    private final Runnable counterRefresh = new Runnable() {
        @Override public void run() {
            if (countView != null && prefs != null) {
                long days = DayCounter.daysElapsed(prefs.getBoolean("nightfall_mode", false));
                countView.setText(String.format(Locale.getDefault(), "%,d", days));
                counterHandler.postDelayed(this, 60000L);
            }
        }
    };

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

    private int normalizedIndex(int value, int length) {
        return ((value % length) + length) % length;
    }

    private int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("zochrim", MODE_PRIVATE);
        theme = normalizedIndex(prefs.getInt("theme", 0), palettes.length);
        quoteIndex = normalizedIndex(prefs.getInt("quote", 0), quotes.length);
        buildUi();
        refreshCounter();
    }

    @Override protected void onResume() {
        super.onResume();
        if (countView != null) {
            counterHandler.removeCallbacks(counterRefresh);
            counterRefresh.run();
        }
    }

    @Override protected void onPause() {
        counterHandler.removeCallbacks(counterRefresh);
        super.onPause();
    }

    private GradientDrawable shape(int color, int radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        int selectedRadius = prefs == null ? radius : prefs.getInt("corner_radius_dp", 10);
        d.setCornerRadius(dp(selectedRadius));
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

        quoteButton = button("משפט אחר מהמקורות", p[1], p[3]);
        quoteButton.setId(View.generateViewId());
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1, -2);
        qp.setMargins(0, dp(5), 0, dp(3));
        content.addView(quoteButton, qp);
        quoteButton.setOnClickListener(v -> advanceQuote());

        themeButton = button("החלפת עיצוב", p[1], p[3]);
        themeButton.setId(View.generateViewId());
        content.addView(themeButton, new LinearLayout.LayoutParams(-1, -2));
        themeButton.setOnClickListener(v -> advanceTheme());

        widgetThemeButton = button(widgetThemeLabel(), p[1], p[3]);
        widgetThemeButton.setId(View.generateViewId());
        content.addView(widgetThemeButton, new LinearLayout.LayoutParams(-1, -2));
        widgetThemeButton.setOnClickListener(v -> showWidgetThemeDialog());

        dayChangeButton = button(dayChangeLabel(), p[1], p[3]);
        dayChangeButton.setId(View.generateViewId());
        content.addView(dayChangeButton, new LinearLayout.LayoutParams(-1, -2));
        dayChangeButton.setOnClickListener(v -> showDayChangeDialog());

        cornerRadiusButton = button(cornerRadiusLabel(), p[1], p[3]);
        cornerRadiusButton.setId(View.generateViewId());
        content.addView(cornerRadiusButton, new LinearLayout.LayoutParams(-1, -2));
        cornerRadiusButton.setOnClickListener(v -> showCornerRadiusDialog());

        guideButton = button("מדריך הפעלה", p[1], p[3]);
        guideButton.setId(View.generateViewId());
        content.addView(guideButton, new LinearLayout.LayoutParams(-1, -2));
        guideButton.setOnClickListener(v -> showGuideAndAbout());

        View[] navigationButtons = {quoteButton, themeButton, widgetThemeButton,
            dayChangeButton, cornerRadiusButton, guideButton};
        for (int i = 0; i < navigationButtons.length; i++) {
            navigationButtons[i].setNextFocusDownId(navigationButtons[(i + 1) % navigationButtons.length].getId());
            navigationButtons[i].setNextFocusUpId(navigationButtons[(i + navigationButtons.length - 1) % navigationButtons.length].getId());
        }
        TextView foot = text("ניווט: מקשי החצים  •  הפעלה: אישור", 9, p[3], false);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.setMargins(0, dp(3), 0, 0);
        content.addView(foot, fp);

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        mainScroll = scroll;
        updateQuote();

        final int restoreY = pendingScrollY;
        final int restoreFocus = pendingFocusIndex;
        scroll.post(() -> {
            View focusTarget = navigationButtonAt(restoreFocus);
            if (focusTarget != null) focusTarget.requestFocus();
            int contentHeight = scroll.getChildAt(0) == null
                ? 0 : scroll.getChildAt(0).getHeight();
            int maxScroll = Math.max(0, contentHeight - scroll.getHeight());
            scroll.scrollTo(0, Math.min(restoreY, maxScroll));
        });
    }

    private void refreshCounter() {
        if (countView == null) return;
        long days = DayCounter.daysElapsed(prefs.getBoolean("nightfall_mode", false));
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
            "• המונה מציג כמה ימים עברו מתאריך הייחוס הקבוע של האפליקציה.\\n" +
            "• ״משפט אחר מהמקורות״ מחליף את הפסוק שמופיע במסך ובווידג׳ט.\\n" +
            "• ״החלפת עיצוב״ משנה את ערכת הצבעים של מסך האפליקציה.\\n" +
            "• ״צבע הווידג׳ט״ שולט בצבע הרקע והכיתוב של הווידג׳טים במסך הבית. ״לפי צבע האפליקציה״ מצמיד את הצבעים שלהם לעיצוב האפליקציה, כך שהם מתחלפים יחד. בחירה בכחול, ירוק, לבן או שחור מעניקה לווידג׳טים צבע עצמאי, שאינו משתנה כשמחליפים את עיצוב האפליקציה.\\n" +
            "• ״מועד עדכון המניין״ קובע מתי המונה עובר ליום הבא: בחצות הלילה לפי השעון המקומי של המכשיר, או בצאת הכוכבים בירושלים לפי אומדן אסטרונומי. בחירה במועד אחר מעדכנת מיד את המספר ואת הווידג׳טים. חישוב צאת הכוכבים הוא הערכה כללית, ולא תחליף ללוח הלכתי לפי מנהג מסוים. אפשר לרענן את המניין בכל עת גם באמצעות מקש 0.\\n" +
            "• ״עיגול פינות״ קובע את מידת העיגול של כרטיסי האפליקציה והווידג׳טים: ללא עיגול (0dp), עדין (8dp), רגיל (10dp — ברירת המחדל), בינוני (14dp) או מלא (22dp).\\n" +
            "• כדי להוסיף ווידג׳ט, לחצו לחיצה ארוכה על מסך הבית, פתחו את רשימת הווידג׳טים ובחרו את הגודל הרצוי — קטן, בינוני או גדול."); 

        addAboutSection(page, "חורבן בית המקדש השני",
            "על פי המסורת, חורבן הבית השני מצוין בתשעה באב (ט׳ באב). אחת הכרונולוגיות המקובלות משייכת את החורבן לשנת ג׳תת״ל (3830 לבריאת העולם), בסביבות שנת 70 לספירה. מאחר שמדובר בתקופה קדומה, יש הבדלים בין שיטות התיארוך וההמרה ללוח האזרחי.\\n" +
            "לצורך חישוב עקבי של המניין, האפליקציה משתמשת בתאריך ייחוס קבוע — 5 באוגוסט 70 לספירה. זהו תאריך הבסיס של החישוב באפליקציה.\\n" +
            "על פי המסורת, בית המקדש השני עמד על תילו 420 שנה. נכון לשנת ה׳תשפ״ז (5787, החלה בשנת 2026 ונמשכת לתוך 2027), חלפו בקירוב 1,956 שנים מאז חורבן הבית. זהו מספר מקורב, ולא תיארוך היסטורי מדויק."); 

        addAboutSection(page, "משמעות השם",
            "השם ״בצאתי מירושלים״ לקוח מן הקינה ״אש תוקד בקרבי״ הנאמרת בתשעה באב. הקינה משווה בין יציאת מצרים, שהייתה יציאה לחירות, לבין היציאה מירושלים שבאה בעקבות החורבן והגלות. כך היא מבטאת את עומק האבל והגעגוע לירושלים ולבית המקדש, ואת התקווה לשוב ולבנותם.");

        addAboutSection(page, "אודות",
            "בצאתי מירושלים — גרסה 1.2\n" +
            "פותחה על ידי שמואל_770 מפורום מתמחים טופ.");

        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        scroller.setFocusable(true);
        scroller.setFocusableInTouchMode(true);
        scroller.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scroller.setVerticalScrollBarEnabled(true);
        scroller.addView(page);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("מדריך הפעלה")
            .setView(scroller)
            .setPositiveButton("סגירה", null)
            .create();
        dialog.setOnShowListener(ignored -> {
            Button closeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            closeButton.setOnClickListener(v -> dialog.dismiss());
            closeButton.setFocusable(true);
            closeButton.setFocusableInTouchMode(true);
            scroller.requestFocus();

            dialog.setOnKeyListener((shownDialog, keyCode, event) -> {
                if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

                // The select/confirm key always activates the visible dialog's close action,
                // rather than accidentally activating a button behind the dialog.
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                        || keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                    closeButton.performClick();
                    return true;
                }

                int amount = Math.max(dp(48), scroller.getHeight() / 2);
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
                    if (scroller.canScrollVertically(1)) {
                        scroller.smoothScrollBy(0, amount);
                    } else {
                        closeButton.requestFocus();
                    }
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP
                        || keyCode == KeyEvent.KEYCODE_PAGE_UP) {
                    if (closeButton.hasFocus()) {
                        scroller.requestFocus();
                    } else if (scroller.canScrollVertically(-1)) {
                        scroller.smoothScrollBy(0, -amount);
                    }
                    return true;
                }
                return false;
            });
        });
        dialog.show();
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
        detailView.setTextIsSelectable(false);
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
        pendingScrollY = mainScroll == null ? 0 : mainScroll.getScrollY();
        pendingFocusIndex = focusedNavigationButtonIndex();
        theme = (theme + 1) % palettes.length;
        prefs.edit().putInt("theme", theme).apply();
        buildUi();
        refreshCounter();
    }

    private String widgetThemeLabel() {
        int selected = prefs.getInt("widget_theme", -1);
        String[] names = {"כחול", "ירוק", "לבן", "שחור"};
        return selected < 0 ? "צבע הווידג׳ט: לפי האפליקציה"
            : "צבע הווידג׳ט: " + names[normalizedIndex(selected, names.length)];
    }

    private String dayChangeLabel() {
        return prefs.getBoolean("nightfall_mode", false)
            ? "מועד עדכון המניין: בצאת הכוכבים בירושלים"
            : "מועד עדכון המניין: בחצות הלילה";
    }

    private void showWidgetThemeDialog() {
        final String[] options = {
            "לפי צבע האפליקציה", "כחול", "ירוק", "לבן", "שחור"
        };
        int selected = prefs.getInt("widget_theme", -1) + 1;
        new AlertDialog.Builder(this)
            .setTitle("בחירת צבע לווידג׳ט")
            .setSingleChoiceItems(options, selected, (dialog, which) -> {
                prefs.edit().putInt("widget_theme", which - 1).apply();
                widgetThemeButton.setText(widgetThemeLabel());
                DayCounterWidget.updateAll(this);
                dialog.dismiss();
            })
            .setNegativeButton("ביטול", null)
            .show();
    }

    private void showDayChangeDialog() {
        final String[] options = {
            "בחצות הלילה (ברירת מחדל)",
            "בצאת הכוכבים בירושלים (חישוב משוער)"
        };
        int selected = prefs.getBoolean("nightfall_mode", false) ? 1 : 0;
        new AlertDialog.Builder(this)
            .setTitle("בחירת מועד עדכון המניין")
            .setMessage("בחירה במועד אחר מעדכנת מיד את המונה ואת הווידג׳טים. אפשר לרענן גם באופן יזום באמצעות מקש 0.")
            .setSingleChoiceItems(options, selected, (dialog, which) -> {
                prefs.edit().putBoolean("nightfall_mode", which == 1).apply();
                dayChangeButton.setText(dayChangeLabel());
                refreshCounter();
                DayCounterWidget.scheduleNextUpdate(this);
                dialog.dismiss();
            })
            .setNegativeButton("ביטול", null)
            .show();
    }

    private String cornerRadiusLabel() {
        int radius = prefs.getInt("corner_radius_dp", 10);
        String name;
        switch (radius) {
            case 0: name = "ללא"; break;
            case 8: name = "עדין"; break;
            case 14: name = "בינוני"; break;
            case 22: name = "מלא"; break;
            default: name = "רגיל"; radius = 10; break;
        }
        return "עיגול פינות: " + name + " (" + radius + "dp)";
    }

    private void showCornerRadiusDialog() {
        final int[] values = {0, 8, 10, 14, 22};
        final String[] options = {
            "ללא (0dp)",
            "עדין (8dp)",
            "רגיל — ברירת מחדל (10dp)",
            "בינוני (14dp)",
            "מלא (22dp)"
        };
        int current = prefs.getInt("corner_radius_dp", 10);
        int selected = 2;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) selected = i;
        }

        final int previousScroll = mainScroll == null ? 0 : mainScroll.getScrollY();
        new AlertDialog.Builder(this)
            .setTitle("בחירת מידת עיגול הפינות")
            .setSingleChoiceItems(options, selected, (dialog, which) -> {
                prefs.edit().putInt("corner_radius_dp", values[which]).apply();
                pendingScrollY = previousScroll;
                pendingFocusIndex = 4;
                buildUi();
                DayCounterWidget.updateAll(this);
                dialog.dismiss();
            })
            .setNegativeButton("ביטול", null)
            .show();
    }

    private View navigationButtonAt(int index) {
        switch (index) {
            case 0: return quoteButton;
            case 1: return themeButton;
            case 2: return widgetThemeButton;
            case 3: return dayChangeButton;
            case 4: return cornerRadiusButton;
            case 5: return guideButton;
            default: return quoteButton;
        }
    }

    private int focusedNavigationButtonIndex() {
        View[] buttons = {quoteButton, themeButton, widgetThemeButton,
            dayChangeButton, cornerRadiusButton, guideButton};
        View focused = getCurrentFocus();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == focused) return i;
        }
        return 0;
    }

    private void moveButtonFocus(boolean down) {
        View[] buttons = {quoteButton, themeButton, widgetThemeButton,
            dayChangeButton, cornerRadiusButton, guideButton};
        int currentIndex = 0;
        View current = getCurrentFocus();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == current) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = (currentIndex + (down ? 1 : buttons.length - 1)) % buttons.length;
        buttons[nextIndex].requestFocus();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        // The only numeric shortcut is 0 for an explicit counter refresh.
        if (keyCode == KeyEvent.KEYCODE_0) {
            refreshCounter();
            DayCounterWidget.scheduleNextUpdate(this);
            return true;
        }
        // All navigation otherwise uses the directional pad and its confirm key.
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            moveButtonFocus(false);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            moveButtonFocus(true);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            moveButtonFocus(keyCode == KeyEvent.KEYCODE_DPAD_LEFT);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                || keyCode == KeyEvent.KEYCODE_BUTTON_A) {
            View current = getCurrentFocus();
            if (current != null && current.isClickable()) {
                current.performClick();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
