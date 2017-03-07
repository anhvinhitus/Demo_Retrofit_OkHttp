package vn.com.vng.zalopay.react.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;

/**
 * Created by hieuvm on 2/28/17.
 */

public class HomePagerAdapter extends AbsFragmentPagerAdapter {

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new Fragment();
            case 1:
                return InternalReactFragment.newInstance(ModuleName.NOTIFICATIONS);
            case 2:
                return ExternalReactFragment.newInstance(PaymentAppConfig.getAppResource(22));
            case 3:
                return new Fragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "HOME";
            case 1:
                return "NOTIFICATION";
            case 2:
                return "SHOW SHOW";
            case 3:
                return "PROFILE";
        }
        return null;
    }
}
