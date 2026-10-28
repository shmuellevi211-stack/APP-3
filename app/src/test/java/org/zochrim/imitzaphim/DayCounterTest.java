package org.zochrim.imitzaphim;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
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
