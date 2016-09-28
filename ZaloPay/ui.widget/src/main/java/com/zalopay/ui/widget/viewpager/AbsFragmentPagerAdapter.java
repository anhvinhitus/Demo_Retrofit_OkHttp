package com.zalopay.ui.widget.viewpager;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * Created by AnhHieu on 9/28/16.
 * *
 */

public abstract class AbsFragmentPagerAdapter extends FragmentPagerAdapter {

    private final SparseArrayCompat<WeakReference<Fragment>> holder;

    public AbsFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        this.holder = new SparseArrayCompat<>(getCount());
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object item = super.instantiateItem(container, position);
        if (item instanceof Fragment) {
            holder.put(position, new WeakReference<>((Fragment) item));
        }
        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        holder.remove(position);
        super.destroyItem(container, position, object);
    }

    @Nullable
    public Fragment getPage(int position) {
        final WeakReference<Fragment> weakRefItem = holder.get(position);
        return (weakRefItem != null) ? weakRefItem.get() : null;
    }

}
