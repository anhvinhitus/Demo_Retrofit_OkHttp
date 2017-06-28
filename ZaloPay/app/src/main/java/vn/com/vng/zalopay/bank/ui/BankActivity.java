package vn.com.vng.zalopay.bank.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

/**
 * Created by datnt10 on 5/25/17.
 * Activity bank: content fragment bank
 */

public class BankActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return BankFragment.newInstance(getIntent().getExtras());
    }
}
