package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;

import vn.com.vng.zalopay.bank.list.BankListFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

/**
 * Created by datnt10 on 5/25/17.
 * Activity bank: content fragment bank
 */

public class BankActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }

        return BankListFragment.newInstance(bundle);
    }
}
