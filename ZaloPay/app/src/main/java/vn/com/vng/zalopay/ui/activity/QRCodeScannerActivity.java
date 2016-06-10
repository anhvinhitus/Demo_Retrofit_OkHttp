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
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.qrcode.activity.AbsQRScanActivity;
import vn.com.vng.zalopay.ui.presenter.QRCodePresenter;
import vn.com.vng.zalopay.ui.view.IQRScanView;

/**
 * Created by AnhHieu on 4/21/16.
 */
public class QRCodeScannerActivity extends AbsQRScanActivity implements IQRScanView {

    private long appId;
    private String zptranstoken;
    private ProgressDialog mProgressDialog;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Inject
    Navigator navigator;

    @Inject
    QRCodePresenter qrCodePresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityComponent();
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        qrCodePresenter.setView(this);
    }

    public int getResLayoutId() {
        return R.layout.activity_qr_scaner;
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
    protected void onDestroy() {
        super.onDestroy();
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

        qrCodePresenter.pay(result);
    }

    protected void setupActivityComponent() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    @Override
    public void showOrderDetail(Order order) {

    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onTokenInvalid() {
        AndroidApplication.instance().sigoutAndCleanData(this);
    }

    /*@Override
    public void swLoading() {
        if(mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), 5);
        }

        if(!mProgressDialog.isShowing()) {
            try {
                mProgressDialog.getProgressHelper().setBarColor(getContext().getResources().getColor(R.color.color_primary));
                mProgressDialog.setTitle("");
                mProgressDialog.setContentText(getContext().getResources().getString(R.string.alert_processing));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e("DIALOG_MANAGER", "There is a showing process dialog!");
        }
    }

    @Override
    public void hideLoading() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.hide();
    }*/

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
}
