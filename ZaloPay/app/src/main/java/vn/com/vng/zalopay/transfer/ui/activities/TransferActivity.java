package vn.com.vng.zalopay.transfer.ui.activities;

import vn.com.vng.zalopay.transfer.ui.fragment.TransferFragment;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferFragment2;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class TransferActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferFragment.newInstance(getIntent().getExtras());
    }
}
