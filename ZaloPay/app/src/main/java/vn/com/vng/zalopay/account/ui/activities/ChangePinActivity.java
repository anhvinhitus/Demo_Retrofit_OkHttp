package vn.com.vng.zalopay.account.ui.activities;

import vn.com.vng.zalopay.account.ui.fragment.ChangePinContainerFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

public class ChangePinActivity extends UserBaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return ChangePinContainerFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.ME_CHANGEPASSWORD;
    }
}
