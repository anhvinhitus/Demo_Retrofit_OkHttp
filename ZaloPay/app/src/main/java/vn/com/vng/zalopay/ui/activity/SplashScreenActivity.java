package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.UserConfig;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 1/29/16.
 */
public class SplashScreenActivity extends BaseActivity {

    private Timer waitTimer;
    private TimerTask timeTask;
    private boolean interstitialCanceled = false;
    private final long TIME_DELAY = 2000;

    @Inject
    UserConfig mUserConfig;

    @Inject
    Navigator navigator;

    @Override
    protected void setupActivityComponent() {
        AndroidApplication.instance().getAppComponent().inject(this);
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    protected int getResLayoutId() {
        return R.layout.activity_splashscreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        waitTimer = new Timer();
        timeTask = new TimerTask() {

            @Override
            public void run() {
                interstitialCanceled = true;
                startLaunchActivity();
            }
        };

        waitTimer.schedule(timeTask, TIME_DELAY);


    }


    @Override
    protected void onPause() {
        super.onPause();
        interstitialCanceled = true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (interstitialCanceled)
            startLaunchActivity();
    }

    @Override
    protected void onDestroy() {

        if (waitTimer != null) {
            waitTimer.cancel();
            waitTimer = null;
        }
        if (timeTask != null) {
            timeTask.cancel();
            timeTask = null;
        }

        super.onDestroy();
    }

    boolean isCallLaunch = false;

    private void startLaunchActivity() {
        if (isCallLaunch) return;
        isCallLaunch = true;

        if (mUserConfig.isClientActivated() || BuildConfig.DEBUG) {
            Timber.d("isClientActivated");
            navigator.startHomeActivity(this);
        } else {
            navigator.startLoginActivity(this);
            Timber.d("startLoginActivity");
        }
        finish();
    }


}