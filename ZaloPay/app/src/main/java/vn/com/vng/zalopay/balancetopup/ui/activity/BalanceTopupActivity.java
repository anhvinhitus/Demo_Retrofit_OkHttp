package vn.com.vng.zalopay.balancetopup.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.balancetopup.ui.fragment.BalanceTopupFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class BalanceTopupActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return new BalanceTopupFragment().newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplication.instance().getUserComponent().inject(this);
    }
}
