package vn.com.vng.zalopay.account.ui.activities;

import vn.com.vng.zalopay.account.ui.fragment.EditAccountNameFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

/**
 * Created by AnhHieu on 8/12/16.
 *
 */
public class EditAccountNameActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return EditAccountNameFragment.newInstance();
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.ME_PROFILE_ZPID;
    }
}
