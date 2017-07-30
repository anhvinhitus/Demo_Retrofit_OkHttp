package vn.com.vng.zalopay.transfer.ui;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.transfer.ui.friendlist.ZaloFriendListFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

public class ZaloContactActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.MONEYTRANSFER_ZFRIEND, -1, ZPEvents.MONEYTRANSFER_ZFRIEND_TOUCH_BACK);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ZaloFriendListFragment.newInstance();
    }
}
