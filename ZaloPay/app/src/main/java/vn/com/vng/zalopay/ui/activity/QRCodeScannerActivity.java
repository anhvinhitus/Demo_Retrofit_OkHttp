package vn.com.vng.zalopay.ui.activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.qrcode.activity.AbsQRScanActivity;
import vn.com.vng.zalopay.ui.presenter.QRCodePresenter;
import vn.com.vng.zalopay.ui.view.IQRScanView;

/**
 * Created by AnhHieu on 4/21/16.
 */
public class QRCodeScannerActivity extends AbsQRScanActivity implements IQRScanView {

    private ProgressDialog mProgressDialog;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Inject
    Navigator navigator;

    @Inject
    QRCodePresenter qrCodePresenter;

    @Inject
    ZPAnalytics zpAnalytics;

    public int getResLayoutId() {
        return R.layout.activity_qr_scaner;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createUserComponent();
        setupActivityComponent();
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        qrCodePresenter.setView(this);
        zpAnalytics.trackEvent(ZPEvents.SCANQR_LAUNCH);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        getSupportActionBar().setTitle(titleId);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodePresenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodePresenter.pause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        zpAnalytics.trackEvent(ZPEvents.SCANQR_NAVIGATEBACK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getAppComponent().monitorTiming().cancelEvent(MonitorEvents.QR_SCANNING);

        qrCodePresenter.destroy();
        hideLoading();
        mProgressDialog = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void handleResult(String result) {
        Timber.tag(TAG).i("result:" + result);

        try {
            vibrate();
        } catch (Exception ex) {
        }

        getAppComponent().monitorTiming().finishEvent(MonitorEvents.QR_SCANNING);
        qrCodePresenter.pay(result);
    }

    protected void setupActivityComponent() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    protected ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onTokenInvalid() {
    }

    @Override
    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.loading));
        }
        mProgressDialog.show();
    }

    @Override
    public void hideLoading() {
        if (mProgressDialog != null)
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
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        hideLoading();
    }

    @Override
    public Context getContext() {
        return this;
    }


    private void createUserComponent() {

        Timber.d(" user component %s", getUserComponent());

        if (getUserComponent() != null)
            return;

        UserConfig userConfig = getAppComponent().userConfig();
        Timber.d(" userConfig %s", userConfig.isSignIn());
        if (userConfig.isSignIn()) {
            userConfig.loadConfig();
            AndroidApplication.instance().createUserComponent(userConfig.getCurrentUser());
        }
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

}

