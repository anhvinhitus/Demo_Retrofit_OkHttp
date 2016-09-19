package vn.com.vng.zalopay.account.ui.adapter;

/**
 * Created by longlv on 03/06/2016.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * A simple pager adapter that represents 2 ProfileSlidePagerAdapter objects, in
 * sequence.
 */
public class ProfileSlidePagerAdapter extends FragmentStatePagerAdapter {

    private final int NUM_PAGES = 2;
    private BaseFragment pinProfileFragment;
    private BaseFragment otpProfileFragment;

    public ProfileSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            if (otpProfileFragment == null) {
                otpProfileFragment = OtpProfileFragment.newInstance();
            }
            return otpProfileFragment;
        } else {
            if (pinProfileFragment == null) {
                pinProfileFragment = PinProfileFragment.newInstance();
            }
            return pinProfileFragment;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}