package vn.com.vng.zalopay.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.zing.zalo.zalosdk.oauth.ValidateOAuthCodeCallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.UserConfig;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 1/29/16.
 */
public class SplashScreenActivity extends BaseActivity implements ValidateOAuthCodeCallback {
    private ProgressDialog mProgressDialog;
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
                showLoading("Tài khoản", "Xác minh lại quyền đăng nhập");
                ZaloSDK.Instance.isAuthenticate(SplashScreenActivity.this);
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
        if (interstitialCanceled) {
            showLoading("Tài khoản", "Xác minh lại quyền đăng nhập");
            ZaloSDK.Instance.isAuthenticate(SplashScreenActivity.this);
        }
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
        hideLoading();
        super.onDestroy();
    }

    @Override
    public void onValidateComplete(boolean isValidated, int errorCode, long userId, String oauthCode) {
        Timber.tag(TAG).d("onValidateComplete###############################isValidated:" + isValidated);
        if(isValidated) {
            //Authenticated
            Timber.d("isClientActivated");
            navigator.startHomeActivity(this);
        } else {
            //Not authenticated
            Timber.d("startLoginActivity");
            navigator.startLoginActivity(this);
        }
        finish();
    }

    public void showLoading(final String title, final String messae) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "showDialog..........progress:" + mProgressDialog);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                if (SplashScreenActivity.this.isFinishing()) {
                    return;
                }
                Log.d(TAG, "showDialog..........hehehe");
                mProgressDialog = ProgressDialog.show(SplashScreenActivity.this, title, messae, true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mProgressDialog.dismiss();
                    }
                });
                mProgressDialog.setCanceledOnTouchOutside(false);
            }
        });
    }

    public void hideLoading() {
        Log.d(TAG, "hideDialog..........mProgressDialog:" + mProgressDialog);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "hideDialog..........");
                if (mProgressDialog == null || !mProgressDialog.isShowing())
                    return;
                mProgressDialog.dismiss();
            }
        });
    }

    public boolean isShowLoading() {
        if (mProgressDialog == null)
            return false;
        return mProgressDialog.isShowing();
    }
}