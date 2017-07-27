package vn.com.vng.zalopay.passport;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import javax.inject.Inject;

import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.analytics.ZPScreens;


public class LoginZaloActivity extends BaseActivity implements ILoginView {

    private SweetAlertDialog mErrorDialog = null;
    private SweetAlertDialog mProgressDialog = null;

    @Override
    protected void setupActivityComponent(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }

    public int getResLayoutId() {
        return R.layout.activity_login_zalo;
    }

    public BaseFragment getFragmentToHost() {
        return null;
    }

    @NonNull
    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.LOGINZALO;
    }

    @Inject
    LoginPresenter mLoginPresenter;

    @Inject
    GlobalEventHandlingService mGlobalEventService;

    private Boolean restarted = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restarted = savedInstanceState != null;
        mLoginPresenter.attachView(this);
        mLoginPresenter.fetchAppResource();
        mLoginPresenter.handleIntent(getIntent());
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mLoginPresenter.handleIntent(intent);
    }

    @OnClick(R.id.layoutLoginZalo)
    public void onClickLogin() {
        mLoginPresenter.loginZalo(this);
    }

    public void onResume() {
        super.onResume();

        if (!restarted) {
            showMessageAtLogin();
        }

        restarted = true;
        mLoginPresenter.resume();
    }

    public void onPause() {
        super.onPause();
        mLoginPresenter.pause();
    }

    public void onDestroy() {
        destroyErrorDialog();
        mLoginPresenter.destroy();
        hideLoading();
        mProgressDialog = null;
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult requestCode %s resultCode %s", requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        mLoginPresenter.onActivityResult(this, requestCode, resultCode, data);
    }

    public void gotoInvitationCode() {
        navigator.startInvitationCodeActivity(getContext());
        finish();
    }

    public void showLoading() {
        Timber.d("showLoading");
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public void showError(String message) {
        Timber.d("showError message %s", message);
        if (mErrorDialog == null) {
            mErrorDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE, R.style.alert_dialog)
                    .setConfirmText(getContext().getString(R.string.accept))
                    .setConfirmClickListener(Dialog::dismiss);
        }
        mErrorDialog.setContentText(message);
        mErrorDialog.show();
    }

    public void destroyErrorDialog() {
        if (mErrorDialog != null && mErrorDialog.isShowing()) {
            mErrorDialog.dismiss();
        }
        mErrorDialog = null;
    }

    public Context getContext() {
        return this;
    }

    public void showMessageAtLogin() {
        GlobalEventHandlingService.Message message = mGlobalEventService.popMessageAtLogin();

        if (message == null) {
            return;
        }

        showCustomDialog(message.content,
                getString(R.string.txt_close),
                message.messageType, null);
    }

    @Override
    public void gotoOnboarding(ZaloProfile zaloProfile, String oauthcode) {
        navigator.startOnboarding(this, zaloProfile, oauthcode);
        finish();
    }

    @Override
    public void gotoHomePage() {
        navigator.startHomeActivity(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mLoginPresenter.sendResultToCallingExternal(this, RESULT_CANCELED);
    }
}
