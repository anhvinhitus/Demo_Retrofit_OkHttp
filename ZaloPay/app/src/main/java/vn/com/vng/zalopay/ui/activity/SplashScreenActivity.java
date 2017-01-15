package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import timber.log.Timber;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.SplashScreenFragment;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 1/29/16.
 * *
 */
public class SplashScreenActivity extends BaseActivity {

    @Override
    public BaseFragment getFragmentToHost() {
        return SplashScreenFragment.newInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ZPAnalytics.trackEvent(ZPEvents.APP_LAUNCH);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate: [%s]", isTaskRoot());
    }

    @Override
    public void onBackPressed() {
        // empty
    }
}