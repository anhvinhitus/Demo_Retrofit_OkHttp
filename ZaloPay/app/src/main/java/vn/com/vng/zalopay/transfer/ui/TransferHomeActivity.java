package vn.com.vng.zalopay.transfer.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

public class TransferHomeActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return TransferHomeFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.MONEYTRANSFER;
    }
}
