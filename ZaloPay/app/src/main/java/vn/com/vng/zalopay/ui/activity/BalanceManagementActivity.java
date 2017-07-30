package vn.com.vng.zalopay.ui.activity;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BalanceManagementFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

public class BalanceManagementActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.BALANCE, -1, -1);

    @Override
    public BaseFragment getFragmentToHost() {
        return BalanceManagementFragment.newInstance();
    }

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }
}
