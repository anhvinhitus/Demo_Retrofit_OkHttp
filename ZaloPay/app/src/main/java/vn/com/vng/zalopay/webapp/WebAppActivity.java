package vn.com.vng.zalopay.webapp;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class WebAppActivity extends BaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return WebAppFragment.newInstance(getIntent().getExtras());
    }
}
