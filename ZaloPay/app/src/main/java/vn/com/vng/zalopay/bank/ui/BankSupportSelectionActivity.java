package vn.com.vng.zalopay.bank.ui;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by datnt10 on 5/25/17.
 */

public class BankSupportSelectionActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return BankSupportSelectionFragment.newInstance();
    }
}
