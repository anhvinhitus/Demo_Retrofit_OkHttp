package vn.com.vng.zalopay.account.ui.activities;

import android.os.Bundle;

import vn.com.vng.zalopay.account.ui.fragment.PreProfileFragment;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class PreProfileActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return PreProfileFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
