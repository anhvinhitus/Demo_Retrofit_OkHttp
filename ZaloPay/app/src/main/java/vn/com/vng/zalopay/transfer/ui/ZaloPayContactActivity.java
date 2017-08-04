package vn.com.vng.zalopay.transfer.ui;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.transfer.ui.friendlist.ZaloPayContactListFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;
import android.os.Bundle;

public class ZaloPayContactActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.MONEYTRANSFER_ZFRIEND, -1, ZPEvents.MONEYTRANSFER_ZFRIEND_TOUCH_BACK);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ZaloPayContactListFragment.newInstance(getExtras());
    }

    private Bundle getExtras() {
        if (getIntent() == null || getIntent().getExtras() == null) {
            return new Bundle();
        }
        return getIntent().getExtras();
    }
}
