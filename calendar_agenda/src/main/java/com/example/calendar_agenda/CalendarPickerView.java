package com.example.calendar_agenda;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageButton;

import org.threeten.bp.LocalDate;

import java.util.Calendar;
import java.util.Locale;


/**
 * Created by lijunguan on 2017/1/11. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

class CalendarPickerView extends ViewGroup {
    private static final int DEFAULT_LAYOUT = R.layout.calendar_picker_content;

    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;

    private static final int[] ATTRS_TEXT_COLOR = new int[]{android.R.attr.textColor};

    private final Calendar mSelectedDay = Calendar.getInstance();
    private final Calendar mMinDate = Calendar.getInstance();
    private final Calendar mMaxDate = Calendar.getInstance();

    private final AccessibilityManager mAccessibilityManager;

    private final ViewPager mViewPager;


    private final CalendarPickerPagerAdapter mAdapter;

    /**
     * Temporary calendar used for date calculations.
     */
    private Calendar mTempCalendar;

    private OnDaySelectedListener mOnDaySelectedListener;

    public CalendarPickerView(Context context) {
        this(context, null);
    }

    public CalendarPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.calendarViewStyle);
    }

    public CalendarPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mAccessibilityManager = (AccessibilityManager) context.getSystemService(
                Context.ACCESSIBILITY_SERVICE);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CalendarPickerView, defStyleAttr, defStyleAttr);

        final int firstDayOfWeek = a.getInt(R.styleable.CalendarPickerView_cpv_first_day_of_week,
                Calendar.SUNDAY);

        final String minDate = a.getString(R.styleable.CalendarPickerView_cpv_min_date);
        final String maxDate = a.getString(R.styleable.CalendarPickerView_cpv_max_date);

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
                R.layout.item_date_picker_month, R.id.month_view);
        mAdapter.setMonthTextAppearance(monthTextAppearanceResId);
        mAdapter.setDayOfWeekTextAppearance(dayOfWeekTextAppearanceResId);
        mAdapter.setDayTextAppearance(dayTextAppearanceResId);
        mAdapter.setDaySelectorColor(daySelectorColor);

        final LayoutInflater inflater = LayoutInflater.from(context);
        final ViewGroup content = (ViewGroup) inflater.inflate(DEFAULT_LAYOUT, this, false);

        // Transfer all children from content to here.
        while (content.getChildCount() > 0) {
            final View child = content.getChildAt(0);
            content.removeViewAt(0);
            addView(child);
        }

        mViewPager = (ViewPager) findViewById(R.id.day_picker_view_pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(mOnPageChangedListener);

        // Set up min and max dates.

        LocalDate.parse("2016-12-04", )
        final Calendar tempDate = Calendar.getInstance();
        if (!CalendarView.parseDate(minDate, tempDate)) {
            tempDate.set(DEFAULT_START_YEAR, Calendar.JANUARY, 1);
        }
        final long minDateMillis = tempDate.getTimeInMillis();

        if (!CalendarView.parseDate(maxDate, tempDate)) {
            tempDate.set(DEFAULT_END_YEAR, Calendar.DECEMBER, 31);
        }
        final long maxDateMillis = tempDate.getTimeInMillis();


        if (maxDateMillis < minDateMillis) {
            throw new IllegalArgumentException("maxDate must be >= minDate");
        }

        final long setDateMillis = MathUtils.constrain(
                System.currentTimeMillis(), minDateMillis, maxDateMillis);

        setFirstDayOfWeek(firstDayOfWeek);
        setMinDate(minDateMillis);
        setMaxDate(maxDateMillis);
        setDate(setDateMillis, false);

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


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final ViewPager viewPager = mViewPager;
        measureChild(viewPager, widthMeasureSpec, heightMeasureSpec);

        final int measuredWidthAndState = viewPager.getMeasuredWidthAndState();
        final int measuredHeightAndState = viewPager.getMeasuredHeightAndState();
        setMeasuredDimension(measuredWidthAndState, measuredHeightAndState);

    }

    @Override
    public void onRtlPropertiesChanged(@ResolvedLayoutDir int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final ImageButton leftButton;
        final ImageButton rightButton;


        final int width = right - left;
        final int height = bottom - top;
        mViewPager.layout(0, 0, width, height);

        final MonthView monthView = (MonthView) mViewPager.getChildAt(0);
        final int monthHeight = monthView.getMonthHeight();
        final int cellWidth = monthView.getCellWidth();

        // Vertically center the previous/next buttons within the month
        // header, horizontally center within the day cell.
        final int leftDW = leftButton.getMeasuredWidth();
        final int leftDH = leftButton.getMeasuredHeight();
        final int leftIconTop = monthView.getPaddingTop() + (monthHeight - leftDH) / 2;
        final int leftIconLeft = monthView.getPaddingLeft() + (cellWidth - leftDW) / 2;
        leftButton.layout(leftIconLeft, leftIconTop, leftIconLeft + leftDW, leftIconTop + leftDH);

        final int rightDW = rightButton.getMeasuredWidth();
        final int rightDH = rightButton.getMeasuredHeight();
        final int rightIconTop = monthView.getPaddingTop() + (monthHeight - rightDH) / 2;
        final int rightIconRight = width - monthView.getPaddingRight() - (cellWidth - rightDW) / 2;
        rightButton.layout(rightIconRight - rightDW, rightIconTop,
                rightIconRight, rightIconTop + rightDH);
    }

    public void setDayOfWeekTextAppearance(int resId) {
        mAdapter.setDayOfWeekTextAppearance(resId);
    }

    public int getDayOfWeekTextAppearance() {
        return mAdapter.getDayOfWeekTextAppearance();
    }

    public void setDayTextAppearance(int resId) {
        mAdapter.setDayTextAppearance(resId);
    }

    public int getDayTextAppearance() {
        return mAdapter.getDayTextAppearance();
    }

    /**
     * Sets the currently selected date to the specified timestamp. Jumps immediately to the new
     * date. To animate to the new date, use {@link #setDate(long, boolean)}.
     *
     * @param timeInMillis the target day in milliseconds
     */
    public void setDate(long timeInMillis) {
        setDate(timeInMillis, false);
    }

    /**
     * Sets the currently selected date to the specified timestamp. Jumps immediately to the new
     * date, optionally animating the transition.
     *
     * @param timeInMillis the target day in milliseconds
     * @param animate      whether to smooth scroll to the new position
     */
    public void setDate(long timeInMillis, boolean animate) {
        setDate(timeInMillis, animate, true);
    }

    /**
     * Moves to the month containing the specified day, optionally setting the day as selected.
     *
     * @param timeInMillis the target day in milliseconds
     * @param animate      whether to smooth scroll to the new position
     * @param setSelected  whether to set the specified day as selected
     */
    private void setDate(long timeInMillis, boolean animate, boolean setSelected) {
        if (setSelected) {
            mSelectedDay.setTimeInMillis(timeInMillis);
        }

        final int position = getPositionFromDay(timeInMillis);
        if (position != mViewPager.getCurrentItem()) {
            mViewPager.setCurrentItem(position, animate);
        }

        mTempCalendar.setTimeInMillis(timeInMillis);
        mAdapter.setSelectedDay(mTempCalendar);
    }

    public long getDate() {
        return mSelectedDay.getTimeInMillis();
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mAdapter.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        return mAdapter.getFirstDayOfWeek();
    }

    public void setMinDate(long timeInMillis) {
        mMinDate.setTimeInMillis(timeInMillis);
        onRangeChanged();
    }

    public long getMinDate() {
        return mMinDate.getTimeInMillis();
    }

    public void setMaxDate(long timeInMillis) {
        mMaxDate.setTimeInMillis(timeInMillis);
        onRangeChanged();
    }

    public long getMaxDate() {
        return mMaxDate.getTimeInMillis();
    }

    /**
     * Handles changes to date range.
     */
    public void onRangeChanged() {
        mAdapter.setRange(mMinDate, mMaxDate);

        // Changing the min/max date changes the selection position since we
        // don't really have stable IDs. Jumps immediately to the new position.
        setDate(mSelectedDay.getTimeInMillis(), false, false);

    }

    /**
     * Sets the listener to call when the user selects a day.
     *
     * @param listener The listener to call.
     */
    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        mOnDaySelectedListener = listener;
    }

    private int getDiffMonths(Calendar start, Calendar end) {
        final int diffYears = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        return end.get(Calendar.MONTH) - start.get(Calendar.MONTH) + 12 * diffYears;
    }

    private int getPositionFromDay(long timeInMillis) {
        final int diffMonthMax = getDiffMonths(mMinDate, mMaxDate);
        final int diffMonth = getDiffMonths(mMinDate, getTempCalendarForTime(timeInMillis));
        return MathUtils.constrain(diffMonth, 0, diffMonthMax);
    }

    private Calendar getTempCalendarForTime(long timeInMillis) {
        if (mTempCalendar == null) {
            mTempCalendar = Calendar.getInstance();
        }
        mTempCalendar.setTimeInMillis(timeInMillis);
        return mTempCalendar;
    }

    /**
     * Gets the position of the view that is most prominently displayed within the list view.
     */
    public int getMostVisiblePosition() {
        return mViewPager.getCurrentItem();
    }

    public void setPosition(int position) {
        mViewPager.setCurrentItem(position, false);
    }

    private final ViewPager.OnPageChangeListener mOnPageChangedListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageSelected(int position) {
            updateButtonVisibility(position);
        }
    };

    public interface OnDaySelectedListener {
        void onDaySelected(CalendarPickerView view, LocalDate day);
    }
}