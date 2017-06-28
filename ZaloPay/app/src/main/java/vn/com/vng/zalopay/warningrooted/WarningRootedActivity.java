package vn.com.vng.zalopay.warningrooted;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseActivity;

public class WarningRootedActivity extends UserBaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return WarningRootedFragment.newInstance();
    }
}
