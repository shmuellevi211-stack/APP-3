package org.zochrim.imitzaphim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;

public class DayCounterTest {
    @Test
    public void referenceDateCountsAsZero() {
        assertEquals(0L, DayCounter.daysBetween(
            70, Calendar.AUGUST, 5, 70, Calendar.AUGUST, 5));
    }

    @Test
    public void nextCivilDateCountsAsOne() {
        assertEquals(1L, DayCounter.daysBetween(
            2026, Calendar.OCTOBER, 28, 2026, Calendar.OCTOBER, 29));
    }

    @Test
    public void knownReferenceCountUsesConsistentGregorianRules() {
        assertEquals(714498L, DayCounter.daysBetween(
            70, Calendar.AUGUST, 5, 2026, Calendar.OCTOBER, 28));
    }

    @Test
    public void leapDayIsCounted() {
        assertEquals(2L, DayCounter.daysBetween(
            2024, Calendar.FEBRUARY, 28, 2024, Calendar.MARCH, 1));
    }

    @Test
    public void estimatedJerusalemNightfallIsInTheEarlyEvening() {
        long instant = DayCounter.nightfallMillisForDate(
            2026, Calendar.NOVEMBER, 2);
        Calendar jerusalem = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        jerusalem.setTimeInMillis(instant);
        assertEquals(2026, jerusalem.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, jerusalem.get(Calendar.MONTH));
        assertEquals(2, jerusalem.get(Calendar.DAY_OF_MONTH));
        assertTrue(jerusalem.get(Calendar.HOUR_OF_DAY) >= 16);
        assertTrue(jerusalem.get(Calendar.HOUR_OF_DAY) <= 18);
    }

    @Test
    public void invalidCivilDatesAreRejected() {
        boolean failed = false;
        try {
            DayCounter.daysBetween(
                2024, Calendar.FEBRUARY, 30, 2024, Calendar.MARCH, 1);
        } catch (IllegalArgumentException expected) {
            failed = true;
        }
        if (!failed) throw new AssertionError("Invalid date was accepted");
    }
}
