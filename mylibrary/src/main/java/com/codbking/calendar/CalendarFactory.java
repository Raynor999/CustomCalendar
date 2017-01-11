package com.codbking.calendar;

import android.support.v4.app.NotificationCompatBase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.net.SocketFactory;

import static com.codbking.calendar.CalendarUtil.getDayOfWeek;


/**
 * Created by lijunguan on 2016/12/16. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

class CalendarFactory {

    private static transient CalendarFactory sInstance;

    private static HashMap<String, List<CalendarBean>> mCache = new HashMap<>();


    public static CalendarFactory getInstance() {
        if (sInstance == null) {
            synchronized (CalendarFactory.class) {
                if (sInstance == null) {
                    sInstance = new CalendarFactory();
                }
            }
        }
        return sInstance;
    }

    //获取一月中的集合
    List<CalendarBean> getMonthOfDayList(int year, int month) {

        String key = year + "" + month;
        if (mCache.containsKey(key)) {
            List<CalendarBean> list = mCache.get(key);
            if (list == null) {
                mCache.remove(key);
            } else {
                return list;
            }
        }

        List<CalendarBean> list = new ArrayList<CalendarBean>();


        int total = CalendarUtil.getTotalDayOfMonth(year, month);
        //计算出一月第一天是星期几
        int indexWeek = getDayOfWeek(year, month, 1);


        //显示前一个月的天数,填满第一行.
        for (int i = indexWeek - 1; i > 0; i--) {
            CalendarBean bean = createCalendarBean(year, month, 1 - i);
            bean.mothFlag = CalendarBean.PREVIOUS;
            list.add(bean);
        }
        //获取当月的天数
        for (int i = 0; i < total; i++) {
            CalendarBean bean = createCalendarBean(year, month, i + 1);
            list.add(bean);
        }
        //显示下月的天数,填满剩余的所有格子
        for (int i = 0; i < 42 - (indexWeek - 1) - total; i++) {
            CalendarBean bean = createCalendarBean(year, month, total + i + 1);
            bean.mothFlag = CalendarBean.NEXT;
            list.add(bean);
        }
        mCache.put(key, list);

        return list;
    }


    private static CalendarBean createCalendarBean(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DATE);

        CalendarBean bean = new CalendarBean(year, month, day);
        bean.week = CalendarUtil.getDayOfWeek(year, month, day);
        String[] chinaDate = ChinaDate.getChinaDate(year, month, day);
        bean.chinaMonth = chinaDate[0];
        bean.chinaDay = chinaDate[1];

        return bean;
    }


}
