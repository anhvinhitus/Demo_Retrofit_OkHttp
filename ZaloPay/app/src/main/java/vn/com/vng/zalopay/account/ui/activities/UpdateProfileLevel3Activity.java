package vn.com.vng.zalopay.account.ui.activities;

import vn.com.vng.zalopay.account.ui.fragment.UpdateProfile3Fragment;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 6/30/16.
 */
public class UpdateProfileLevel3Activity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return UpdateProfile3Fragment.newInstance();
    }
}
