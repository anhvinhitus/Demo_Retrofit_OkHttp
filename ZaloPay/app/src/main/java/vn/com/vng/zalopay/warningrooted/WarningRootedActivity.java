package vn.com.vng.zalopay.warningrooted;

import android.support.annotation.NonNull;

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
}
