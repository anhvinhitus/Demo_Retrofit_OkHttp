package vn.com.vng.zalopay.ui.activity;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.ui.fragment.BalanceManagementFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;
import vn.com.zalopay.analytics.ZPScreens;

public class BalanceManagementActivity extends UserBaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return BalanceManagementFragment.newInstance();
    }

    @NonNull
    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.BALANCE;
    }
}
