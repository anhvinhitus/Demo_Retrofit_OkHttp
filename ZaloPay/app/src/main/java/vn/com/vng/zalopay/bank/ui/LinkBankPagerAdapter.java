package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

/**
 * Created by longlv on 1/17/17.
 * *
 */

class LinkBankPagerAdapter extends AbsFragmentPagerAdapter {
    private Bundle mBundle;

    LinkBankPagerAdapter(FragmentManager fm, Bundle bundle) {
        super(fm);
        mBundle = bundle;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return LinkCardFragment.newInstance(mBundle);
            case 1:
                return LinkAccountFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "LIÊN KẾT THẺ";
            case 1:
                return "LIÊN KẾT TÀI KHOẢN";
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

}