package com.example.calendar_agenda.util;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.format.TextStyle;

import java.util.Locale;

/**
 * Created by lijunguan on 2017/1/11. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

public class CalendarUtils {


    public static String[] getsTinyWeekdayNames(Locale locale) {
        String[] sTinyWeekdayNames = new String[7];
        sTinyWeekdayNames[0] = DayOfWeek.SUNDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[1] = DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[2] = DayOfWeek.TUESDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[3] = DayOfWeek.WEDNESDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[4] = DayOfWeek.THURSDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[5] = DayOfWeek.FRIDAY.getDisplayName(TextStyle.SHORT, locale);
        sTinyWeekdayNames[6] = DayOfWeek.SATURDAY.getDisplayName(TextStyle.SHORT, locale);
        return sTinyWeekdayNames;
    }
}
