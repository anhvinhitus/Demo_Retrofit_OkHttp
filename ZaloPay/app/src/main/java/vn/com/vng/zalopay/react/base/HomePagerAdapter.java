package vn.com.vng.zalopay.react.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.ui.fragment.tabmain.PersonalFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;

/**
 * Created by hieuvm on 2/28/17.
 * *
 */

public class HomePagerAdapter extends AbsFragmentPagerAdapter {

    private static final int TAB_COUNT = 3;
    public static final int TAB_MAIN_INDEX = 0;
    public static final int TAB_SHOW_SHOW_INDEX = 1;
    public static final int TAB_PERSONAL_INDEX = 2;

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case TAB_MAIN_INDEX:
                return ZaloPayFragment.newInstance();
            case TAB_SHOW_SHOW_INDEX:
                return ExternalReactFragment.newInstance(PaymentAppConfig.getAppResource(22));
            case TAB_PERSONAL_INDEX:
                return PersonalFragment.newInstance();
        }

        return null;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

}
