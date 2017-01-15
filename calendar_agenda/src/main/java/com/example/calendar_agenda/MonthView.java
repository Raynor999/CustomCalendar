package com.example.calendar_agenda;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.calendar_agenda.util.CalendarUtils;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.github.xhinliang.lunarcalendar.LunarCalendar;

/**
 * Created by lijunguan on 2017/1/11. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

class MonthView extends View {
    private static final int DAYS_IN_WEEK = 7;
    private static final int MAX_WEEKS_IN_MONTH = 6;

    private static final int DEFAULT_SELECTED_DAY = -1;
    private static final int DEFAULT_WEEK_START = DayOfWeek.SUNDAY.getValue();


    private final TextPaint mSolarDayPaint = new TextPaint();
    private final TextPaint mLunarDayPaint = new TextPaint();

    private final Paint mDaySelectorPaint = new Paint();

    private boolean isShowLunar = true;

    private final GestureDetector mGestureDetector;

    // Desired dimensions.

    private final int mDesiredDayHeight;
    private final int mDesiredCellWidth;
    private final int mDesiredDaySelectorRadius;

    private String mMonthYearLabel;

    private int mMonth = 1;
    private int mYear;

    private int mDayHeight;
    private int mCellWidth;
    private int mDaySelectorRadius;

    private int mPaddedWidth;
    private int mPaddedHeight;

    /**
     * The day of month for the selected day, or -1 if no day is selected.
     */
    private int mActivatedDay = -1;

    /**
     * The day of month for today, or -1 if the today is not in the current month.
     */
    private int mToday = DEFAULT_SELECTED_DAY;

    /**
     * The first day of the week (ex. Calendar.SUNDAY) indexed from one.
     */
    private int mWeekStart = DEFAULT_WEEK_START;

    /**
     * The number of days (ex. 28) in the current month.
     */
    private int mDaysInMonth;

    /**
     * The day of week (ex. Calendar.SUNDAY) for the first day of the current month.
     */
    private int mDayOfWeekStart;

    /**
     * The day of month for the first (inclusive) enabled day.
     */
    private int mEnabledDayStart = 1;

    /**
     * The day of month for the last (inclusive) enabled day.
     */
    private int mEnabledDayEnd = 31;

    /**
     * Optional listener for handling day click actions.
     */
    private OnDayClickListener mOnDayClickListener;

    private List<Integer> mEventDay = Arrays.asList(5, 9);

    @ColorInt
    private int mDayTextColor;
    @ColorInt
    private int mDayDisableTextColor;
    @ColorInt
    private int mDayActivatedTextColor;


    private LunarCalendar[][] mLunarMonth;


    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.datePickerStyle);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources res = context.getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        mDesiredCellWidth = (int) (displayMetrics.density * 40 + 0.5f);
        mDesiredDayHeight = (int) (displayMetrics.density * 44 + 0.5f);
        mDesiredDaySelectorRadius = (int) (displayMetrics.density * 20 + 0.5f);

        mDayDisableTextColor = ContextCompat.getColor(context, android.R.color.darker_gray);
        mDayActivatedTextColor = Color.WHITE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        // updateMonthYearLabel();
        initPaints(res);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
git                final int x = (int) (event.getX() + 0.5f);
                final int y = (int) (event.getY() + 0.5f);
                final int clickedDay = getDayAtLocation(x, y);
                onDayClicked(clickedDay);
                return true;
            }
        });

    }


    private void updateMonthYearLabel() {
        //  mMonthYearLabel = YearMonth.of(mYear, mMonth).format(DateTimeFormatter.ISO_INSTANT);
    }


    public int getCellWidth() {
        return mCellWidth;
    }


    public void setDayTextAppearance(@ColorRes int resId) {
        mDayTextColor = ContextCompat.getColor(getContext(), resId);
        invalidate();
    }

    /**
     * Sets up the text and style properties for painting.
     */
    private void initPaints(Resources res) {

        //TODO 添加属性配置
        float dayTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, res.getDisplayMetrics());


        mDaySelectorPaint.setAntiAlias(true);
        mDaySelectorPaint.setStyle(Paint.Style.FILL);
        mDaySelectorPaint.setColor(ContextCompat.getColor(getContext(), R.color.md_deep_orange_A200));

        mSolarDayPaint.setAntiAlias(true);
        mSolarDayPaint.setTextSize(dayTextSize);
        mSolarDayPaint.setTextAlign(Paint.Align.CENTER);
        mSolarDayPaint.setStyle(Paint.Style.FILL);

        mLunarDayPaint.setAntiAlias(true);
        mLunarDayPaint.setTextSize(dayTextSize);
        mLunarDayPaint.setTextAlign(Paint.Align.CENTER);
        mLunarDayPaint.setStyle(Paint.Style.FILL);

    }


    void setDayTextColor(@ColorInt int color) {
        mDayTextColor = color;
        invalidate();

    }


    public void setOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        final int x = (int) (event.getX() + 0.5f);
//        final int y = (int) (event.getY() + 0.5f);
//
//        final int action = event.getAction();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_MOVE:
//                final int touchedItem = getDayAtLocation(x, y);
//                mIsTouchHighlighted = true;
//                if (mHighlightedDay != touchedItem) {
//                    mHighlightedDay = touchedItem;
//                    mPreviouslyHighlightedDay = touchedItem;
//                    invalidate();
//                }
//                if (action == MotionEvent.ACTION_DOWN && touchedItem < 0) {
//                    // Touch something that's not an item, reject event.
//                    return false;
//                }
//                break;
//
//            case MotionEvent.ACTION_UP:
//                final int clickedDay = getDayAtLocation(x, y);
//                onDayClicked(clickedDay);
//                // Fall through.
//            case MotionEvent.ACTION_CANCEL:
//                // Reset touched day on stream end.
//                mHighlightedDay = -1;
//                mIsTouchHighlighted = false;
//                invalidate();
//                break;
//        }
//        return true;
//    }

//    @SuppressWarnings("WrongConstant")
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        // We need to handle focus change within the SimpleMonthView because we are simulating
//        // multiple Views. The arrow keys will move between days until there is no space (no
//        // day to the left, top, right, or bottom). Focus forward and back jumps out of the
//        // SimpleMonthView, skipping over other SimpleMonthViews in the parent ViewPager
//        // to the next focusable View in the hierarchy.
//        boolean focusChanged = false;
//        switch (event.getKeyCode()) {
//            case KeyEvent.KEYCODE_DPAD_LEFT:
//                if (event.hasNoModifiers()) {
//                    focusChanged = moveOneDay(false);
//                }
//                break;
//            case KeyEvent.KEYCODE_DPAD_RIGHT:
//                if (event.hasNoModifiers()) {
//                    focusChanged = moveOneDay(true);
//                }
//                break;
//            case KeyEvent.KEYCODE_DPAD_UP:
//                if (event.hasNoModifiers()) {
//                    ensureFocusedDay();
//                    if (mHighlightedDay > 7) {
//                        mHighlightedDay -= 7;
//                        focusChanged = true;
//                    }
//                }
//                break;
//            case KeyEvent.KEYCODE_DPAD_DOWN:
//                if (event.hasNoModifiers()) {
//                    ensureFocusedDay();
//                    if (mHighlightedDay <= mDaysInMonth - 7) {
//                        mHighlightedDay += 7;
//                        focusChanged = true;
//                    }
//                }
//                break;
//            case KeyEvent.KEYCODE_DPAD_CENTER:
//            case KeyEvent.KEYCODE_ENTER:
//                if (mHighlightedDay != -1) {
//                    onDayClicked(mHighlightedDay);
//                    return true;
//                }
//                break;
//            case KeyEvent.KEYCODE_TAB: {
//                int focusChangeDirection = 0;
//                if (event.hasNoModifiers()) {
//                    focusChangeDirection = View.FOCUS_FORWARD;
//                } else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
//                    focusChangeDirection = View.FOCUS_BACKWARD;
//                }
//                if (focusChangeDirection != 0) {
//                    final ViewParent parent = getParent();
//                    // move out of the ViewPager next/previous
//                    View nextFocus = this;
//                    do {
//                        nextFocus = nextFocus.focusSearch(focusChangeDirection);
//                    } while (nextFocus != null && nextFocus != this &&
//                            nextFocus.getParent() == parent);
//                    if (nextFocus != null) {
//                        nextFocus.requestFocus();
//                        return true;
//                    }
//                }
//                break;
//            }
//        }
//        if (focusChanged) {
//            invalidate();
//            return true;
//        } else {
//            return super.onKeyDown(keyCode, event);
//        }
//    }

//    private boolean moveOneDay(boolean positive) {
//        ensureFocusedDay();
//        boolean focusChanged = false;
//        if (positive) {
//            if (!isLastDayOfWeek(mHighlightedDay) && mHighlightedDay < mDaysInMonth) {
//                mHighlightedDay++;
//                focusChanged = true;
//            }
//        } else {
//            if (!isFirstDayOfWeek(mHighlightedDay) && mHighlightedDay > 1) {
//                mHighlightedDay--;
//                focusChanged = true;
//            }
//        }
//        return focusChanged;
//    }
//
//    @Override
//    protected void onFocusChanged(boolean gainFocus, @ViewCompat.FocusDirection int direction,
//                                  @Nullable Rect previouslyFocusedRect) {
//        if (gainFocus) {
//            // If we've gained focus through arrow keys, we should find the day closest
//            // to the focus rect. If we've gained focus through forward/back, we should
//            // focus on the selected day if there is one.
//            final int offset = findDayOffset();
//            switch (direction) {
//                case View.FOCUS_RIGHT: {
//                    int row = findClosestRow(previouslyFocusedRect);
//                    mHighlightedDay = row == 0 ? 1 : (row * DAYS_IN_WEEK) - offset + 1;
//                    break;
//                }
//                case View.FOCUS_LEFT: {
//                    int row = findClosestRow(previouslyFocusedRect) + 1;
//                    mHighlightedDay = Math.min(mDaysInMonth, (row * DAYS_IN_WEEK) - offset);
//                    break;
//                }
//                case View.FOCUS_DOWN: {
//                    final int col = findClosestColumn(previouslyFocusedRect);
//                    final int day = col - offset + 1;
//                    mHighlightedDay = day < 1 ? day + DAYS_IN_WEEK : day;
//                    break;
//                }
//                case View.FOCUS_UP: {
//                    final int col = findClosestColumn(previouslyFocusedRect);
//                    final int maxWeeks = (offset + mDaysInMonth) / DAYS_IN_WEEK;
//                    final int day = col - offset + (DAYS_IN_WEEK * maxWeeks) + 1;
//                    mHighlightedDay = day > mDaysInMonth ? day - DAYS_IN_WEEK : day;
//                    break;
//                }
//            }
//            ensureFocusedDay();
//            invalidate();
//        }
//        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//    }

//    /**
//     * Returns the row (0 indexed) closest to previouslyFocusedRect or center if null.
//     */
//    private int findClosestRow(@Nullable Rect previouslyFocusedRect) {
//        if (previouslyFocusedRect == null) {
//            return 3;
//        } else {
//            int centerY = previouslyFocusedRect.centerY();
//
//            final TextPaint p = mSolarDayPaint;
//
//            final int rowHeight = mDayHeight;
//
//            // Text is vertically centered within the row height.
//            final float halfLineHeight = (p.ascent() + p.descent()) / 2f;
//            final int rowCenter = rowHeight / 2;
//
//            centerY -= rowCenter - halfLineHeight;
//            int row = Math.round(centerY / (float) rowHeight);
//            final int maxDay = findDayOffset() + mDaysInMonth;
//            final int maxRows = (maxDay / DAYS_IN_WEEK) - ((maxDay % DAYS_IN_WEEK == 0) ? 1 : 0);
//
//            row = constrain(row, 0, maxRows);
//            return row;
//        }
//    }

    /**
     * Returns the column (0 indexed) closest to the previouslyFocusedRect or center if null. The 0
     * index is related to the first day of the week.
     */
    private int findClosestColumn(@Nullable Rect previouslyFocusedRect) {
        if (previouslyFocusedRect == null) {
            return DAYS_IN_WEEK / 2;
        } else {
            int centerX = previouslyFocusedRect.centerX() - getPaddingLeft();
            final int columnFromLeft =
                    constrain(centerX / mCellWidth, 0, DAYS_IN_WEEK - 1);
            return columnFromLeft;
        }
    }

//    @Override
//    public void getFocusedRect(Rect r) {
//        if (mHighlightedDay > 0) {
//            getBoundsForDay(mHighlightedDay, r);
//        } else {
//            super.getFocusedRect(r);
//        }
//    }

//    @Override
//    protected void onFocusLost() {
//        if (!mIsTouchHighlighted) {
//            // Unhighlight a day.
//            mPreviouslyHighlightedDay = mHighlightedDay;
//            mHighlightedDay = -1;
//            invalidate();
//        }
//        super.onFocusLost();
//    }
//

//    /**
//     * Ensure some day is highlighted. If a day isn't highlighted, it chooses the selected day, if
//     * possible, or the first day of the month if not.
//     */
//    private void ensureFocusedDay() {
//        if (mHighlightedDay != -1) {
//            return;
//        }
//        if (mPreviouslyHighlightedDay != -1) {
//            mHighlightedDay = mPreviouslyHighlightedDay;
//            return;
//        }
//        if (mActivatedDay != -1) {
//            mHighlightedDay = mActivatedDay;
//            return;
//        }
//        mHighlightedDay = 1;
//    }

    private boolean isFirstDayOfWeek(int day) {
        final int offset = findDayOffset();
        return (offset + day - 1) % DAYS_IN_WEEK == 0;
    }

    private boolean isLastDayOfWeek(int day) {
        final int offset = findDayOffset();
        return (offset + day) % DAYS_IN_WEEK == 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        canvas.translate(paddingLeft, paddingTop);

        drawDays(canvas);
        drawEventBadge(canvas);
        canvas.translate(-paddingLeft, -paddingTop);
    }

    private void drawEventBadge(Canvas canvas) {
        if (mEventDay == null || mEventDay.isEmpty()) return;

        final int rowHeight = mDayHeight;
        final int colWidth = mCellWidth;
        final int dayOffset = findDayOffset();
        final Paint p = mDaySelectorPaint;
        p.setColor(Color.RED);
        float distance = (float) (Math.sin(45) * mDaySelectorRadius); //计算绘制坐标X,Y偏移量
        //圆点半径 16px
        final int radius = 10  ;

        for (Integer day : mEventDay) {
            //根据天定位到 坐标 [row][col]
            int row = (day + dayOffset) / DAYS_IN_WEEK;
            int col = (day + dayOffset - 1) % DAYS_IN_WEEK;

            final int colCenter = colWidth * col + colWidth / 2;
            final int rowCenter = rowHeight * row + rowHeight / 2;

            canvas.drawCircle(colCenter + distance, rowCenter - distance, radius, p);

        }

    }


    public String getMonthYearLabel() {
        return mMonthYearLabel;
    }


    /**
     * Draws the month days.
     */
    private void drawDays(Canvas canvas) {
        final TextPaint solarP = mSolarDayPaint;
        final TextPaint lunarP = mLunarDayPaint;
        lunarP.setTextSize(solarP.getTextSize()*0.75f);
        final int rowHeight = mDayHeight;
        final int colWidth = mCellWidth;

        int rowCenter = rowHeight / 2;
        float sOffset = 0, lOffset = 0;
        float solarBaseLineY;
        float lunarBaseLineY;

        if (isShowLunar) {  //如果需要绘制农历, 需要考虑两行文本居中的情况,计算偏移量
            Paint.FontMetrics dayFontMetrics = solarP.getFontMetrics();
            Paint.FontMetrics lunarFontMetrics = lunarP.getFontMetrics();
            //农历文本的高度(包括top) 加上日历文本的文本最低点和bottom的距离 ,
            float totalHeight = dayFontMetrics.bottom - dayFontMetrics.descent +
                    (-lunarFontMetrics.top + lunarFontMetrics.descent);

            //由于绘制农历文本,导致 日期文本baseLine 向上偏移的数值
            sOffset = -totalHeight / 2;

            lOffset = (solarP.descent() - solarP.ascent()) / 2;
        }
        // Text is vertically centered within the row height.


        for (int day = 1, col = findDayOffset(); day <= mDaysInMonth; day++) {
            final int colCenter = colWidth * col + colWidth / 2;

            final boolean isDayEnabled = isDayEnabled(day);
            final boolean isDayActivated = mActivatedDay == day;
            final boolean isDayToday = mToday == day;
            if (isDayActivated) {
                final Paint paint = mDaySelectorPaint;
                canvas.drawCircle(colCenter, rowCenter, mDaySelectorRadius, paint);
            }

            final int dayTextColor;


            if (isDayEnabled) {
                dayTextColor = mDayDisableTextColor;
            } else if (isDayToday && !isDayActivated) {
                dayTextColor = mDaySelectorPaint.getColor();
            } else if (!isDayActivated) {
                dayTextColor = mDayTextColor;
            } else {
                dayTextColor = mDayActivatedTextColor;
            }

            solarP.setColor(dayTextColor);
            solarBaseLineY = calculateBaseLineY(rowCenter, solarP) + sOffset;
            canvas.drawText(String.valueOf(day), colCenter, solarBaseLineY, solarP);
            if (isShowLunar && mLunarMonth != null) {
                lunarBaseLineY = calculateBaseLineY(rowCenter, lunarP) + lOffset;
                LunarCalendar lunar = mLunarMonth[rowCenter / rowHeight][col];
                if (lunar != null) {
                    String lunarText = lunar.getLunarDay();
                    lunarP.setColor(dayTextColor);
                    canvas.drawText(lunarText, colCenter, lunarBaseLineY, lunarP);
                }

            }

            col++;

            if (col == DAYS_IN_WEEK) {
                col = 0;
                rowCenter += rowHeight;
            }
        }
    }

    private float calculateBaseLineY(int centerY, TextPaint p) {
        /*
         *1. 中心位置坐标，centerX, centerY
          2. 文本高度：height ＝ descent－ascent
          3.descent的坐标：descentY ＝ centerY ＋ 1/2height
          4.baseLineY坐标：baseLineY ＝ descentY－descent
          所以推到出公式
          baseLineY ＝ centerY － 1/2(ascent + descent)
         */
        final float halfLineHeight = (p.ascent() + p.descent()) / 2f;
        return centerY - halfLineHeight;
    }

    private boolean isDayEnabled(int day) {
        return day >= mEnabledDayStart && day <= mEnabledDayEnd;
    }

    private boolean isValidDayOfMonth(int day) {
        return day >= 1 && day <= mDaysInMonth;
    }

    private static boolean isValidDayOfWeek(int day) {
        return day >= Calendar.SUNDAY && day <= Calendar.SATURDAY;
    }

    private static boolean isValidMonth(int month) {
        return month >= Calendar.JANUARY && month <= Calendar.DECEMBER;
    }

    /**
     * Sets the selected day.
     *
     * @param dayOfMonth the selected day of the month, or {@code -1} to clear the selection
     */
    public void setSelectedDay(int dayOfMonth) {
        mActivatedDay = dayOfMonth;

        invalidate();
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

    /**
     * Sets all the parameters for displaying this week. <p> Parameters have a default value and
     * will only update if a new value is included, except for focus month, which will always
     * default to no focus month if no value is passed in. The only required parameter is the week
     * start.
     *
     * @param selectedDay     the selected day of the month, or -1 for no selection
     * @param month           the month
     * @param year            the year
     * @param weekStart       which day the week should start on, valid values are {@link
     *                        Calendar#SUNDAY} through {@link Calendar#SATURDAY}
     * @param enabledDayStart the first enabled day
     * @param enabledDayEnd   the last enabled day
     */
    void setMonthParams(int selectedDay, int month, int year, int weekStart, int enabledDayStart,
                        int enabledDayEnd) {
        mActivatedDay = selectedDay;

        if (isValidMonth(month)) {
            mMonth = month;
        }
        mYear = year;

        LocalDate localDate = LocalDate.of(mYear, mMonth, 1);
        //获取本月第一天是星期几
        mDayOfWeekStart = localDate.getDayOfWeek().getValue();

        if (isValidDayOfWeek(weekStart)) {
            mWeekStart = weekStart;
        } else {
            mWeekStart = DayOfWeek.SUNDAY.getValue();
        }
        mDaysInMonth = localDate.lengthOfMonth();

        mToday = -1;
        final LocalDate nowDate = LocalDate.now();
        if (nowDate.getYear() == year && nowDate.getMonthValue() == month) {
            mToday = nowDate.getDayOfMonth();
        }

        mEnabledDayStart = enabledDayStart;
        mEnabledDayEnd = enabledDayEnd;

        updateMonthYearLabel();
        if (isShowLunar) {
            mLunarMonth = LunarCalendar.getInstanceMonth(mYear, mMonth);
        }
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int preferredHeight = mDesiredDayHeight * MAX_WEEKS_IN_MONTH
                + getPaddingTop() + getPaddingBottom();
        final int preferredWidth = mDesiredCellWidth * DAYS_IN_WEEK
                + ViewCompat.getPaddingStart(this) + ViewCompat.getPaddingEnd(this);

        final int resolvedWidth = resolveSize(preferredWidth, widthMeasureSpec);
        final int resolvedHeight = resolveSize(preferredHeight, heightMeasureSpec);
        setMeasuredDimension(resolvedWidth, resolvedHeight);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }
        // Let's initialize a completely reasonable number of variables.
        final int w = right - left;
        final int h = bottom - top;
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int paddedRight = w - paddingRight;
        final int paddedBottom = h - paddingBottom;
        final int paddedWidth = paddedRight - paddingLeft;
        final int paddedHeight = paddedBottom - paddingTop;
        if (paddedWidth == mPaddedWidth || paddedHeight == mPaddedHeight) {
            return;
        }
        //不含Padding的宽 高
        mPaddedWidth = paddedWidth;
        mPaddedHeight = paddedHeight;

        // We may have been laid out smaller than our preferred size. If so,
        // scale all dimensions to fit.
        final int measuredPaddedHeight = getMeasuredHeight() - paddingTop - paddingBottom;
        final float scaleH = paddedHeight / (float) measuredPaddedHeight;
        final int cellWidth = mPaddedWidth / DAYS_IN_WEEK;
        mDayHeight = (int) (mDesiredDayHeight * scaleH);
        mCellWidth = cellWidth;

        // Compute the largest day selector radius that's still within the clip
        // bounds and desired selector radius.
        final int maxSelectorWidth = cellWidth / 2 + Math.min(paddingLeft, paddingRight);
        final int maxSelectorHeight = mDayHeight / 2 + paddingBottom;
        mDaySelectorRadius = Math.min(mDesiredDaySelectorRadius,
                Math.min(maxSelectorWidth, maxSelectorHeight));

    }


    private int findDayOffset() {
        final int offset = mDayOfWeekStart - mWeekStart;
        if (mDayOfWeekStart < mWeekStart) {
            return offset + DAYS_IN_WEEK;
        }
        return offset;
    }

    /**
     * Calculates the day of the month at the specified touch position. Returns the day of the month
     * or -1 if the position wasn't in a valid day.
     *
     * @param x the x position of the touch event
     * @param y the y position of the touch event
     * @return the day of the month at (x, y), or -1 if the position wasn't in a valid day
     */
    private int getDayAtLocation(int x, int y) {
        final int paddedX = x - getPaddingLeft();
        if (paddedX < 0 || paddedX >= mPaddedWidth) {
            return -1;
        }

        final int paddedY = y - getPaddingTop();
        if (paddedY >= mPaddedHeight) {
            return -1;
        }


        final int row = paddedY / mDayHeight;
        final int col = (paddedX * DAYS_IN_WEEK) / mPaddedWidth;
        final int index = col + row * DAYS_IN_WEEK;
        final int day = index + 1 - findDayOffset();
        if (!isValidDayOfMonth(day)) {
            return -1;
        }

        return day;
    }
//
//    /**
//     * Calculates the bounds of the specified day.
//     *
//     * @param id        the day of the month
//     * @param outBounds the rect to populate with bounds
//     */
//    private boolean getBoundsForDay(int id, Rect outBounds) {
//        if (!isValidDayOfMonth(id)) {
//            return false;
//        }
//
//        final int index = id - 1 + findDayOffset();
//
//        // Compute left edge, taking into account RTL.
//        final int col = index % DAYS_IN_WEEK;
//        final int colWidth = mCellWidth;
//        final int left;
//
//        left = getPaddingLeft() + col * colWidth;
//
//
//        // Compute top edge.
//        final int row = index / DAYS_IN_WEEK;
//        final int rowHeight = mDayHeight;
//
//        final int top = getPaddingTop() + row * rowHeight;
//
//        outBounds.set(left, top, left + colWidth, top + rowHeight);
//
//        return true;
//    }

    /**
     * Called when the user clicks on a day. Handles callbacks to the {@link OnDayClickListener} if
     * one is set.
     *
     * @param day the day that was clicked
     */
    private boolean onDayClicked(int day) {
        if (!isValidDayOfMonth(day) || !isDayEnabled(day)) {
            return false;
        }

        if (mOnDayClickListener != null) {
            final LocalDate date = LocalDate.of(mYear, mMonth, day);
            mOnDayClickListener.onDayClick(this, date);
        }
        return true;
    }


    /**
     * Handles callbacks when the user clicks on a time object.
     */
    public interface OnDayClickListener {
        void onDayClick(MonthView view, LocalDate day);

    }

    private int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
}