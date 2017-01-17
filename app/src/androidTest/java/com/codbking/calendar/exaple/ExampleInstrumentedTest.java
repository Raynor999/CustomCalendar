package com.codbking.calendar.exaple;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.TemporalField;
import org.threeten.bp.temporal.WeekFields;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private final Application application =
            (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

    @Before
    public void setUp() {
        AndroidThreeTen.init(application);
    }

    @Test
    public void testLocalDate() {
        int us = WeekFields.of(Locale.US).getFirstDayOfWeek().getValue();
        int france = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek().getValue();
        assertEquals(us,7);
        assertEquals(france,1);
    }

    @Test
    public void testWeekFiled() {
        LocalDate min = LocalDate.of(2017,1,2);
        LocalDate max = LocalDate.of(2017,2,3);
        long weeks = min.until(max, ChronoUnit.WEEKS);
        assertEquals(5,weeks);

    }
}
