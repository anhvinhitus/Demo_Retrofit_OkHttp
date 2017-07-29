package vn.com.vng.zalopay.protect.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by hieuvm on 12/24/16.
 */

public class ProtectAccountActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ProtectAccountFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.ME_SECURITY;
    }

    @Override
    protected void getTrackingEventBack() {
        ZPAnalytics.trackEvent(ZPEvents.ME_SECURITY_TOUCH_BACK);
    }
}
