package vn.com.vng.zalopay.account.ui.activities;

import android.os.Bundle;
import android.os.PersistableBundle;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class PinProfileActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return ProfileFragment.newInstance(Constants.PIN_PROFILE_TYPE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
