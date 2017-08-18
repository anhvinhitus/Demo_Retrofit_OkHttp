package vn.com.vng.zalopay.account.ui.activities;

import android.content.Intent;
import android.support.annotation.NonNull;

import vn.com.vng.zalopay.account.ui.fragment.UpdateProfile3Fragment;
import vn.com.vng.zalopay.tracker.ActivityTracker;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by AnhHieu on 6/30/16.
 */
public class UpdateProfileLevel3Activity extends UserBaseToolBarActivity {
    private final ActivityTracker mActivityTracker = new ActivityTracker(ZPScreens.ME_PROFILE_IDENTIFY, -1, ZPEvents.ME_PROFILE_IDENTITY_BACK);
    private UpdateProfile3Fragment updateProfile3Fragment;

    @NonNull
    @Override
    protected ActivityTracker getTrackerInformation() {
        return mActivityTracker;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        boolean focusIdentity = getIntent().getBooleanExtra("focusIdentity", false);
        updateProfile3Fragment = UpdateProfile3Fragment.newInstance(focusIdentity);
        return updateProfile3Fragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (updateProfile3Fragment != null)
            updateProfile3Fragment.onActivityResult(requestCode, resultCode, data);
    }
}
