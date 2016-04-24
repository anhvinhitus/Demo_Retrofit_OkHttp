package vn.com.vng.zalopay.ui.activity;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.LoginFragment;

/**
 * Created by AnhHieu on 3/26/16.
 */
public class LoginActivity extends BaseToolBarActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return LoginFragment.newInstance();
    }
}
