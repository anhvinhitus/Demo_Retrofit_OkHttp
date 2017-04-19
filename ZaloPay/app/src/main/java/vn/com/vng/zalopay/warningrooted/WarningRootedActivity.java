package vn.com.vng.zalopay.warningrooted;

import android.os.Bundle;

import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class WarningRootedActivity extends BaseToolBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return WarningRootedFragment.newInstance();
    }
}
