package vn.com.vng.zalopay.ui.activity;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.InvitationCodeFragment;

/**
 * Created by AnhHieu on 6/27/16.
 */
public class InvitationCodeActivity extends BaseActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker("", -1, -1);

    @Override
    public BaseFragment getFragmentToHost() {
        return InvitationCodeFragment.newInstance();
    }

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }
}
