package vn.com.vng.zalopay.account.ui.activities;

import vn.com.vng.zalopay.account.ui.fragment.ChangePinContainerFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class ChangePinActivity extends BaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return ChangePinContainerFragment.newInstance();
    }
}
