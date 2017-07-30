package vn.com.vng.zalopay.warningrooted;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseActivity;

public class WarningRootedActivity extends UserBaseActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker("", -1, -1);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return WarningRootedFragment.newInstance();
    }

}
