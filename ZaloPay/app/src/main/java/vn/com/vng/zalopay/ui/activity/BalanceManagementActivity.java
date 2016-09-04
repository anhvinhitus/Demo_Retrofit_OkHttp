package vn.com.vng.zalopay.ui.activity;

import vn.com.vng.zalopay.ui.fragment.BalanceManagementFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class BalanceManagementActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return BalanceManagementFragment.newInstance();
    }
}
