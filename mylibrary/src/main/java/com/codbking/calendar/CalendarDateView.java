package com.codbking.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.LinkedList;

/**
 * Created by codbking on 2016/12/18. email:codbking@gmail.com github:https://github.com/codbking
 * blog:http://www.jianshu.com/users/49d47538a2dd/latest_articles
 */

public class CalendarDateView extends ViewPager implements CalendarTopView {

    SparseArray<CalendarView> mCalendarViews = new SparseArray<>();
    private CalendarTopViewChangeListener mCalendarLayoutChangeListener;
    private CalendarView.OnItemClickListener onItemClickListener;


    private boolean mShowPreAndNext = false;

    private CalendarAdapter mAdapter;
    private int calendarItemHeight = 0;

    public void setAdapter(CalendarAdapter adapter) {
        mAdapter = adapter;
        initData();
    }

    public void setOnItemClickListener(CalendarView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public CalendarDateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarDateView);
        mShowPreAndNext = a.getBoolean(R.styleable.CalendarDateView_cdv_show_pre_and_next, false);
        a.recycle();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int calendarHeight = 0;
        if (getAdapter() != null) {
            CalendarView view = (CalendarView) getChildAt(0);
            if (view != null) {
                calendarHeight = view.getMeasuredHeight();
                calendarItemHeight = view.getItemHeight();
            }
        }
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(calendarHeight, MeasureSpec.EXACTLY));
    }

    private void init() {
        final int[] dateArr = CalendarUtil.getYMD(new Date());

        setAdapter(new PagerAdapter() {
          //  private LinkedList<CalendarView> mCache = new LinkedList();

            @Override
            public int getCount() {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {

                int year = dateArr[0];
                int month = dateArr[1] + position - Integer.MAX_VALUE / 2;
                //计算需要的行数
                int row = CalendarUtil.getRowsNeed(dateArr[0], dateArr[1] + position - Integer.MAX_VALUE / 2);
                CalendarFactory factory = CalendarFactory.getInstance();

                CalendarView calendarView;
//                if (!mCache.isEmpty()) {
//                    calendarView = mCache.removeFirst();
//                } else {
                    calendarView = new CalendarView(container.getContext(), row, mShowPreAndNext);
              //  }
                calendarView.setOnItemClickListener(onItemClickListener);
                calendarView.setAdapter(mAdapter);
                calendarView.setData(factory.getMonthOfDayList(year, month), position == Integer.MAX_VALUE / 2);
                container.addView(calendarView);
                mCalendarViews.put(position, calendarView);
                return calendarView;
            }


            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
              //  mCache.addLast((CalendarView) object);
                mCalendarViews.remove(position);
            }
        });

        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (onItemClickListener != null) {
                    CalendarView view = mCalendarViews.get(position);
                    Object[] obs = view.getSelect();
                    onItemClickListener.onItemClick((View) obs[0], (int) obs[1], (CalendarBean) obs[2]);
                }
                mCalendarLayoutChangeListener.onLayoutChange(CalendarDateView.this);
            }
        });
    }


    private void initData() {
        setCurrentItem(Integer.MAX_VALUE / 2, false);
        getAdapter().notifyDataSetChanged();

    }

    @Override
    public int[] getCurrentSelectPosition() {
        CalendarView view = mCalendarViews.get(getCurrentItem());
        if (view == null) {
            view = (CalendarView) getChildAt(0);
        }
        if (view != null) {
            return view.getSelectedPosition();
        }
        return new int[4];
    }

    @Override
    public int getItemHeight() {
        return calendarItemHeight;
    }

    @Override
    public void setCalendarTopViewChangeListener(CalendarTopViewChangeListener listener) {
        mCalendarLayoutChangeListener = listener;
    }


}
