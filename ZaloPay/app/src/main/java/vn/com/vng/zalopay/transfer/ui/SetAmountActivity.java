package vn.com.vng.zalopay.transfer.ui;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 8/29/16.
 * *
 */
public class SetAmountActivity extends BaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return SetAmountFragment.newInstance();
    }
}
