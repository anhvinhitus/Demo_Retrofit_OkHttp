package vn.com.vng.zalopay.account.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import javax.inject.Inject;

import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.LoginPresenter;
import vn.com.vng.zalopay.ui.view.ILoginView;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;


public class LoginZaloActivity extends BaseActivity implements ILoginView {


    @Override
    protected void setupActivityComponent() {
        getAppComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.activity_login_zalo;
    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    protected ProgressDialog mProgressDialog;

    @Inject
    LoginPresenter loginPresenter;

    @Inject
    Navigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginPresenter.setView(this);

        String message = getIntent().getStringExtra(Constants.ARG_MESSAGE);
        if (TextUtils.isEmpty(message)) {
            return;
        }


        SweetAlertDialog alertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
        alertDialog.setContentText(message);
        alertDialog.setConfirmText("Đồng ý");
        alertDialog.show();
    }

    @OnClick(R.id.layoutLoginZalo)
    public void onClickLogin(View v) {
        loginPresenter.loginZalo(this);
        zpAnalytics.logEvent(ZPEvents.TAP_LOGIN);
    }

   /* @Override
    public void onResume() {
        super.onResume();
        loginPresenter.pause();
    }

    @Override
    public void onPause() {
        super.onPause();
        loginPresenter.pause();
    }*/

    @Override
    public void onDestroy() {
        loginPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //   super.onBackPressed();
        finish();
        System.exit(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult requestCode %s resultCode %s", requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        loginPresenter.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void gotoMainActivity() {
        navigator.startHomeActivity(this, true);
        finish();
    }

    @Override
    public void gotoUpdateProfileLevel2() {
        navigator.startUpdateProfileLevel2Activity(this, true);
        finish();
    }

    @Override
    public void showLoading() {
        Timber.d("showLoading");
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.login), getString(R.string.loading));
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        mProgressDialog.show();
    }

    public void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showRetry() {
    }

    @Override
    public void hideRetry() {
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
