package vn.com.vng.zalopay.webapp;

import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class WebAppActivity extends BaseActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return WebAppFragment.newInstance(getIntent().getExtras());
    }
}
