package com.codbking.calendar;

import android.icu.math.MathContext;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lijunguan on 2016/6/1. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */
public class CalendarUtil {

    //获取一月的第一天是星期几
    public static int getDayOfWeek(int y, int m, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(y, m - 1, day);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    //获取一月最大天数
    public static int getTotalDayOfMonth(int y, int m) {
        Calendar cal = Calendar.getInstance();
        cal.set(y, m - 1, 1);
        return cal.getActualMaximum(Calendar.DATE);
    }

    public static int getRowsNeed(int year, int month) {
        double weekIndex = getDayOfWeek(year, month, 1); //计算出一月第一天是星期几
        double totalDays = getTotalDayOfMonth(year, month);

        return (int) Math.ceil((weekIndex - 1.0d + totalDays) / 7.0d);
    }

    public static int getMothOfMonth(int y, int m) {
        Calendar cal = Calendar.getInstance();
        cal.set(y, m - 1, 1);
        int dateOfMonth = cal.get(Calendar.MONTH);
        return dateOfMonth + 1;
    }

    public static int[] getYMD(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};
    }

}
