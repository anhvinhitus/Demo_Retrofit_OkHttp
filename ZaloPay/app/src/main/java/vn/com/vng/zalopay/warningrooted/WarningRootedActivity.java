package vn.com.vng.zalopay.warningrooted;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseActivity;

public class WarningRootedActivity extends UserBaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return WarningRootedFragment.newInstance();
    }

    @NonNull
    @Override
    protected String getTrackingScreenName() {
        return "";
    }

    /**
     * Get eventId for given event type.
     * Return -1 if activity does not have matching eventId
     *
     * @param eventType event type
     */
    @Override
    protected int getEventId(EventType eventType) {
        return -1;
    }
}
