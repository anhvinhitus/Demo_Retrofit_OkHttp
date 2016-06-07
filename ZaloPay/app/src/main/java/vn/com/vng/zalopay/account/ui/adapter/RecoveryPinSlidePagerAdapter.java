package vn.com.vng.zalopay.account.ui.adapter;

/**
 * Created by longlv on 03/06/2016.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import vn.com.vng.zalopay.account.ui.fragment.AbsProfileFragment;
import vn.com.vng.zalopay.account.ui.fragment.OTPRecoveryPinFragment;
import vn.com.vng.zalopay.account.ui.fragment.RecoveryPinFragment;

/**
 * A simple pager adapter that represents 2 ProfileSlidePagerAdapter objects, in
 * sequence.
 */
public class RecoveryPinSlidePagerAdapter extends FragmentStatePagerAdapter {

    private final int NUM_PAGES = 2;
    private AbsProfileFragment recoveryPinFragment;
    private AbsProfileFragment otpRecoveryPinFragment;

    public RecoveryPinSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            if (otpRecoveryPinFragment == null) {
                otpRecoveryPinFragment = OTPRecoveryPinFragment.newInstance();
            }
            return otpRecoveryPinFragment;
        } else {
            if (recoveryPinFragment == null) {
                recoveryPinFragment = RecoveryPinFragment.newInstance();
            }
            return recoveryPinFragment;
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