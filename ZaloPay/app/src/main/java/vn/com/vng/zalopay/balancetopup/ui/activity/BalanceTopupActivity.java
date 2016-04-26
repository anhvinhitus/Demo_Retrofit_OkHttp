package vn.com.vng.zalopay.balancetopup.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class BalanceTopupActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return new BalanceTopupFragment().newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_topup);
    }
}
