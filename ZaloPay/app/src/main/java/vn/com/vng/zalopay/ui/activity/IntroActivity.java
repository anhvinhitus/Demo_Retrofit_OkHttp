package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.IntroFragment;

public class IntroActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return IntroFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
