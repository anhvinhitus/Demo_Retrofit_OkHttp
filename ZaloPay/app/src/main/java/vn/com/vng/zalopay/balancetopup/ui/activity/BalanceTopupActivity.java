package vn.com.vng.zalopay.balancetopup.ui.activity;

import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

public class BalanceTopupActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return BalanceTopupFragment.newInstance(getIntent().getExtras());
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.BALANCE_ADDCASH;
    }

    @Override
    protected void getTrackingEventBack() {
        ZPAnalytics.trackEvent(ZPEvents.BALANCE_TOUCH_BACK);
    }
}
