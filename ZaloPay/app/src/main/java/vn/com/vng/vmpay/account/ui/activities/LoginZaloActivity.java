package vn.com.vng.vmpay.account.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import timber.log.Timber;
import vn.com.vng.vmpay.account.network.listener.LoginListener;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.activity.ZPHomeActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.ToastUtil;

public class LoginZaloActivity extends BaseActivity implements View.OnClickListener, LoginListener.ILoginZaloListener {
    private final String TAG = this.getClass().getSimpleName();
    protected ProgressDialog mProgressDialog;
    private View mLayoutLoginZalo;
    private LoginListener mLoginListener;

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_login_zalo;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findView();
        mLoginListener = new LoginListener(this);
    }

    private void findView() {
        mLayoutLoginZalo = findViewById(R.id.layoutLoginZalo);
        mLayoutLoginZalo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int itemId = v.getId();
        if (itemId == R.id.layoutLoginZalo) {
            startLoginZalo();
        }
    }

    private void startLoginZalo() {
        Timber.tag(TAG).d("startLoginZalo................");
        showLoading("", getString(R.string.login_with_zalo));
        ZaloSDK.Instance.authenticate(this, mLoginListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.tag(TAG).d("onActivityResult................" + requestCode + ";" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        try {
            ZaloSDK.Instance.onActivityResult(this, requestCode, resultCode, data);
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onAuthenError(int errorCode, String message) {
        Timber.tag(TAG).d("onAuthenError................errorCode:" + errorCode);
        Timber.tag(TAG).d("onAuthenError................message:" + message);
        hideLoading();
        ToastUtil.showToast(this, message);
    }

    @Override
    public void onGetOAuthComplete(long uId, String oauthCode, String channel) {
        Timber.tag(TAG).d("onGetOAuthComplete................oauthCode:" + oauthCode);
        hideLoading();
        gotoMainActivity();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, ZPHomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void showLoading(final String title, final String messae) {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                hideLoading();
//            }
//        }, SCAN_PERIOD);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "showDialog..........progress:" + mProgressDialog);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                if (LoginZaloActivity.this.isFinishing()) {
                    return;
                }
                Log.d(TAG, "showDialog..........hehehe");
                mProgressDialog = ProgressDialog.show(LoginZaloActivity.this, title, messae, true, true, new DialogInterface.OnCancelListener() {
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
