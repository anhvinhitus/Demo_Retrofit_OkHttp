package vn.com.zalopay.wallet.business.channel.pager.interfaces;

import android.support.v4.view.ViewPager;


public interface IPageChangeListener extends ViewPager.OnPageChangeListener {
    @Override
    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

    @Override
    void onPageScrollStateChanged(int state);

    @Override
    void onPageSelected(int position);
}
