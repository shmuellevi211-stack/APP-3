package org.zochrim.imitzaphim;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/** The fixed historical starting date used by version 2.0. */
public final class DayCounter {
    private static final long MILLIS_PER_DAY = 86400000L;

    private DayCounter() { }

    /**
     * Counts civil days from 5 August AD 70. GregorianCalendar's historical
     * Julian/Gregorian cutover treats this pre-1582 date as Julian.
     */
    public static long daysElapsed() {
        Calendar localToday = Calendar.getInstance();
        int year = localToday.get(Calendar.YEAR);
        int month = localToday.get(Calendar.MONTH);
        int day = localToday.get(Calendar.DAY_OF_MONTH);

        GregorianCalendar todayUtc = new GregorianCalendar(
            TimeZone.getTimeZone("UTC"), Locale.US);
        todayUtc.clear();
        todayUtc.set(Calendar.ERA, GregorianCalendar.AD);
        todayUtc.set(year, month, day, 0, 0, 0);

        GregorianCalendar startUtc = new GregorianCalendar(
            TimeZone.getTimeZone("UTC"), Locale.US);
        startUtc.clear();
        startUtc.set(Calendar.ERA, GregorianCalendar.AD);
        startUtc.set(70, Calendar.AUGUST, 5, 0, 0, 0);

        // Historical civil-day correction: the hybrid calendar is two days higher than the agreed count.
        return (todayUtc.getTimeInMillis() - startUtc.getTimeInMillis()) / MILLIS_PER_DAY - 2L;
    }
}
