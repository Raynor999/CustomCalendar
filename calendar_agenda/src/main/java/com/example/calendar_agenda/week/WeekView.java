package com.example.calendar_agenda.week;

import android.content.Context;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.View;

import com.example.calendar_agenda.util.CalendarUtils;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.Calendar;

/**
 * Created by lijunguan on 2017/1/17. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

public class WeekView extends View {

    private static final int DEFAULT_WEEK_START = DayOfWeek.SUNDAY.getValue();

    private int mWeekStart = DEFAULT_WEEK_START;

    private OnDayClickListener mOnDayClickListener;

    private int mActivatedDay;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Sets the first day of the week.
     *
     * @param weekStart which day the week should start on, valid values are {@link Calendar#SUNDAY}
     *                  through {@link Calendar#SATURDAY}
     */
    public void setFirstDayOfWeek(@IntRange(from = 1, to = 7) int weekStart) {
        if (isValidDayOfWeek(weekStart)) {
            mWeekStart = weekStart;
        } else {
            mWeekStart = CalendarUtils.getFirstDayOfWeek();
        }

        invalidate();
    }

    public void setSelectedDay(int dayOfMonth) {
        mActivatedDay = dayOfMonth;
        invalidate();
    }

    private static boolean isValidDayOfWeek(int day) {
        return day >= 1 && day <= 7;
    }

    public void setOnDayClickListener(WeekView.OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }

    /**
     * Handles callbacks when the user clicks on a time object.
     */
    interface OnDayClickListener {
        void onDayClick(WeekView view, LocalDate day);

    }
}
