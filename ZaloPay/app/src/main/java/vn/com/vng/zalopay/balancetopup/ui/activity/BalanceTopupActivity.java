package vn.com.vng.zalopay.balancetopup.ui.activity;

import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class BalanceTopupActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return BalanceTopupFragment.newInstance();
    }

}
