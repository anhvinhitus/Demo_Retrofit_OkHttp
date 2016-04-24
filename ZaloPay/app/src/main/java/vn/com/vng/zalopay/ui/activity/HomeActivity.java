package vn.com.vng.zalopay.ui.activity;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.HomeFragment;

/**
 * Created by AnhHieu on 3/26/16.
 */
public class HomeActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return HomeFragment.newInstance();
    }
}

