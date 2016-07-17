package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.InvitationCodeFragment;

/**
 * Created by AnhHieu on 6/27/16.
 */
public class InvitationCodeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return InvitationCodeFragment.newInstance();
    }
}
