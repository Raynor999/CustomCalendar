package com.example.calendar_agenda;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.example.calendar_agenda.util.CalendarUtils;

import java.util.Locale;

/**
 * Created by lijunguan on 2017/1/11. email: lijunguan199210@gmail.com blog:
 * https://lijunguan.github.io
 */

public class WeekBarView extends View {

    @ColorInt
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#666666");

    private int mWeekStart ;

    private final TextPaint mDayOfWeekPaint = new TextPaint();

    private int mWeekTextSize;

    private int mWeekTextColor;

    private DisplayMetrics mMetrics;

    private Locale mLocale;

    /**
     * Array of single-character weekday labels ordered by column index.
     */
    private final String[] mDayOfWeekLabels = new String[7];


    public WeekBarView(Context context) {
        this(context, null);
    }

    public WeekBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLocale = getResources().getConfiguration().locale;
        mMetrics = getResources().getDisplayMetrics();
        mWeekStart = CalendarUtils.getFirstDayOfWeek(mLocale);
        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeekBarView);
        mWeekTextColor = a.getColor(R.styleable.WeekBarView_week_text_color, DEFAULT_TEXT_COLOR);
        mWeekTextSize = a.getDimensionPixelSize(R.styleable.WeekBarView_week_text_size, (int) (13 * mMetrics.scaledDensity));
        a.recycle();
    }

    private void initPaint() {
        mDayOfWeekPaint.setAntiAlias(true);
        mDayOfWeekPaint.setTextSize(mWeekTextSize);
        mDayOfWeekPaint.setColor(mWeekTextColor);
        mDayOfWeekPaint.setTextAlign(Paint.Align.CENTER);
        mDayOfWeekPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 设置一周的第一天从星期几开始
     *
     * @param weekStart 那一天应该作为一周的开始, 有效的取值范围是 1~7,  eg 1 等价于 Mon
     */
    public void setFirstDayOfWeek(@IntRange(from = 1, to = 7) int weekStart) {
        mWeekStart = weekStart;
        updateDayOfWeekLabels();
        // Invalidate cached accessibility information.
        invalidate();
    }

    private void updateDayOfWeekLabels() {
        // for this list correspond to Calendar days, e.g. SUNDAY is index 0.
        final String[] tinyWeekdayNames = CalendarUtils.getTinyWeekdayNames(mLocale);
        for (int i = 0; i < 7; i++) {
            mDayOfWeekLabels[i] = tinyWeekdayNames[(mWeekStart + i - 1) % 7];
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST) { // 没有指定值的情况下,设置默认高度为32dp
            heightSize = mMetrics.densityDpi * 36;
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = mMetrics.densityDpi * 300;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final TextPaint p = mDayOfWeekPaint;
        final int rowHeight = getHeight();
        final int colWidth = getWidth() / 7;
        // 计算文本和行高,用来使文本居中显示
        final float halfLineHeight = (p.ascent() + p.descent()) / 2;
        final int rowCenter = rowHeight / 2;
        //绘制周label eg  Mon ,Thu...
        for (int i = 0; i < mDayOfWeekLabels.length; i++) {
            final String label = mDayOfWeekLabels[i];
            int labelWith = (int) p.measureText(label);
            int startX = colWidth * i + (colWidth - labelWith) / 2;
            canvas.drawText(label, startX, rowCenter - halfLineHeight, p);
        }
    }
}
