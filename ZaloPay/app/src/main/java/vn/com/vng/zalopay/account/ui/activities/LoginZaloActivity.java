package vn.com.vng.zalopay.account.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.home.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.LoginPresenter;
import vn.com.vng.zalopay.ui.view.ILoginView;
import vn.com.vng.zalopay.utils.ToastUtil;

public class LoginZaloActivity extends BaseActivity implements ILoginView, View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    protected ProgressDialog mProgressDialog;
    private View mLayoutLoginZalo;

    @Inject
    LoginPresenter loginPresenter;

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
        AndroidApplication.instance().getAppComponent().inject(this);
        findView();
        loginPresenter.setView(this);
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
        showLoading();
        loginPresenter.loginZalo(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginPresenter.pause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        loginPresenter.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.tag(TAG).d("onActivityResult................" + requestCode + ";" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        loginPresenter.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void gotoMainActivity() {
//        Intent intent = new Intent(this,  ZPHomeActivity.class);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showLoading() {
        Timber.tag(TAG).d("showDialog..........progress:" + mProgressDialog);
        if (isFinishing()) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        Timber.tag(TAG).d("showDialog..........hehehe");
        mProgressDialog = ProgressDialog.show(LoginZaloActivity.this, "", "Loading", true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mProgressDialog.dismiss();
            }
        });
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    public void hideLoading() {
        Timber.tag(TAG).d("hideDialog..........");
        Timber.tag(TAG).d("hideDialog..........mProgressDialog:" + mProgressDialog);
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.dismiss();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        ToastUtil.showToast(this, message);
    }

    @Override
    public Context getContext() {
        return this;
    }

    public boolean isShowLoading() {
        if (mProgressDialog == null)
            return false;
        return mProgressDialog.isShowing();
    }
}
