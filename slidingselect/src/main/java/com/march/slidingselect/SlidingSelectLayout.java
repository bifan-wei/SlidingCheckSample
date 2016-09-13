package com.march.slidingselect;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.march.slidingcheck.R;

/**
 * Project  : SlidingCheckSample
 * Package  : com.march.slidingcheck
 * CreateAt : 16/9/12
 * Describe :
 *
 * @author chendong
 */

public class SlidingSelectLayout extends FrameLayout {

    public SlidingSelectLayout(Context context) {
        this(context, null);
    }

    public SlidingSelectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTagKey(R.id.sliding_pos, R.id.sliding_data);
    }

    private static final float TOUCH_SLOP_RATE = 0.15f;
    // 初始化值
    private static final int INVALID_PARAM = -1;
    // 滑动选中监听
    private OnSlidingSelectListener onSlidingSelectListener;

    private int offsetTop = 0;
    // 横轴滑动阈值，超过阈值表示触发横轴滑动
    private float xTouchSlop;
    // 纵轴滑动阈值，超过阈值表示触发纵轴滑动
    private float yTouchSlop;
    // 横向的item数量
    private int itemSpanCount = INVALID_PARAM;
    // 内部的rv
    private RecyclerView mTargetRv;

    // down 事件初始值
    private float mInitialDownX;
    // down 事件初始值
    private float mInitialDownY;
    // 是否正在滑动
    private boolean isBeingSlide;

    private int tagPosKey;
    private int tagDataKey;

    private int preViewPos = INVALID_PARAM;


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled())
            return super.onInterceptTouchEvent(ev);
        ensureTarget();
        ensureLayoutManager();
        if (!isReadyToIntercept())
            return super.onInterceptTouchEvent(ev);
        int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // init
                mInitialDownX = ev.getX();
                mInitialDownY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                // stop
                isBeingSlide = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // handle
                float xDiff = Math.abs(ev.getX() - mInitialDownX);
                float yDiff = Math.abs(ev.getY() - mInitialDownY);
                if (yDiff < xTouchSlop && xDiff > yTouchSlop) {
                    isBeingSlide = true;
                }
                break;
        }
        return isBeingSlide;
    }


    private float generateX(float x) {
        return x;
    }

    private float generateY(float y) {
        return y - offsetTop;
    }


    private void setTargetRv(RecyclerView mTargetRv) {
        this.mTargetRv = mTargetRv;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                // re init
                isBeingSlide = false;
                preViewPos = INVALID_PARAM;
                break;
            case MotionEvent.ACTION_MOVE:
                publishSlidingCheck(ev);
                break;
        }
        return isBeingSlide;
    }

    private void ensureLayoutManager() {
        if (mTargetRv == null || itemSpanCount != INVALID_PARAM)
            return;
        RecyclerView.LayoutManager lm = mTargetRv.getLayoutManager();
        if (lm == null)
            return;
        if (lm instanceof GridLayoutManager) {
            GridLayoutManager glm = (GridLayoutManager) lm;
            itemSpanCount = glm.getSpanCount();
        } else {
            itemSpanCount = 4;
        }
        int size = (int) (getResources().getDisplayMetrics().widthPixels / (itemSpanCount * 1.0f));
        xTouchSlop = yTouchSlop = size * TOUCH_SLOP_RATE;
    }


    private void publishSlidingCheck(MotionEvent event) {
        float x = generateX(event.getX());
        float y = generateY(event.getY());
        View childViewUnder = mTargetRv.findChildViewUnder(x, y);
        // fast stop
        if (onSlidingSelectListener == null || childViewUnder == null)
            return;
        int pos = getPos(childViewUnder);
        Object data = getData(childViewUnder);
        // fast stop
        if (pos == INVALID_PARAM || preViewPos == pos || data == null)
            return;

        try {
            onSlidingSelectListener.onSlidingSelect(pos, childViewUnder, data);
            preViewPos = pos;
        } catch (ClassCastException e) {
            Log.e("SlidingSelect", "ClassCastException:填写的范型有误，无法转换");
        }
    }

    private void setTagKey(int tagPosKey, int tagDataKey) {
        this.tagPosKey = tagPosKey;
        this.tagDataKey = tagDataKey;
    }

    public void markView(View parentView, int pos, Object data) {
        parentView.setTag(tagPosKey, pos);
        parentView.setTag(tagDataKey, data);
    }

    private int getPos(View parentView) {
        int pos = INVALID_PARAM;
        Object tag = parentView.getTag(tagPosKey);
        if (tag != null)
            pos = (int) tag;
        return pos;
    }

    private Object getData(View parentView) {
        return parentView.getTag(tagDataKey);
    }

    /**
     * 是否可以开始拦截处理事件，当recyclerView数据完全ok之后开始
     *
     * @return 是否可以开始拦截处理事件
     */

    private boolean isReadyToIntercept() {
        return mTargetRv != null
                && mTargetRv.getAdapter() != null
                && itemSpanCount != INVALID_PARAM;
    }

    /**
     * 获取RecyclerView
     */
    private void ensureTarget() {
        if (mTargetRv != null)
            return;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof RecyclerView) {
                mTargetRv = (RecyclerView) childAt;
                return;
            }
        }
    }

    public void setOffsetTop(int offsetTop) {
        this.offsetTop = offsetTop;
    }

    public <D> void setOnSlidingSelectListener(OnSlidingSelectListener<D> onSlidingCheckListener) {
        this.onSlidingSelectListener = onSlidingCheckListener;
    }

    public interface OnSlidingSelectListener<D> {
        void onSlidingSelect(int pos, View parentView, D data);
    }
}

