package vn.com.vng.zalopay.transfer.ui;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class TransferViaZaloPayNameActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferViaZaloPayNameFragment.newInstance();
    }
}
