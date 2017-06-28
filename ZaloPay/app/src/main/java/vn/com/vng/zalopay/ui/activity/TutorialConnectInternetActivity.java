package vn.com.vng.zalopay.ui.activity;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.TutorialConnectInternetFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

public class TutorialConnectInternetActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TutorialConnectInternetFragment.newInstance();
    }
}
