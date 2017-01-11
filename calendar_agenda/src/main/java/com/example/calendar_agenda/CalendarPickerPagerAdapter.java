package com.example.calendar_agenda;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.Calendar;

/**
 * Created by lijunguan on 2017/1/11. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */


/**
 * An adapter for a list of {@link MonthView} items.
 */
class CalendarPickerPagerAdapter extends PagerAdapter {
    private static final int MONTHS_IN_YEAR = 12;

    private final LocalDate mMinDate = LocalDate.MIN;
    private final LocalDate mMaxDate = LocalDate.MAX;

    private final SparseArray<ViewHolder> mItems = new SparseArray<>();

    private final LayoutInflater mInflater;
    private final int mLayoutResId;
    private final int mCalendarViewId;

    private LocalDate mSelectedDay = null;

    private int mMonthTextAppearance;
    private int mDayOfWeekTextAppearance;
    private int mDayTextAppearance;

    private ColorStateList mCalendarTextColor;
    private ColorStateList mDaySelectorColor;
    private ColorStateList mDayHighlightColor;

    private OnDaySelectedListener mOnDaySelectedListener;

    private int mCount;
    private int mFirstDayOfWeek;

    public CalendarPickerPagerAdapter(@NonNull Context context, @LayoutRes int layoutResId,
                                      @IdRes int calendarViewId) {
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mCalendarViewId = calendarViewId;

        final TypedArray ta = context.obtainStyledAttributes(new int[]{
                com.android.internal.R.attr.colorControlHighlight});
        mDayHighlightColor = ta.getColorStateList(0);
        ta.recycle();
    }

    public void setRange(@NonNull LocalDate min, @NonNull LocalDate max) {

        Period period = Period.between(min, max);
        mCount = period.getMonths();
        // Positions are now invalid, clear everything and start over.
        notifyDataSetChanged();
    }

    /**
     * Sets the first day of the week.
     *
     * @param weekStart which day the week should start on, valid values are {@link Calendar#SUNDAY}
     *                  through {@link Calendar#SATURDAY}
     */
    public void setFirstDayOfWeek(int weekStart) {
        mFirstDayOfWeek = weekStart;
        // Update displayed views.
        final int count = mItems.size();
        for (int i = 0; i < count; i++) {
            final MonthView monthView = mItems.valueAt(i).calendar;
            monthView.setFirstDayOfWeek(weekStart);
        }
    }

    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
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
            final ViewHolder oldMonthView = mItems.get(oldPosition, null);
            if (oldMonthView != null) {
                oldMonthView.calendar.setSelectedDay(-1);
            }
        }

        // Set the new position.
        if (newPosition >= 0) {
            final ViewHolder newMonthView = mItems.get(newPosition, null);
            if (newMonthView != null) {
                newMonthView.calendar.setSelectedDay(day.getDayOfMonth());
            }
        }

        mSelectedDay = day;
    }

    /**
     * Sets the listener to call when the user selects a day.
     *
     * @param listener The listener to call.
     */
    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        mOnDaySelectedListener = listener;
    }

    void setCalendarTextColor(ColorStateList calendarTextColor) {
        mCalendarTextColor = calendarTextColor;
        notifyDataSetChanged();
    }

    void setDaySelectorColor(ColorStateList selectorColor) {
        mDaySelectorColor = selectorColor;
        notifyDataSetChanged();
    }

    void setMonthTextAppearance(int resId) {
        mMonthTextAppearance = resId;
        notifyDataSetChanged();
    }

    void setDayOfWeekTextAppearance(int resId) {
        mDayOfWeekTextAppearance = resId;
        notifyDataSetChanged();
    }

    int getDayOfWeekTextAppearance() {
        return mDayOfWeekTextAppearance;
    }

    void setDayTextAppearance(int resId) {
        mDayTextAppearance = resId;
        notifyDataSetChanged();
    }

    int getDayTextAppearance() {
        return mDayTextAppearance;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        final ViewHolder holder = (ViewHolder) object;
        return view == holder.container;
    }

    private int getMonthForPosition(int position) {
        return (position + mMinDate.get(Calendar.MONTH)) % MONTHS_IN_YEAR;
    }

    private int getYearForPosition(int position) {
        final int yearOffset = (position + mMinDate.get(Calendar.MONTH)) / MONTHS_IN_YEAR;
        return yearOffset + mMinDate.get(Calendar.YEAR);
    }

    private int getPositionForDay(@Nullable LocalDate day) {
        if (day == null) {
            return -1;
        }
        //计算两个日期之间的月数返回
        return Period.between(mMinDate, day).getMonths();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View itemView = mInflater.inflate(mLayoutResId, container, false);

        final MonthView v = (MonthView) itemView.findViewById(mCalendarViewId);
        v.setOnDayClickListener(mOnDayClickListener);
        v.setMonthTextAppearance(mMonthTextAppearance);
        v.setDayOfWeekTextAppearance(mDayOfWeekTextAppearance);
        v.setDayTextAppearance(mDayTextAppearance);

        if (mDaySelectorColor != null) {
            v.setDaySelectorColor(mDaySelectorColor);
        }

        if (mDayHighlightColor != null) {
            v.setDayHighlightColor(mDayHighlightColor);
        }

        if (mCalendarTextColor != null) {
            v.setMonthTextColor(mCalendarTextColor);
            v.setDayOfWeekTextColor(mCalendarTextColor);
            v.setDayTextColor(mCalendarTextColor);
        }

        final int month = getMonthForPosition(position);
        final int year = getYearForPosition(position);

        final int selectedDay;
        if (mSelectedDay != null && mSelectedDay.getMonthValue() == month) {
            selectedDay = mSelectedDay.getDayOfMonth();
        } else {
            selectedDay = -1;
        }

        final int enabledDayRangeStart;
        if (mMinDate.get(Calendar.MONTH) == month && mMinDate.get(Calendar.YEAR) == year) {
            enabledDayRangeStart = mMinDate.get(Calendar.DAY_OF_MONTH);
        } else {
            enabledDayRangeStart = 1;
        }

        final int enabledDayRangeEnd;
        if (mMaxDate.get(Calendar.MONTH) == month && mMaxDate.get(Calendar.YEAR) == year) {
            enabledDayRangeEnd = mMaxDate.get(Calendar.DAY_OF_MONTH);
        } else {
            enabledDayRangeEnd = 31;
        }

        v.setMonthParams(selectedDay, month, year, mFirstDayOfWeek,
                enabledDayRangeStart, enabledDayRangeEnd);

        final ViewHolder holder = new ViewHolder(position, itemView, v);
        mItems.put(position, holder);

        container.addView(itemView);

        return holder;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        final ViewHolder holder = (ViewHolder) object;
        container.removeView(holder.container);

        mItems.remove(position);
    }

    @Override
    public int getItemPosition(Object object) {
        final ViewHolder holder = (ViewHolder) object;
        return holder.position;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final MonthView v = mItems.get(position).calendar;
        if (v != null) {
            return v.getMonthYearLabel();
        }

        return null;
    }

    MonthView getView(Object object) {
        if (object == null) {
            return null;
        }
        final ViewHolder holder = (ViewHolder) object;
        return holder.calendar;
    }

    private final MonthView.OnDayClickListener mOnDayClickListener = new MonthView.OnDayClickListener() {
        @Override
        public void onDayClick(MonthView view, LocalDate day) {
            if (day != null) {
                setSelectedDay(day);

                if (mOnDaySelectedListener != null) {
                    mOnDaySelectedListener.onDaySelected(CalendarPickerPagerAdapter.this, day);
                }
            }
        }
    };

    private static class ViewHolder {
        public final int position;
        public final View container;
        public final MonthView calendar;

        public ViewHolder(int position, View container, MonthView calendar) {
            this.position = position;
            this.container = container;
            this.calendar = calendar;
        }
    }

    public interface OnDaySelectedListener {
        public void onDaySelected(CalendarPickerPagerAdapter view, LocalDate day);
    }
}
