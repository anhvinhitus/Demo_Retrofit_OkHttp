package vn.com.vng.zalopay.account.ui.activities;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.account.ui.fragment.EditAccountNameFragment;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by AnhHieu on 8/12/16.
 *
 */
public class EditAccountNameActivity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.ME_PROFILE_ZPID, -1, -1);

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return EditAccountNameFragment.newInstance();
    }
}
