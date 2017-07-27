package vn.com.vng.zalopay.transfer.ui;

import android.support.annotation.NonNull;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseToolBarActivity;

/**
 * Created by AnhHieu on 8/29/16.
 * *
 */
public class SetAmountActivity extends UserBaseToolBarActivity {
    @Override
    public BaseFragment getFragmentToHost() {
        return SetAmountFragment.newInstance();
    }

    @NonNull
    @Override
    protected String getTrackingScreenName() {
        return "";
    }
}
