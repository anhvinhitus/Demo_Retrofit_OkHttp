package vn.com.vng.zalopay.account.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.WindowManager;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.PreProfileFragment;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class PreProfileActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
//        updateStatusBar();
    }

    private void updateStatusBar() {
        // clear FLAG_TRANSLUCENT_STATUS flag:
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getColor(R.color.color_primary));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.color_primary));
        }
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return PreProfileFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
