package com.example.calendar_agenda.week;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.calendar_agenda.R;
import com.example.calendar_agenda.util.CalendarUtils;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.WeekFields;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.github.xhinliang.lunarcalendar.LunarCalendar;

/**
 * Created by lijunguan on 201DAYS_IN_WEEK/1/1DAYS_IN_WEEK. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

public class WeekView extends View {

    private static final int DEFAULT_WEEK_START = DayOfWeek.SUNDAY.getValue();

    private int mWeekStart = DEFAULT_WEEK_START;

    private OnDayClickListener mOnDayClickListener;

    private static final int DAYS_IN_WEEK = 7;


    private final TextPaint mSolarDayPaint = new TextPaint();
    private final TextPaint mLunarDayPaint = new TextPaint();

    private final Paint mDaySelectorPaint = new Paint();

    private boolean isShowLunar = true;

    private final GestureDetector mGestureDetector;
    // Desired dimensions.

    private final int mDesiredDayHeight;
    private final int mDesiredCellWidth;
    private final int mDesiredDaySelectorRadius;

    private int mDayHeight;
    private int mCellWidth;
    private int mDaySelectorRadius;

    private int mPaddedWidth;
    private int mPaddedHeight;

    //日历起始日期
    private LocalDate mMinDate;
    //日历截止日期
    private LocalDate mMaxDate;

    // 周视图,起始的日期(可能比最小日期 小)
    private LocalDate mStartDayOfWeek;

    private LocalDate mEndDayOfWeek;


    private LocalDate mActivatedDay;

    private LocalDate mToday;


    private String mWeekMonthYearLabel;

    @ColorInt
    private int mDayTextColor;
    @ColorInt
    private int mDayDisableTextColor;
    @ColorInt
    private int mDayActivatedTextColor;

    private SparseArray<List<Integer>> mMonthEvents;


    /**
     * 额外的事件 (当这周显示的日期涉及到跨月甚至跨年时的情况,对应上个月或下个月的时间 ) 是非常差的实现, 但由于种种原因,只能这样写. 服务器只能根据月份返回每月的event
     */
    private List<Integer> mExtraEventDay = Arrays.asList(29, 31);


    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources res = context.getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        mDesiredCellWidth = (int) (displayMetrics.density * 40 + 0.5f);
        mDesiredDayHeight = (int) (displayMetrics.density * 44 + 0.5f);
        mDesiredDaySelectorRadius = (int) (displayMetrics.density * 20 + 0.5f);
        mDayDisableTextColor = ContextCompat.getColor(context, android.R.color.darker_gray);
        mDayActivatedTextColor = Color.WHITE;
        mDayTextColor = Color.BLACK;

        initPaints(res);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                final int x = (int) (event.getX() + 0.5f);
                final int y = (int) (event.getY() + 0.5f);
                final LocalDate clickedDay = getDayAtLocation(x, y);
                onDayClicked(clickedDay);
                return true;
            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int preferredHeight = mDesiredDayHeight
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

    @Override
    protected void onDraw(Canvas canvas) {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        canvas.translate(paddingLeft, paddingTop);

        drawDays(canvas);
        drawEventBadge(canvas);
        canvas.translate(-paddingLeft, -paddingTop);
    }

    /**
     * Draws the month days.
     */
    private void drawDays(Canvas canvas) {
        final TextPaint solarP = mSolarDayPaint;
        final TextPaint lunarP = mLunarDayPaint;
        lunarP.setTextSize(solarP.getTextSize() * 0.75f);
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

        for (int i = 0; i < 7; i++) {
            final int colCenter = colWidth * i + colWidth / 2;

            LocalDate tempDay = mStartDayOfWeek.plusDays(i);
            final boolean isDayEnabled = isDayEnabled(tempDay);
            final boolean isDayActivated = mActivatedDay.equals(tempDay);
            final boolean isDayToday = mToday.equals(tempDay);
            if (isDayActivated) {
                final Paint paint = mDaySelectorPaint;
                canvas.drawCircle(colCenter, rowCenter, mDaySelectorRadius, paint);
            }
            final int dayTextColor;

            if (!isDayEnabled) {
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
            canvas.drawText(String.valueOf(tempDay.getDayOfMonth()), colCenter, solarBaseLineY, solarP);
            if (isShowLunar) {
                lunarBaseLineY = calculateBaseLineY(rowCenter, lunarP) + lOffset;

                LunarCalendar lunar = LunarCalendar.getInstance(tempDay.getYear(), tempDay.getMonthValue(), tempDay.getDayOfMonth());
                if (lunar != null) {
                    String lunarText = lunar.getLunarDay();
                    lunarP.setColor(dayTextColor);
                    canvas.drawText(lunarText, colCenter, lunarBaseLineY, lunarP);
                }

            }


        }

    }

    private void drawEventBadge(Canvas canvas) {
        if (mMonthEvents == null || mMonthEvents.size() == 0) return;

        final int rowHeight = mDayHeight;
        final int colWidth = mCellWidth;

        final Paint p = mDaySelectorPaint;
        p.setColor(Color.RED);

        float distance = (float) (Math.sin(45) * mDaySelectorRadius); //计算绘制坐标X,Y偏移量
        //圆点半径 16px
        final int radius = 10;
        final float centerY = rowHeight / 2 - distance;

        final int startDay = mStartDayOfWeek.getDayOfMonth();
        final int endDay = mEndDayOfWeek.getDayOfMonth();
        int startMonth = mStartDayOfWeek.getMonthValue();
        int endMonth = mEndDayOfWeek.getMonthValue();

        //由于种种原因,导致绘制周视图的Event 红点的代码相对复杂,  接口返回的Event是按月返回的,且换回的是一个天数数组,
        //而周视图 可能涉及到跨年跨月等情况
        if (startMonth == endMonth) { //不存在跨月的情况
            List<Integer> eventDays = mMonthEvents.get(startMonth);
            if (eventDays == null) return;
            for (Integer eventDay : eventDays) {
                if (eventDay >= startDay && eventDay <= endDay) {
                    int colCenter = (eventDay - startDay) * colWidth - colWidth / 2;
                    canvas.drawCircle(colCenter + distance, centerY, radius, p);
                }
            }
        } else {  //跨月的情况
            List<Integer> preEventDays = mMonthEvents.get(startMonth);
            List<Integer> nextEventDays = mMonthEvents.get(endMonth);

            if (preEventDays != null) {
                for (Integer preEventDay : preEventDays) {
                    if (preEventDay >= startDay) {
                        int colCenter = (preEventDay - startDay) * colWidth - colWidth / 2;
                        canvas.drawCircle(colCenter + distance, centerY, radius, p);

                    }
                }
            }

            if (nextEventDays != null) {
                for (Integer nextEventDay : nextEventDays) {
                    if (nextEventDay <= endDay) {
                        int colCenter = (6 - endDay + nextEventDay) * colWidth - colWidth / 2;
                        canvas.drawCircle(colCenter + distance, centerY, radius, p);
                    }
                }
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

    private LocalDate getDayAtLocation(int x, int y) {
        final int paddedX = x - getPaddingLeft();
        if (paddedX < 0 || paddedX >= mPaddedWidth) {
            return null;
        }

        final int paddedY = y - getPaddingTop();
        if (paddedY >= mPaddedHeight) {
            return null;
        }
        final int col = (paddedX * DAYS_IN_WEEK) / mPaddedWidth;
        return mStartDayOfWeek.plusDays(col);
    }

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


    /**
     * Sets the first day of the week.
     *
     * @param weekStart which day the week should start on, valid values are {@link Calendar#SUNDAY}
     *                  through {@link Calendar#SATURDAY}
     */
    public void setFirstDayOfWeek(@IntRange(from = 1, to = DAYS_IN_WEEK) int weekStart) {
        if (isValidDayOfWeek(weekStart)) {
            mWeekStart = weekStart;
        } else {
            mWeekStart = CalendarUtils.getFirstDayOfWeek();
        }

        invalidate();
    }

    public void setSelectedDay(LocalDate dayOfMonth) {
        if (mActivatedDay.equals(dayOfMonth)) {
            return;
        }
        mActivatedDay = dayOfMonth;
        invalidate();
    }

    private static boolean isValidDayOfWeek(int day) {
        return day >= 1 && day <= DAYS_IN_WEEK;
    }

    public void setOnDayClickListener(WeekView.OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }

    public void setWeekParams(LocalDate startDayOfWeek, LocalDate mSelectedDay,
                              LocalDate mMinDate, LocalDate mMaxDate, int weekStart, SparseArray<List<Integer>> monthEvents) {
        mStartDayOfWeek = startDayOfWeek;
        mEndDayOfWeek = startDayOfWeek.plusDays(6);
        this.mActivatedDay = mSelectedDay;
        this.mMinDate = mMinDate;
        this.mMaxDate = mMaxDate;
        this.mMonthEvents = monthEvents;

        if (isValidDayOfWeek(weekStart)) {
            mWeekStart = weekStart;
        } else {
            mWeekStart = DayOfWeek.SUNDAY.getValue();
        }

        mToday = LocalDate.now();
        updateWeekMonthYearLabel();
        invalidate();
    }

    private void updateWeekMonthYearLabel() {

        LocalDate tempDate = mStartDayOfWeek.plusDays(3);
        int week = tempDate.get(WeekFields.of(DayOfWeek.of(mWeekStart), 1).weekOfMonth());

        mWeekMonthYearLabel = String.format(Locale.getDefault(), "%d年%d月第%d周",
                tempDate.getYear(),
                tempDate.getMonthValue(),
                week);

    }

    /**
     * Called when the user clicks on a day. Handles callbacks to the {@link
     * WeekView.OnDayClickListener} if one is set.
     *
     * @param day the day that was clicked
     */
    private boolean onDayClicked(LocalDate day) {
        if (!isDayEnabled(day)) {
            return false;
        }

        if (mOnDayClickListener != null) {
            mOnDayClickListener.onDayClick(this, day);
        }
        return true;
    }

    private boolean isDayEnabled(LocalDate day) {
        return !day.isBefore(mMinDate) && !day.isAfter(mMaxDate);
    }


    public String getMonthYearLabel() {
        return mWeekMonthYearLabel;
    }

    public LocalDate getStartDayOfWeek() {
        return mStartDayOfWeek;
    }

    /**
     * Handles callbacks when the user clicks on a time object.
     */
    interface OnDayClickListener {
        void onDayClick(WeekView view, LocalDate day);
    }

    public void setShowLunar(boolean showLunar) {
        isShowLunar = showLunar;
        invalidate();
    }

}
