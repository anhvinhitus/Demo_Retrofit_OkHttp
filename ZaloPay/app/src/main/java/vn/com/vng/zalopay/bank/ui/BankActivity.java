package vn.com.vng.zalopay.bank.ui;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by Duke on 5/25/17.
 */

public class BankActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_bank;
    }

    @Override
    protected void setupActivityComponent() {
        getUserComponent().inject(this);
    }
}
