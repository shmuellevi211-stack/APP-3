package org.zochrim.imitzaphim;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/** Counts elapsed civil days from the fixed 5 August 70 reference date. */
public final class DayCounter {
    private static final long MILLIS_PER_DAY = 86400000L;
    private static final TimeZone JERUSALEM = TimeZone.getTimeZone("Asia/Jerusalem");
    // Approximate tzeit: the sun is 8.5 degrees below the horizon at Jerusalem.
    private static final double JERUSALEM_LATITUDE = 31.778;
    private static final double JERUSALEM_LONGITUDE = 35.235;
    private static final double NIGHTFALL_ZENITH = 98.5;

    private DayCounter() { }

    /** Default behavior: the displayed count changes at local midnight. */
    public static long daysElapsed() {
        return daysElapsed(false);
    }

    /**
     * When useNightfall is true, the displayed date advances at the estimated
     * Jerusalem nightfall time. Before that time, the previous civil date is used.
     */
    public static long daysElapsed(boolean useNightfall) {
        Calendar countedDate = useNightfall
            ? Calendar.getInstance(JERUSALEM)
            : Calendar.getInstance();

        if (useNightfall && System.currentTimeMillis() < nightfallMillis(countedDate)) {
            countedDate.add(Calendar.DAY_OF_MONTH, -1);
        }

        return daysBetween(
            70, Calendar.AUGUST, 5,
            countedDate.get(Calendar.YEAR),
            countedDate.get(Calendar.MONTH),
            countedDate.get(Calendar.DAY_OF_MONTH));
    }

    /** Returns the next selected counter-change instant as an absolute timestamp. */
    public static long nextBoundaryMillis(boolean useNightfall) {
        if (useNightfall) {
            Calendar jerusalemDate = Calendar.getInstance(JERUSALEM);
            long nextNightfall = nightfallMillis(jerusalemDate);
            if (nextNightfall <= System.currentTimeMillis()) {
                jerusalemDate.add(Calendar.DAY_OF_MONTH, 1);
                nextNightfall = nightfallMillis(jerusalemDate);
            }
            return nextNightfall;
        }

        Calendar nextMidnight = Calendar.getInstance();
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);
        nextMidnight.add(Calendar.DAY_OF_MONTH, 1);
        return nextMidnight.getTimeInMillis();
    }

    /** Package-private deterministic hook for unit tests; months are zero-based. */
    static long nightfallMillisForDate(int year, int month, int day) {
        GregorianCalendar date = new GregorianCalendar(JERUSALEM, Locale.US);
        date.setLenient(false);
        date.clear();
        date.set(year, month, day, 12, 0, 0);
        date.getTimeInMillis();
        return nightfallMillis(date);
    }

    /**
     * NOAA sunset-hour-angle calculation for a solar altitude of -8.5 degrees.
     * This is an approximation for Jerusalem, not a location-aware halachic-time service.
     */
    private static long nightfallMillis(Calendar jerusalemDate) {
        int dayOfYear = jerusalemDate.get(Calendar.DAY_OF_YEAR);
        double longitudeHour = JERUSALEM_LONGITUDE / 15.0;
        double approximateTime = dayOfYear + ((18.0 - longitudeHour) / 24.0);

        double meanAnomaly = (0.9856 * approximateTime) - 3.289;
        double trueLongitude = normalizeDegrees(
            meanAnomaly
                + (1.916 * sinDegrees(meanAnomaly))
                + (0.020 * sinDegrees(2.0 * meanAnomaly))
                + 282.634);

        double rightAscension = normalizeDegrees(
            Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(trueLongitude)))));
        double longitudeQuadrant = Math.floor(trueLongitude / 90.0) * 90.0;
        double rightAscensionQuadrant = Math.floor(rightAscension / 90.0) * 90.0;
        rightAscension += longitudeQuadrant - rightAscensionQuadrant;
        rightAscension /= 15.0;

        double sinDeclination = 0.39782 * sinDegrees(trueLongitude);
        double cosDeclination = Math.cos(Math.asin(sinDeclination));
        double cosHourAngle =
            (Math.cos(Math.toRadians(NIGHTFALL_ZENITH))
                - (sinDeclination * sinDegrees(JERUSALEM_LATITUDE)))
                / (cosDeclination * Math.cos(Math.toRadians(JERUSALEM_LATITUDE)));

        // The fallback is only for an unexpected date/location calculation failure.
        if (cosHourAngle < -1.0 || cosHourAngle > 1.0) {
            Calendar fallback = (Calendar) jerusalemDate.clone();
            fallback.set(Calendar.HOUR_OF_DAY, 18);
            fallback.set(Calendar.MINUTE, 0);
            fallback.set(Calendar.SECOND, 0);
            fallback.set(Calendar.MILLISECOND, 0);
            return fallback.getTimeInMillis();
        }

        double hourAngle = Math.toDegrees(Math.acos(cosHourAngle)) / 15.0;
        double localMeanTime = hourAngle + rightAscension
            - (0.06571 * approximateTime) - 6.622;
        double utcHour = localMeanTime - longitudeHour;
        while (utcHour < 0.0) utcHour += 24.0;
        while (utcHour >= 24.0) utcHour -= 24.0;

        GregorianCalendar utcMidnight =
            new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        utcMidnight.clear();
        utcMidnight.set(
            jerusalemDate.get(Calendar.YEAR),
            jerusalemDate.get(Calendar.MONTH),
            jerusalemDate.get(Calendar.DAY_OF_MONTH),
            0, 0, 0);
        return utcMidnight.getTimeInMillis() + Math.round(utcHour * 3600000.0);
    }

    private static double sinDegrees(double value) {
        return Math.sin(Math.toRadians(value));
    }

    private static double normalizeDegrees(double value) {
        double result = value % 360.0;
        return result < 0.0 ? result + 360.0 : result;
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
        calendar.getTimeInMillis();
        return calendar;
    }
}
