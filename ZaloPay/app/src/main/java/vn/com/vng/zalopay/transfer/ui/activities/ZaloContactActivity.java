package vn.com.vng.zalopay.transfer.ui.activities;

import vn.com.vng.zalopay.transfer.ui.fragment.ZaloContactFragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class ZaloContactActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ZaloContactFragment.newInstance();
    }

}
