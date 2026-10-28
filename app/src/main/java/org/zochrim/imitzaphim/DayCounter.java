package org.zochrim.imitzaphim;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/** Calculates the civil-day count from the app's fixed 5 August 70 reference date. */
public final class DayCounter {
    private static final long MILLIS_PER_DAY = 86400000L;

    private DayCounter() { }

    /** Counts calendar dates, independent of daylight-saving changes and the device time zone. */
    public static long daysElapsed() {
        Calendar localToday = Calendar.getInstance();
        return daysBetween(
            70, Calendar.AUGUST, 5,
            localToday.get(Calendar.YEAR),
            localToday.get(Calendar.MONTH),
            localToday.get(Calendar.DAY_OF_MONTH));
    }

    /** Package-private for deterministic unit tests; months use Calendar's zero-based constants. */
    static long daysBetween(int startYear, int startMonth, int startDay,
                            int endYear, int endMonth, int endDay) {
        GregorianCalendar start = utcProlepticGregorianDate(startYear, startMonth, startDay);
        GregorianCalendar end = utcProlepticGregorianDate(endYear, endMonth, endDay);
        return (end.getTimeInMillis() - start.getTimeInMillis()) / MILLIS_PER_DAY;
    }

    private static GregorianCalendar utcProlepticGregorianDate(int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar(
            TimeZone.getTimeZone("UTC"), Locale.US);
        // Use Gregorian rules for every year so the fixed date does not silently become Julian.
        calendar.setGregorianChange(new Date(Long.MIN_VALUE));
        calendar.setLenient(false);
        calendar.clear();
        calendar.set(Calendar.ERA, GregorianCalendar.AD);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.getTimeInMillis(); // Validate the supplied date.
        return calendar;
    }
}
