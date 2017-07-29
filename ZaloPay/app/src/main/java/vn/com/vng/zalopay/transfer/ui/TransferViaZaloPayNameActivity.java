package vn.com.vng.zalopay.transfer.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class TransferViaZaloPayNameActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferViaZaloPayNameFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.MONEYTRANSFER_INPUTZPID;
    }

    @Override
    protected void getTrackingEventBack() {
        ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_ZPID_TOUCH_BACK);
    }
}
