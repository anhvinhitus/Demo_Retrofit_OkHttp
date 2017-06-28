package vn.com.vng.zalopay.balancetopup.ui.activity;

import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

public class BalanceTopupActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return BalanceTopupFragment.newInstance(getIntent().getExtras());
    }
}
