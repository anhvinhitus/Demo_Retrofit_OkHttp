package vn.com.vng.zalopay.withdraw.ui.activities;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.withdraw.ui.fragment.WithdrawFragment;

public class WithdrawActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return WithdrawFragment.newInstance();
    }
}
