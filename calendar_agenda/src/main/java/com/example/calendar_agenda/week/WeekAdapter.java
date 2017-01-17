package com.example.calendar_agenda.week;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.calendar_agenda.month.MonthAdapter;
import com.example.calendar_agenda.util.ProxyOnDaySelectedListener;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.WeekFields;

import java.util.Calendar;

/**
 * Created by lijunguan on 2017/1/17. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

public class WeekAdapter extends PagerAdapter {

    private static final int DAYS_IN_WEEK = 7;
    //日历起始日期
    private LocalDate mMinDate = LocalDate.MIN;
    //日历截止日期
    private LocalDate mMaxDate = LocalDate.MAX;

    private final SparseArray<WeekView> mWeekViews = new SparseArray<>();

    private int mCount;

    private final LayoutInflater mInflater;
    private final int mLayoutResId;
    private final int mWeekViewId;


    private LocalDate mSelectedDay = null;

    /**
     * 设置的起始日期的偏移量, 即最小日期所在的周,距离周的第一天的偏移量.
     */
    private int mOffset;


    private ProxyOnDaySelectedListener mOnDaySelectedListener;

    public WeekAdapter(@NonNull Context context, @LayoutRes int layoutResId,
                       @IdRes int calendarViewId) {
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mWeekViewId = calendarViewId;

    }

    /**
     * 日历设置的 周首日 (eg US 周日开头, 法国 周一开头)
     */
    private int mFirstDayOfWeek;

    public void setRange(@NonNull LocalDate min, @NonNull LocalDate max) {
        mMinDate = min;
        mMaxDate = max;

        int days = (int) min.until(max, ChronoUnit.DAYS);

        final int dayOfWeek = min.getDayOfWeek().getValue(); //最小日期是周几
        //计算偏移量
        mOffset = dayOfWeek - mFirstDayOfWeek;
        if (dayOfWeek < mFirstDayOfWeek) {
            mOffset += DAYS_IN_WEEK;
        }
        mCount = (days + mOffset) / DAYS_IN_WEEK;

        // Positions are now invalid, clear everything and start over.
        notifyDataSetChanged();

    }


    /**
     * Sets the first day of the week.
     *
     * @param weekStart which day the week should start on, valid values are {@link Calendar#SUNDAY}
     *                  through {@link Calendar#SATURDAY}
     */
    public void setWeekStart(int weekStart) {
        mFirstDayOfWeek = weekStart;
        // Update displayed views.
        final int count = mWeekViews.size();
        for (int i = 0; i < count; i++) {
            final WeekView weekView = mWeekViews.valueAt(i);
            weekView.setFirstDayOfWeek(weekStart);
        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View itemView = (WeekView) mInflater.inflate(mLayoutResId, container, false);
        final WeekView weekView = (WeekView) itemView.findViewById(mWeekViewId);
        weekView.setOnDayClickListener(mOnDayClickListener);

        return weekView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mWeekViews.remove(position);
    }

    /**
     * Sets the selected day.
     *
     * @param day the selected day
     */
    public void setSelectedDay(@Nullable LocalDate day) {
        final int oldPosition = getPositionForDay(mSelectedDay);
        final int newPosition = getPositionForDay(day);

        // Clear the old position if necessary.
        if (oldPosition != newPosition && oldPosition >= 0) {
            final WeekView oldWeekView = mWeekViews.get(oldPosition, null);
            if (oldWeekView != null) {
                oldWeekView.setSelectedDay(-1);
            }
        }

        // Set the new position.
        if (newPosition >= 0) {
            final WeekView newWeekView = mWeekViews.get(oldPosition, null);
            if (newWeekView != null) {
                newWeekView.setSelectedDay(day.getDayOfMonth());
            }
        }

        mSelectedDay = day;
    }

    private int getPositionForDay(@Nullable LocalDate day) {
        if (day == null) {
            return -1;
        }
        int offsetDays = (int) mMinDate.until(day, ChronoUnit.DAYS);
        //根据日期
        return (offsetDays + mOffset) / DAYS_IN_WEEK;
    }


    private final WeekView.OnDayClickListener mOnDayClickListener = new WeekView.OnDayClickListener() {
        @Override
        public void onDayClick(WeekView view, LocalDate day) {
            if (day != null) {
                setSelectedDay(day);

                if (mOnDaySelectedListener != null) {
                    mOnDaySelectedListener.onDaySelected(WeekAdapter.this, day);
                }
            }
        }
    };

}
