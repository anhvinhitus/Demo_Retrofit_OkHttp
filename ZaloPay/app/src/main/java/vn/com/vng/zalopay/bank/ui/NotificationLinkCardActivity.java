package vn.com.vng.zalopay.bank.ui;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

public class NotificationLinkCardActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return NotificationLinkCardFragment.newInstance(getIntent().getExtras());
    }

    @NonNull
    @Override
    protected String getTrackingScreenName() {
        return "";
    }
}
