package vn.com.vng.zalopay.transfer.ui.activities;

import vn.com.vng.zalopay.transfer.ui.fragment.MyQRCodeFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 8/25/16.
 */
public class MyQRCodeActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return MyQRCodeFragment.newInstance();
    }
}
