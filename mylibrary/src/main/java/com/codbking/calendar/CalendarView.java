package com.codbking.calendar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

/**
 * Created by codbking on 2016/12/18. email:codbking@gmail.com github:https://github.com/codbking
 * blog:http://www.jianshu.com/users/49d47538a2dd/latest_articles
 */

public class CalendarView extends ViewGroup {

    private static final String TAG = "CalendarView";

    private int mSelectedPosition = -1;

    private CalendarAdapter adapter;
    private List<CalendarBean> data;
    private OnItemClickListener onItemClickListener;

    private int row;
    private int column = 7;
    private int itemHeight;

    private boolean isCurrentMonth;

    private boolean mShowPreAndNext = false;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, CalendarBean bean);
    }

    public CalendarView(Context context, int row, boolean showPreAndNext) {
        super(context);
        this.row = row;
        this.mShowPreAndNext = showPreAndNext;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public void setAdapter(CalendarAdapter adapter) {
        this.adapter = adapter;
    }

    public void setData(List<CalendarBean> data, boolean isCurrentMonth) {
        this.data = data;
        this.isCurrentMonth = isCurrentMonth;
        setItem();
        requestLayout();

    }

    private void setItem() {

        mSelectedPosition = -1;
        if (adapter == null) {
            throw new RuntimeException("adapter is null,please setAdapter");
        }
        int[] ymdAttrs = CalendarUtil.getYMD(new Date());
        for (int i = 0; i < data.size(); i++) {
            CalendarBean bean = data.get(i);
            View view = getChildAt(i);
            View childView = adapter.getView(view, this, bean);

            if (view == null || view != childView) {
                addViewInLayout(childView, i, childView.getLayoutParams(), true);
            }
            //设置默认选中的日期
            if (mSelectedPosition == -1) {
                if (!isCurrentMonth && bean.day == 1 //不是当月设置默认选中第一天
                        || bean.year == ymdAttrs[0] && bean.moth == ymdAttrs[1] && bean.day == ymdAttrs[2]) {
                    mSelectedPosition = i;
                }
            }

            if (!mShowPreAndNext && bean.mothFlag != CalendarBean.SELECTED) {
                childView.setVisibility(INVISIBLE);
            } else {
                childView.setVisibility(VISIBLE);
                childView.setSelected(mSelectedPosition == i);
                setItemClick(childView, i, bean);
            }


        }
    }

    public Object[] getSelect() {
        return new Object[]{getChildAt(mSelectedPosition), mSelectedPosition, data.get(mSelectedPosition)};
    }

    public void setItemClick(final View view, final int potsition, final CalendarBean bean) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectedPosition != -1) {
                    getChildAt(mSelectedPosition).setSelected(false);
                    getChildAt(potsition).setSelected(true);
                }
                mSelectedPosition = potsition;

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, potsition, bean);
                }
            }
        });
    }

    public int[] getSelectedPosition() {
        Rect rect = new Rect();
        try {
            getChildAt(mSelectedPosition).getHitRect(rect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{rect.left, rect.top, rect.right, rect.top};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY));

        int itemWidth = parentWidth / column;
        itemHeight = itemWidth;

        View view = getChildAt(0);
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null && params.height > 0) {
            itemHeight = params.height;
        }
        setMeasuredDimension(parentWidth, itemHeight * row);


        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            childView.measure(MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY));
        }

        Log.i(TAG, "onMeasure() called with: itemHeight = [" + itemHeight + "], itemWidth = [" + itemWidth + "]");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            layoutChild(getChildAt(i), i, l, t, r, b);
        }
    }

    private void layoutChild(View view, int postion, int l, int t, int r, int b) {

        int cc = postion % column;
        int cr = postion / column;

        int itemWidth = view.getMeasuredWidth();
        int itemHeight = view.getMeasuredHeight();

        l = cc * itemWidth;
        t = cr * itemHeight;
        r = l + itemWidth;
        b = t + itemHeight;
        view.layout(l, t, r, b);

    }
}
