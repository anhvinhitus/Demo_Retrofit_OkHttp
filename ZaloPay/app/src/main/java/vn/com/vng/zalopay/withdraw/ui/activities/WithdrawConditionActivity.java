package vn.com.vng.zalopay.withdraw.ui.activities;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.withdraw.ui.fragment.WithdrawConditionFragment;

public class WithdrawConditionActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return WithdrawConditionFragment.newInstance();
    }
}
