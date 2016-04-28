package vn.com.vng.zalopay.balancetopup.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.balancetopup.ui.fragment.ConfirmTransactionFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class ConfirmTransactionActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ConfirmTransactionFragment.newInstance(this.getIntent().getExtras());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
