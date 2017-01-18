package com.example.calendar_agenda;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.calendar_agenda.month.MonthAdapter;
import com.example.calendar_agenda.util.ProxyOnDaySelectedListener;
import com.example.calendar_agenda.week.WeekAdapter;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.format.DateTimeParseException;


/**
 * Created by lijunguan on 2017/1/11. email: lijunguan199210@gmail.com blog: Created by lijunguan on
 * 2017/1/11. email: lijunguan199210@gmail.com blog: https://lijunguan.github.io
 */

public class CalendarPickerView extends LinearLayout {
    public static final String TAG = CalendarPickerView.class.getSimpleName();


    private LocalDate mSelectedDay;
    private LocalDate mMinDate = LocalDate.of(1900, Month.JANUARY, 1);
    private LocalDate mMaxDate = LocalDate.of(2100, Month.DECEMBER, 31);


    private final ViewPager mMonthViewPager;
    private final ViewPager mWeekViewPager;

    private final WeekBarView mWeekBarView;

    private MonthAdapter mMonthAdapter;

    private WeekAdapter mWeekAdapter;


    private OnDaySelectedListener mOnDaySelectedListener;

    // Proxy selection callbacks to our own listener.
    private final ProxyOnDaySelectedListener mProxyListener = new ProxyOnDaySelectedListener() {
        @Override
        public void onDaySelected(MonthAdapter adapter, LocalDate day) {
            if (mOnDaySelectedListener != null) {
                mOnDaySelectedListener.onDaySelected(CalendarPickerView.this, day);
            }
        }

        @Override
        public void onDaySelected(WeekAdapter adapter, LocalDate day) {
            if (mOnDaySelectedListener != null) {
                mOnDaySelectedListener.onDaySelected(CalendarPickerView.this, day);
            }
        }
    };

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
        final int weekStart = a.getInt(R.styleable.CalendarPickerView_cpv_week_start,
                DayOfWeek.SUNDAY.getValue());
        final String minDateStr = a.getString(R.styleable.CalendarPickerView_cpv_min_date);
        final String maxDateStr = a.getString(R.styleable.CalendarPickerView_cpv_max_date);
        a.recycle();

        mMonthAdapter = new MonthAdapter(context, R.layout.layout_month_view, R.id.month_view);
        mWeekAdapter = new WeekAdapter(context, R.layout.layout_week_view, R.id.week_view);
        // Set up adapter.
        final LayoutInflater inflater = LayoutInflater.from(context);
        mMonthViewPager = new ViewPager(context);
        mMonthViewPager.setLayoutParams(new ViewPager.LayoutParams());

        mWeekViewPager = new ViewPager(context);
        mWeekViewPager.setLayoutParams(new ViewPager.LayoutParams());
        mWeekBarView = (WeekBarView) inflater.inflate(R.layout.layout_week_bar, this, false);

        addView(mWeekBarView);
        addView(mWeekViewPager);
        addView(mMonthViewPager);

        mMonthViewPager.setVisibility(GONE);

        mMonthViewPager.setAdapter(mMonthAdapter);
        mWeekViewPager.setAdapter(mWeekAdapter);
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

        setWeekStart(weekStart);
        onRangeChanged();
        setDate(mSelectedDay, false);
        mMonthAdapter.setOnDaySelectedListener(mProxyListener);
        mWeekAdapter.setOnDaySelectedListener(mProxyListener);

        mWeekViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mOnCalendarChangedListener != null) {
                    mMonthAdapter.getItemPosition()
                            mOnCalendarChangedListener.onMonthChanged();
                }
            }
        });
    }

    private OnCalendarChangedListener mOnCalendarChangedListener;




    /**
     * Sets the currently selected date to the specified LocalDate. Jumps immediately to the new
     * date. To animate to the new date, use {@link #setDate(LocalDate, boolean)}.
     *
     * @param localDate the target day
     */
    public void setDate(@NonNull LocalDate localDate) {
        setDate(localDate, false);
    }

    /**
     * Sets the currently selected date to the specified LocalDate. Jumps immediately to the new
     * date, optionally animating the transition.
     *
     * @param localDate the target day in milliseconds
     * @param animate   whether to smooth scroll to the new position
     */
    public void setDate(@NonNull LocalDate localDate, boolean animate) {
        setDate(localDate, animate, true);
    }

    /**
     * Moves to the month containing the specified day, optionally setting the day as selected.
     *
     * @param date        the target day in LocalDate
     * @param animate     whether to smooth scroll to the new position
     * @param setSelected whether to set the specified day as selected
     */
    private void setDate(@NonNull LocalDate date, boolean animate, boolean setSelected) {
        if (setSelected) {
            mSelectedDay = date;
        }
        if (true) {
            final int position = mWeekAdapter.getPositionForDay(date);
            if (position != mWeekViewPager.getCurrentItem()) {
                mWeekViewPager.setCurrentItem(position, animate);
            }
            mWeekAdapter.setSelectedDay(date);

        } else {
            final int position = mMonthAdapter.getPositionFromDay(date);
            if (position != mMonthViewPager.getCurrentItem()) {
                mMonthViewPager.setCurrentItem(position, animate);
            }

            mMonthAdapter.setSelectedDay(date);
        }


    }

    public LocalDate getDate() {
        return mSelectedDay;
    }

    public void setWeekStart(int weekStart) {
        mMonthAdapter.setWeekStart(weekStart);
        mWeekAdapter.setWeekStart(weekStart);
        mWeekBarView.setWeekStart(weekStart);
    }

//    public int getFirstDayOfWeek() {
//        return mAdapter.getFirstDayOfWeek();
//    }

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
        mMonthAdapter.setRange(mMinDate, mMaxDate);
        mWeekAdapter.setRange(mMinDate, mMaxDate);
        // Changing the min/max date changes the selection position since we
        // don't really have stable IDs. Jumps immediately to the new position.
        setDate(mSelectedDay, false, false);

    }


    public interface OnDaySelectedListener {
        void onDaySelected(CalendarPickerView view, LocalDate day);
    }


    public enum CalendarState {
        WEEK,
        MONTH,
    }

    public void addOnPage(ViewPager.OnPageChangeListener listener) {
        mMonthViewPager.addOnPageChangeListener(listener);
        mWeekViewPager.addOnPageChangeListener(listener);
    }


    public CharSequence getYearMonthTitle() {

        return mWeekAdapter.getPageTitle(mWeekViewPager.getCurrentItem());
        // return mMonthAdapter.getPageTitle(mMonthViewPager.getCurrentItem());
    }

    public interface OnCalendarChangedListener {
        void onMonthChanged(LocalDate firstDayOfMonth);

        void onWeekChanged(LocalDate startDayOfWeek);
    }
}