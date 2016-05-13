package vn.com.vng.zalopay.ui.activity;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.SplashScreenFragment;

/**
 * Created by AnhHieu on 1/29/16.
 */
public class SplashScreenActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return SplashScreenFragment.newInstance();
    }

    @Override
    public void onBackPressed() {
        // empty
    }
}