package vn.com.vng.zalopay.bank.ui;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class NotificationLinkCardActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return NotificationLinkCardFragment.newInstance(getIntent().getExtras());
    }
}
