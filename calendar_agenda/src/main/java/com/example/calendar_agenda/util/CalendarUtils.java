package com.example.calendar_agenda.util;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.format.TextStyle;
import org.threeten.bp.temporal.WeekFields;

import java.util.Locale;

/**
 * Created by lijunguan on 2017/1/11. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

public class CalendarUtils {


    public static String[] getTinyWeekdayNames(Locale locale) {
        String[] sTinyWeekdayNames = new String[7];
        sTinyWeekdayNames[0] = DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[1] = DayOfWeek.TUESDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[2] = DayOfWeek.WEDNESDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[3] = DayOfWeek.THURSDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[4] = DayOfWeek.FRIDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[5] = DayOfWeek.SATURDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[6] = DayOfWeek.SUNDAY.getDisplayName(TextStyle.SHORT, locale);
        return sTinyWeekdayNames;
    }

    /**
     *the first day of the week, where 1 = MONDAY and 7 = SUNDAY
     * @param locale  specific geographical
     * @return range 1 ~ 7
     */
    public static int getFirstDayOfWeek(Locale locale) {
        return WeekFields.of(locale).getFirstDayOfWeek().getValue();
    }

    /**
     *the first day of the week, where 1 = MONDAY and 7 = SUNDAY
     * @return range 1 ~ 7
     */
    public static int getFirstDayOfWeek() {
        return WeekFields.of(Locale.getDefault()).getFirstDayOfWeek().getValue();
    }
}
