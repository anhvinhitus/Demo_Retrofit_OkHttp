package com.zalopay.ui.widget.slider;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.zalopay.ui.widget.slider.base.ViewPagerEx;

import java.lang.reflect.Field;

public class InfiniteViewPager extends ViewPagerEx {

    public InfiniteViewPager(Context context) {
        super(context);
    }

    public InfiniteViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSliderTransformDuration(int period, Interpolator interpolator) {
        try {
            Field mScroller = ViewPagerEx.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(getContext(), interpolator, period);
            mScroller.set(this, scroller);
        } catch (Exception ignore) {
        }
    }

    private static final class FixedSpeedScroller extends Scroller {

        private int mDuration = 1000;

        FixedSpeedScroller(Context context, Interpolator interpolator, int period) {
            super(context, interpolator);
            mDuration = period;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }
}