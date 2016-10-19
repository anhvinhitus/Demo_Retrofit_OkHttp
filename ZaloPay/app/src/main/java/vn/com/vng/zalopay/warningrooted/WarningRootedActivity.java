package vn.com.vng.zalopay.warningrooted;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class WarningRootedActivity extends BaseToolBarActivity {


    @Override
    public BaseFragment getFragmentToHost() {
        return WarningRootedFragment.newInstance();
    }
}
