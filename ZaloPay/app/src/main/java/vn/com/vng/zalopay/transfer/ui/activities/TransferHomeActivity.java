package vn.com.vng.zalopay.transfer.ui.activities;

import vn.com.vng.zalopay.transfer.ui.fragment.TransferHomeFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class TransferHomeActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferHomeFragment.newInstance();
    }
}
