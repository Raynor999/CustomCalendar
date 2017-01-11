package com.example.calendar_agenda;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;
import org.threeten.bp.format.DateTimeParseException;

import java.util.Calendar;


/**
 * Created by lijunguan on 2017/1/11. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

class CalendarPickerView extends LinearLayout {
    public static final String TAG = CalendarPickerView.class.getSimpleName();

    private static final int DEFAULT_LAYOUT = R.layout.layout_moth_view_pager;


    private static final int[] ATTRS_TEXT_COLOR = new int[]{android.R.attr.textColor};

    private LocalDate mSelectedDay;
    private LocalDate mMinDate = LocalDate.of(1900, Month.JANUARY, 1);
    private LocalDate mMaxDate = LocalDate.of(2100, Month.DECEMBER, 31);


    private final ViewPager mViewPager;

    private final WeekBarView mWeekBarView;


    private final CalendarPickerPagerAdapter mAdapter;

    private OnDaySelectedListener mOnDaySelectedListener;

    public CalendarPickerView(Context context) {
        this(context, null);
    }

    public CalendarPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.calendarViewStyle);
    }

    public CalendarPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CalendarPickerView, defStyleAttr, defStyleAttr);

        final int firstDayOfWeek = a.getInt(R.styleable.CalendarPickerView_cpv_first_day_of_week,
                Calendar.SUNDAY);

        final String minDateStr = a.getString(R.styleable.CalendarPickerView_cpv_min_date);
        final String maxDateStr = a.getString(R.styleable.CalendarPickerView_cpv_max_date);

//        final int monthTextAppearanceResId = a.getResourceId(
//                R.styleable.CalendarView_monthTextAppearance,
//                R.style.TextAppearance_Material_Widget_Calendar_Month);
//        final int dayOfWeekTextAppearanceResId = a.getResourceId(
//                R.styleable.CalendarView_weekDayTextAppearance,
//                R.style.TextAppearance_Material_Widget_Calendar_DayOfWeek);
//        final int dayTextAppearanceResId = a.getResourceId(
//                R.styleable.CalendarView_dateTextAppearance,
//                R.style.TextAppearance_Material_Widget_Calendar_Day);
//
//        final ColorStateList daySelectorColor = a.getColorStateList(
//                R.styleable.CalendarView_daySelectorColor);

        a.recycle();

        // Set up adapter.
        mAdapter = new CalendarPickerPagerAdapter(context,
                R.layout.layout_month_view, R.id.month_view);

        mAdapter.setDayTextAppearance(dayTextAppearanceResId);
        mAdapter.setDaySelectorColor(daySelectorColor);

        final LayoutInflater inflater = LayoutInflater.from(context);
        mViewPager = (ViewPager) inflater.inflate(R.layout.layout_moth_view_pager, this, false);
        mWeekBarView = (WeekBarView) inflater.inflate(R.layout.layout_week_bar, this, false);
        addView(mWeekBarView);
        addView(mViewPager);
        mViewPager.setAdapter(mAdapter);

        // Set up min and max dates. 设置最小时间和最大时间
        try {
            if (!TextUtils.isEmpty(minDateStr)) {
                mMinDate = LocalDate.parse(minDateStr);
            }
            if (!TextUtils.isEmpty(maxDateStr)) {
                mMaxDate = LocalDate.parse(maxDateStr);
            }
        } catch (DateTimeParseException e) {
            Log.w(TAG, "Date String: " + minDateStr + maxDateStr + " not in format: ");
        }

        if (mMaxDate.isBefore(mMinDate)) {
            throw new IllegalArgumentException("maxDateStr must be >= minDateStr");
        }


        mSelectedDay = LocalDate.now();
        if (mSelectedDay.isBefore(mMinDate)) {
            mSelectedDay = mMinDate;
        }
        if (mSelectedDay.isAfter(mMaxDate)) {
            mSelectedDay = mMaxDate;
        }


        setFirstDayOfWeek(firstDayOfWeek);
        onRangeChanged();

        setDate(mSelectedDay, false);
        // Proxy selection callbacks to our own listener.
        mAdapter.setOnDaySelectedListener(new CalendarPickerPagerAdapter.OnDaySelectedListener() {
            @Override
            public void onDaySelected(CalendarPickerPagerAdapter adapter, LocalDate day) {
                if (mOnDaySelectedListener != null) {
                    mOnDaySelectedListener.onDaySelected(CalendarPickerView.this, day);
                }
            }
        });
    }


    public void setDayTextAppearance(int resId) {
        mAdapter.setDayTextAppearance(resId);
    }

    public int getDayTextAppearance() {
        return mAdapter.getDayTextAppearance();
    }

    /**
     * Sets the currently selected date to the specified LocalDate. Jumps immediately to the new
     * date. To animate to the new date, use {@link #setDate(LocalDate, boolean)}.
     *
     * @param localDate the target day
     */
    public void setDate(LocalDate localDate) {
        setDate(localDate, false);
    }

    /**
     * Sets the currently selected date to the specified LocalDate. Jumps immediately to the new
     * date, optionally animating the transition.
     *
     * @param localDate the target day in milliseconds
     * @param animate   whether to smooth scroll to the new position
     */
    public void setDate(LocalDate localDate, boolean animate) {
        setDate(localDate, animate, true);
    }

    /**
     * Moves to the month containing the specified day, optionally setting the day as selected.
     *
     * @param localDate   the target day in LocalDate
     * @param animate     whether to smooth scroll to the new position
     * @param setSelected whether to set the specified day as selected
     */
    private void setDate(LocalDate localDate, boolean animate, boolean setSelected) {
        if (setSelected) {
            mSelectedDay = localDate;
        }

        final int position = getPositionFromDay(localDate);
        if (position != mViewPager.getCurrentItem()) {
            mViewPager.setCurrentItem(position, animate);
        }

        mAdapter.setSelectedDay(localDate);
    }

    public LocalDate getDate() {
        return mSelectedDay;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mAdapter.setFirstDayOfWeek(firstDayOfWeek);
        mWeekBarView.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        return mAdapter.getFirstDayOfWeek();
    }

    public void setMinDate(@NonNull LocalDate minDate) {
        mMinDate = minDate;
        onRangeChanged();
    }

    public LocalDate getMinDate() {
        return mMinDate;
    }

    public void setMaxDate(LocalDate maxDate) {
        mMaxDate = maxDate;
        onRangeChanged();
    }

    public LocalDate getMaxDate() {
        return mMaxDate;
    }

    /**
     * Handles changes to date range.
     */
    public void onRangeChanged() {
        mAdapter.setRange(mMinDate, mMaxDate);

        // Changing the min/max date changes the selection position since we
        // don't really have stable IDs. Jumps immediately to the new position.
        setDate(mSelectedDay, false, false);

    }


    private int getPositionFromDay(LocalDate localDate) {
        final int totalMonth = mAdapter.getCount();
        //计算给定日期和最小日期之间有几个月，   diff moth between mMinDate and params
        final int diffMonth = Period.between(mMinDate, localDate).getMonths();
        return constrain(diffMonth, 0, totalMonth - 1);
    }

    private int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }


    public interface OnDaySelectedListener {
        void onDaySelected(CalendarPickerView view, LocalDate day);
    }
}