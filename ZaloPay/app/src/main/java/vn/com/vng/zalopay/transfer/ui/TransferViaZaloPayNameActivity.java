package vn.com.vng.zalopay.transfer.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

/**
 * Created by AnhHieu on 9/14/16.
 * *
 */
public class TransferViaZaloPayNameActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferViaZaloPayNameFragment.newInstance();
    }
}
