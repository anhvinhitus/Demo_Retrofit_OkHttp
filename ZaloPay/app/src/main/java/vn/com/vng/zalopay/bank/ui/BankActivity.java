package vn.com.vng.zalopay.bank.ui;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by datnt10 on 5/25/17.
 * Activity bank: content fragment bank
 */

public class BankActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return new BankFragment();
    }

    @Override
    protected void setupActivityComponent() {
        getUserComponent().inject(this);
    }
}
