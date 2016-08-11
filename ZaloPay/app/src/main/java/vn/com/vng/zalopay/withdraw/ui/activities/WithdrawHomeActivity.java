package vn.com.vng.zalopay.withdraw.ui.activities;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.withdraw.ui.fragment.WithdrawHomeFragment;

public class WithdrawHomeActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return WithdrawHomeFragment.newInstance();
    }
}
