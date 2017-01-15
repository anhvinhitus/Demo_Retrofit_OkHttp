package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * Created by longlv on 09/09/2016.
 * Override duration of the scroll
 */
public class SmoothViewPager extends ViewPager {
    private final int DURATION = 600; //Duration of the scroll in milliseconds

    public SmoothViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMyScroller();
    }

    private void setMyScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        } catch (Exception e) {
            Timber.w(e, "Get Declared field 'mScroller' exception [%s]", e.getMessage());
        }
    }

    public class MyScroller extends Scroller {
        public MyScroller(Context context) {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, DURATION);
        }
    }
}
