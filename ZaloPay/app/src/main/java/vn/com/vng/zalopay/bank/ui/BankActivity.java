package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;

import vn.com.vng.zalopay.bank.list.BankListFragment;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by datnt10 on 5/25/17.
 * Activity bank: content fragment bank
 */

public class BankActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.BANK_MAIN, ZPEvents.LINKBANK_LAUNCH, ZPEvents.LINKBANK_TOUCH_BACK);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }

        return BankListFragment.newInstance(bundle);
    }
}
