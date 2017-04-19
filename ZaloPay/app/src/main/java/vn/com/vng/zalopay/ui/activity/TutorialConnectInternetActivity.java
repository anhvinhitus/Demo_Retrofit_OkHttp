package vn.com.vng.zalopay.ui.activity;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.TutorialConnectInternetFragment;

public class TutorialConnectInternetActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TutorialConnectInternetFragment.newInstance();
    }
}
