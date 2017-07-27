package vn.com.vng.zalopay.withdraw.ui.activities;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.vng.zalopay.withdraw.ui.fragment.WithdrawFragment;
import vn.com.zalopay.analytics.ZPScreens;

public class WithdrawActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return WithdrawFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.BALANCE_WITHDRAW;
    }
}
