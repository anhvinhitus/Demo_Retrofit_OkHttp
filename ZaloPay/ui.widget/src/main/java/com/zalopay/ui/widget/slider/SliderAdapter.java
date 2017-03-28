package com.zalopay.ui.widget.slider;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class SliderAdapter extends PagerAdapter {

    private ArrayList<BaseSliderView> mImageContents;
    private final Object lock = new Object();

    SliderAdapter() {
        mImageContents = new ArrayList<>();
    }

    public <T extends BaseSliderView> void addSlider(T slider) {
        synchronized (lock) {
            mImageContents.add(slider);
        }
        notifyDataSetChanged();
    }

    public <T extends BaseSliderView> void addAllSlider(List<T> slider) {
        synchronized (lock) {
            mImageContents.addAll(slider);
        }
        notifyDataSetChanged();
    }

    public <T extends BaseSliderView> void setSliders(List<T> slider) {
        synchronized (lock) {
            mImageContents.clear();
            mImageContents.addAll(slider);
        }
        notifyDataSetChanged();
    }

    public BaseSliderView getSliderView(int position) {
        if (position < 0 || position >= mImageContents.size()) {
            return null;
        } else {
            return mImageContents.get(position);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public <T extends BaseSliderView> void removeSlider(T slider) {
        if (mImageContents.contains(slider)) {
            mImageContents.remove(slider);
            notifyDataSetChanged();
        }
    }

    public void removeSliderAt(int position) {
        if (mImageContents.size() > position) {
            mImageContents.remove(position);
            notifyDataSetChanged();
        }
    }

    public void removeAllSliders() {
        mImageContents.clear();
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return mImageContents.size();
    }

    @Override
    public int getCount() {
        return mImageContents.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        BaseSliderView b = mImageContents.get(position);
        View v = b.getView();
        container.addView(v);
        return v;
    }

}
