package vn.com.vng.zalopay.account.ui.activities;

import vn.com.vng.zalopay.account.ui.fragment.UpdateProfile3Fragment;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 6/30/16.
 */
public class UpdateProfileLevel3Activity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        boolean focusIdentity = getIntent().getBooleanExtra("focusIdentity", false);
        return UpdateProfile3Fragment.newInstance(focusIdentity);
    }
}
