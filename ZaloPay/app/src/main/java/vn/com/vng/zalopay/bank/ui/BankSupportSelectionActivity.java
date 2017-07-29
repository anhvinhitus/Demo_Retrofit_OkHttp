package vn.com.vng.zalopay.bank.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by datnt10 on 5/25/17.
 */

public class BankSupportSelectionActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return BankSupportSelectionFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.BANK_BANKLIST;
    }

    @Override
    protected void getTrackingEventBack() {
        ZPAnalytics.trackEvent(ZPEvents.LINKBANK_ADD_LAUNCH);
    }
}
