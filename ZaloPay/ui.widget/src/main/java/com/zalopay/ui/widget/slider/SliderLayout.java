package com.zalopay.ui.widget.slider;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.slider.base.InfinitePagerAdapter;
import com.zalopay.ui.widget.slider.base.PagerIndicator;
import com.zalopay.ui.widget.slider.base.ViewPagerEx;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SliderLayout extends RelativeLayout {

    private InfiniteViewPager mViewPager;

    private SliderAdapter mSliderAdapter;

    private PagerIndicator mIndicator;

    private Timer mCycleTimer;
    private TimerTask mCycleTask;

    private Timer mResumingTimer;
    private TimerTask mResumingTask;

    private boolean mCycling;

    private boolean mAutoRecover = true;

    private boolean mAutoCycle;

    private long mSliderDuration = 4000;

    public SliderLayout(Context context) {
        this(context, null);
    }

    public SliderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.SliderStyle);
    }

    public SliderLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.slider_image_layout, this, true);

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SliderLayout,
                defStyle, 0);

        int mTransformerSpan = attributes.getInteger(R.styleable.SliderLayout_pager_animation_span, 1100);
        mAutoCycle = attributes.getBoolean(R.styleable.SliderLayout_auto_cycle, false);

        mSliderAdapter = new SliderAdapter();
        PagerAdapter wrappedAdapter = new InfinitePagerAdapter(mSliderAdapter);

        mViewPager = (InfiniteViewPager) findViewById(R.id.pager);
        int contentColor = attributes.getColor(R.styleable.SliderLayout_content_background_color, Color.WHITE);
        mViewPager.setBackgroundColor(contentColor);

        int contentHeight = attributes.getDimensionPixelSize(R.styleable.SliderLayout_content_height, -1);
        if (contentHeight > 0) {
            ViewGroup.LayoutParams layoutParams = mViewPager.getLayoutParams();
            layoutParams.height = contentHeight;
            mViewPager.setLayoutParams(layoutParams);
        }

        mViewPager.setAdapter(wrappedAdapter);
        mViewPager.setSliderTransformDuration(mTransformerSpan, null);
        mIndicator = (PagerIndicator) findViewById(R.id.indicator);

        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        recoverCycle();
                        break;
                }
                return false;
            }
        });

        attributes.recycle();
        setCustomIndicator(mIndicator);
        mIndicator.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Visible);
        if (mAutoCycle) {
            startAutoCycle();
        }
    }

    public void setCustomIndicator(PagerIndicator indicator) {
        if (mIndicator != null) {
            mIndicator.destroySelf();
        }
        mIndicator = indicator;
        mIndicator.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Visible);
        mIndicator.setViewPager(mViewPager);
        mIndicator.redraw();
    }

    public void addOnPageChangeListener(ViewPagerEx.OnPageChangeListener onPageChangeListener) {
        if (onPageChangeListener != null) {
            mViewPager.addOnPageChangeListener(onPageChangeListener);
        }
    }

    public void removeOnPageChangeListener(ViewPagerEx.OnPageChangeListener onPageChangeListener) {
        mViewPager.removeOnPageChangeListener(onPageChangeListener);
    }

    public <T extends BaseSliderView> void addSlider(T imageContent) {
        mSliderAdapter.addSlider(imageContent);
    }

    public int getItemCount() {
        return mSliderAdapter.getItemCount();
    }

    public <T extends BaseSliderView> void addAllSlider(List<T> values) {
        mSliderAdapter.addAllSlider(values);
    }

    public <T extends BaseSliderView> void setSliders(List<T> values) {
        mSliderAdapter.setSliders(values);
    }

    private android.os.Handler mh = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            moveNextPosition(true);
        }
    };

    public void startAutoCycle() {
        startAutoCycle(mSliderDuration, mSliderDuration, mAutoRecover);
    }

    public void startAutoCycle(long delay, long duration, boolean autoRecover) {
        if (mCycleTimer != null) {
            mCycleTimer.cancel();
        }
        if (mCycleTask != null) {
            mCycleTask.cancel();
        }
        if (mResumingTask != null) {
            mResumingTask.cancel();
        }
        if (mResumingTimer != null) {
            mResumingTimer.cancel();
        }
        mSliderDuration = duration;
        mCycleTimer = new Timer();
        mAutoRecover = autoRecover;
        mCycleTask = new TimerTask() {
            @Override
            public void run() {
                mh.sendEmptyMessage(0);
            }
        };
        mCycleTimer.schedule(mCycleTask, delay, mSliderDuration);
        mCycling = true;
        mAutoCycle = true;
    }

    private void pauseAutoCycle() {
        if (mCycling) {
            mCycleTimer.cancel();
            mCycleTask.cancel();
            mCycling = false;
        } else {
            if (mResumingTimer != null && mResumingTask != null) {
                recoverCycle();
            }
        }
    }

    public void setDuration(long duration) {
        if (duration >= 500) {
            mSliderDuration = duration;
            if (mAutoCycle && mCycling) {
                startAutoCycle();
            }
        }
    }

    public void stopAutoCycle() {
        if (mCycleTask != null) {
            mCycleTask.cancel();
        }
        if (mCycleTimer != null) {
            mCycleTimer.cancel();
        }
        if (mResumingTimer != null) {
            mResumingTimer.cancel();
        }
        if (mResumingTask != null) {
            mResumingTask.cancel();
        }
        mAutoCycle = false;
        mCycling = false;
    }

    private void recoverCycle() {
        if (!mAutoRecover || !mAutoCycle) {
            return;
        }

        if (!mCycling) {
            if (mResumingTask != null && mResumingTimer != null) {
                mResumingTimer.cancel();
                mResumingTask.cancel();
            }
            mResumingTimer = new Timer();
            mResumingTask = new TimerTask() {
                @Override
                public void run() {
                    startAutoCycle();
                }
            };
            mResumingTimer.schedule(mResumingTask, 6000);
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                pauseAutoCycle();
                break;
        }
        return false;
    }
    
    private InfinitePagerAdapter getWrapperAdapter() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter != null) {
            return (InfinitePagerAdapter) adapter;
        } else {
            return null;
        }
    }

    private SliderAdapter getRealAdapter() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter != null) {
            return ((InfinitePagerAdapter) adapter).getRealAdapter();
        }
        return null;
    }

    public int getCurrentPosition() {
        if (getRealAdapter() == null) {
            throw new IllegalStateException("You did not set a slider adapter");
        }
        return mViewPager.getCurrentItem() % getRealAdapter().getCount();

    }

    public BaseSliderView getCurrentSlider() {
        if (getRealAdapter() == null) {
            throw new IllegalStateException("You did not set a slider adapter");
        }

        int count = getRealAdapter().getCount();
        int realCount = mViewPager.getCurrentItem() % count;
        return getRealAdapter().getSliderView(realCount);
    }

    public void removeSliderAt(int position) {
        if (getRealAdapter() != null) {
            getRealAdapter().removeSliderAt(position);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem(), false);
        }
    }

    public void removeAllSliders() {
        if (getRealAdapter() != null) {
            int count = getRealAdapter().getCount();
            getRealAdapter().removeAllSliders();
            //a small bug, but fixed by this trick.
            //bug: when remove adapter's all the sliders.some caching slider still alive.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + count, false);
        }
    }

    public void setCurrentPosition(int position, boolean smooth) {
        if (getRealAdapter() == null) {
            throw new IllegalStateException("You did not set a slider adapter");
        }
        if (position >= getRealAdapter().getCount()) {
            throw new IllegalStateException("Item position is not exist");
        }
        int p = mViewPager.getCurrentItem() % getRealAdapter().getCount();
        int n = (position - p) + mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(n, smooth);
    }

    public void setCurrentPosition(int position) {
        setCurrentPosition(position, true);
    }

    public void movePrevPosition(boolean smooth) {
        if (getRealAdapter() == null) {
            throw new IllegalStateException("You did not set a slider adapter");
        }
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, smooth);
    }

    public void movePrevPosition() {
        movePrevPosition(true);
    }

    public void moveNextPosition(boolean smooth) {
        if (getRealAdapter() == null) {
            throw new IllegalStateException("You did not set a slider adapter");
        }
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, smooth);
    }

    public void moveNextPosition() {
        moveNextPosition(true);
    }
}
