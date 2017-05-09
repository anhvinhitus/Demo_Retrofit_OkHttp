package vn.com.vng.zalopay.ui.activity;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.IntroAppFragment;

public class IntroAppActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return IntroAppFragment.newInstance();
    }
}
