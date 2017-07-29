package vn.com.vng.zalopay.transfer.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

public class TransferHomeActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferHomeFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.MONEYTRANSFER;
    }

    @Override
    protected void getTrackingEventBack() {
        ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_TOUCH_BACK);
    }

    @Override
    protected void getTrackingEventLaunch() {
        ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_LAUNCH);
    }
}
