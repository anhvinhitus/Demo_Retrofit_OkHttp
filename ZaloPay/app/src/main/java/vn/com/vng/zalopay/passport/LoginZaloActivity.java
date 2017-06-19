package vn.com.vng.zalopay.passport;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import butterknife.OnClick;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.ui.activity.BaseActivity;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

import javax.inject.Inject;


public class LoginZaloActivity extends BaseActivity implements ILoginView {

    private SweetAlertDialog mErrorDialog = null;
    private SweetAlertDialog mProgressDialog = null;

    public void setupActivityComponent() {
        getAppComponent().inject(this);
    }

    public int getResLayoutId() {
        return R.layout.activity_login_zalo;
    }

    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Inject
    LoginPresenter loginPresenter;

    @Inject
    GlobalEventHandlingService mGlobalEventService;

    private Boolean restarted = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Timber.d("onCreate taskid %s", getTaskId());
        restarted = savedInstanceState != null;
        loginPresenter.attachView(this);
        loginPresenter.fetchAppResource();
        handleIntent(getIntent());
    }

    public void handleIntent(Intent data) {
        if (data == null) {
            return;
        }

        boolean callFrom = data.getBooleanExtra("callingExternal", false);
        Timber.d("handleIntent: %s", callFrom);
        loginPresenter.setCallingExternal(callFrom);
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        handleIntent(intent);
    }

    @OnClick(R.id.layoutLoginZalo)
    public void onClickLogin() {
        if (AppVersionUtils.showDialogForceUpgradeApp(this)) {
            return;
        }
        loginPresenter.loginZalo(this);
        ZPAnalytics.trackEvent(ZPEvents.TAP_LOGIN);
    }

    public void onResume() {
        super.onResume();

        if (!restarted) {
            showMessageAtLogin();
        }

        restarted = true;
        loginPresenter.resume();
    }

    public void onPause() {
        super.onPause();
        loginPresenter.pause();
    }

    public void onDestroy() {
        destroyErrorDialog();
        loginPresenter.destroy();
        hideLoading();
        mProgressDialog = null;
        super.onDestroy();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult requestCode %s resultCode %s", requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        loginPresenter.onActivityResult(this, requestCode, resultCode, data);
    }

    public void gotoMainActivity() {
        navigator.startHomeActivity(this);
        finish();
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

    public void showNetworkError() {
        DialogHelper.showNetworkErrorDialog(getActivity(), null);
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

    public void gotoOnboarding(ZaloProfile zaloProfile, String oauthcode) {
        navigator.startOnboarding(this, zaloProfile, oauthcode);
        finish();
    }
}
