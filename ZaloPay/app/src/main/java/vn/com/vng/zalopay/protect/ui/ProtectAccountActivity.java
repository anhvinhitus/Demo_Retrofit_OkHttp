package vn.com.vng.zalopay.protect.ui;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

/**
 * Created by hieuvm on 12/24/16.
 */

public class ProtectAccountActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ProtectAccountFragment.newInstance();
    }
}
