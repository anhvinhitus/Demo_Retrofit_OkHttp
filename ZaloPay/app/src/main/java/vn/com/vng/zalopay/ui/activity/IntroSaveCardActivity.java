package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.IntroSaveCardFragment;

public class IntroSaveCardActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return IntroSaveCardFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
