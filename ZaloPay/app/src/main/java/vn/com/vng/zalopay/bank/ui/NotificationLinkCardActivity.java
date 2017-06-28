package vn.com.vng.zalopay.bank.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

public class NotificationLinkCardActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return NotificationLinkCardFragment.newInstance(getIntent().getExtras());
    }
}
