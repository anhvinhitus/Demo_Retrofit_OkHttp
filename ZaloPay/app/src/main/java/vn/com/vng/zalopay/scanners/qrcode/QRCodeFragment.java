package vn.com.vng.zalopay.scanners.qrcode;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.qrcode.fragment.AbsQrScanFragment;
import vn.com.vng.zalopay.ui.presenter.QRCodePresenter;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 6/7/16.
 */
public class QRCodeFragment extends AbsQrScanFragment implements IQRScanView {


    public static QRCodeFragment newInstance() {

        Bundle args = new Bundle();

        QRCodeFragment fragment = new QRCodeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }


    @Inject
    QRCodePresenter qrCodePresenter;

    private SweetAlertDialog mProgressDialog;

    @Override
    public int getResLayoutId() {
        return R.layout.fragment_qr_code;
    }

    @Override
    protected void handleResult(String result) {
        try {
            vibrate();
        } catch (Exception ex) {
        }

        getAppComponent().monitorTiming().finishEvent(MonitorEvents.QR_SCANNING);
        qrCodePresenter.pay(result);
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    public ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    @Override
    public void onTokenInvalid() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setupFragmentComponent();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        qrCodePresenter.setView(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        qrCodePresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    @Override
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
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        hideLoading();
    }

    @Override
    public void resumeScanner() {
        start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            hideLoading();
            start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    private boolean mIsVisibleToUser = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Timber.d("isVisibleToUser %s", isVisibleToUser);
        if (isVisibleToUser) {
            start();
        } else {
            pause();
        }
        mIsVisibleToUser = isVisibleToUser;
    }

    public boolean isVisibleToUser() {
        return mIsVisibleToUser;
    }


}
