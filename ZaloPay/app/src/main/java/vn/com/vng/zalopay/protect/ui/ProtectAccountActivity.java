package vn.com.vng.zalopay.protect.ui;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by hieuvm on 12/24/16.
 */

public class ProtectAccountActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return ProtectAccountFragment.newInstance();
    }
}
