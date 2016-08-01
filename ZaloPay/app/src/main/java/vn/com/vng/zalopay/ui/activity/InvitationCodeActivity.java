package vn.com.vng.zalopay.ui.activity;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.InvitationCodeFragment;

/**
 * Created by AnhHieu on 6/27/16.
 */
public class InvitationCodeActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return InvitationCodeFragment.newInstance();
    }
}
