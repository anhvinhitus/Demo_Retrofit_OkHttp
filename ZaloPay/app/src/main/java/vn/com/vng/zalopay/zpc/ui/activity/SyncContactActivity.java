package vn.com.vng.zalopay.zpc.ui.activity;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.zpc.ui.fragment.SyncContactFragment;

/**
 * Created by hieuvm on 7/21/17.
 * *
 */

public class SyncContactActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker("", -1, -1);

    @Override
    public BaseFragment getFragmentToHost() {
        return SyncContactFragment.newInstance();
    }

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }
}
