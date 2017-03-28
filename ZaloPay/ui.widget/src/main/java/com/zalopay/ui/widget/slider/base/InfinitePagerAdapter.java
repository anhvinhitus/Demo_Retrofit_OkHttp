package com.zalopay.ui.widget.slider.base;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.zalopay.ui.widget.slider.SliderAdapter;

public class InfinitePagerAdapter extends PagerAdapter {

    private SliderAdapter adapter;

    public InfinitePagerAdapter(SliderAdapter adapter) {
        this.adapter = adapter;
    }

    public SliderAdapter getRealAdapter() {
        return this.adapter;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public int getRealCount() {
        return adapter.getCount();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (getRealCount() == 0) {
            return null;
        }
        int virtualPosition = position % getRealCount();
        return adapter.instantiateItem(container, virtualPosition);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (getRealCount() == 0) {
            return;
        }
        int virtualPosition = position % getRealCount();

        adapter.destroyItem(container, virtualPosition, object);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        adapter.finishUpdate(container);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return adapter.isViewFromObject(view, object);
    }

    @Override
    public void restoreState(Parcelable bundle, ClassLoader classLoader) {
        adapter.restoreState(bundle, classLoader);
    }

    @Override
    public Parcelable saveState() {
        return adapter.saveState();
    }

    @Override
    public void startUpdate(ViewGroup container) {
        adapter.startUpdate(container);
    }
}