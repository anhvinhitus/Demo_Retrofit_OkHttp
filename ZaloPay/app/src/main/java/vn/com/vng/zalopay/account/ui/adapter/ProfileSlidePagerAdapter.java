package vn.com.vng.zalopay.account.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

import vn.com.vng.zalopay.account.ui.fragment.OtpProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.PinProfileFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * A simple pager adapter that represents 2 ProfileSlidePagerAdapter objects, in
 * sequence.
 */
public class ProfileSlidePagerAdapter extends AbsFragmentPagerAdapter {

    public ProfileSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return PinProfileFragment.newInstance();
            case 1:
                return OtpProfileFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}