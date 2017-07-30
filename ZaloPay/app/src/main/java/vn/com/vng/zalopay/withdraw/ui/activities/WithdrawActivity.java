package vn.com.vng.zalopay.withdraw.ui.activities;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.withdraw.ui.fragment.WithdrawFragment;
import vn.com.zalopay.analytics.ZPScreens;

public class WithdrawActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.BALANCE_WITHDRAW, -1, -1);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return WithdrawFragment.newInstance();
    }
}
