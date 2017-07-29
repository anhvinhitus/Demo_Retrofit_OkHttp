package vn.com.vng.zalopay.transfer.ui;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 8/29/16.
 * *
 */
public class SetAmountActivity extends UserBaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return SetAmountFragment.newInstance();
    }

    @NonNull
    @Override
    protected String getTrackingScreenName() {
        return "";
    }

    @Override
    protected void getTrackingEventBack() {
        ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_SETAMOUNT_BACK);
    }
}
