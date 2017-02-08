package vn.com.vng.zalopay.bank.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

/**
 * Created by longlv on 1/17/17.
 * *
 */

class LinkBankPagerAdapter extends AbsFragmentPagerAdapter {

    LinkBankPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return LinkCardFragment.newInstance();
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