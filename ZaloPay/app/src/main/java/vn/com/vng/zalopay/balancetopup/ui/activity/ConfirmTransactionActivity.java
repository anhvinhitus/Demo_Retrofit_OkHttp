package vn.com.vng.zalopay.balancetopup.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.balancetopup.ui.fragment.ConfirmTransactionFragment;

public class ConfirmTransactionActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ConfirmTransactionFragment.newInstance(this.getIntent().getExtras());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
