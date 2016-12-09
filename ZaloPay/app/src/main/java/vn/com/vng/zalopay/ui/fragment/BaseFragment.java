package vn.com.vng.zalopay.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;


/**
 * Created by AnhHieu on 3/24/16.
 */
public abstract class BaseFragment extends Fragment {
    protected abstract void setupFragmentComponent();

    protected abstract int getResLayoutId();

    public final String TAG = getClass().getSimpleName();

    private Snackbar mSnackBar;
    private SweetAlertDialog mProgressDialog;
    private Unbinder unbinder;

    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();
    protected final UserConfig userConfig = AndroidApplication.instance().getAppComponent().userConfig();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getResLayoutId(), container, false);
        unbinder = ButterKnife.bind(this, view);
        setupFragmentComponent();
        return view;
    }

    @Override
    public void onDestroyView() {
        hideKeyboard();
        super.onDestroyView();
        hideProgressDialog();
        mProgressDialog = null;
        unbinder.unbind();
    }

    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public void showSnackbar(int message, View.OnClickListener listener) {
        hideSnackbar();
        mSnackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        if (listener != null) mSnackBar.setAction(R.string.retry, listener);
        mSnackBar.show();
    }

    public void showNetworkError() {
        showSnackbar(R.string.exception_no_connection, null);
    }

    public void hideSnackbar() {
        if (mSnackBar != null) mSnackBar.dismiss();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(),
                    SweetAlertDialog.PROGRESS_TYPE, R.style.alert_dialog_transparent);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.dismiss();
    }

    public void showNetworkErrorDialog() {
        showNetworkErrorDialog(null);
    }

    public void showNetworkErrorDialog(ZPWOnSweetDialogListener listener) {
        DialogManager.showDialog(getActivity(),
                getString(R.string.txt_warning),
                getString(R.string.exception_no_connection_try_again),
                R.drawable.ic_no_internet,
                listener,
                getString(R.string.txt_close));
    }

    public void showWarning(String message, ZPWOnEventDialogListener cancelListener) {
        showWarningDialog(message, getString(R.string.txt_close), cancelListener);
    }

    public void showErrorDialog(String message) {
        DialogManager.showSweetDialogCustom(getActivity(),
                message,
                getString(R.string.txt_close),
                SweetAlertDialog.ERROR_TYPE,
                null);
    }

    public void showErrorDialog(String message,
                                String cancelText,
                                final ZPWOnEventDialogListener cancelListener) {
        DialogManager.showSweetDialogCustom(getActivity(),
                message,
                cancelText,
                SweetAlertDialog.ERROR_TYPE,
                cancelListener);
    }

    public void showWarningDialog(String message,
                                  String cancelBtnText,
                                  final ZPWOnEventDialogListener cancelListener) {
        DialogManager.showSweetDialogCustom(getActivity(),
                message,
                cancelBtnText,
                SweetAlertDialog.WARNING_TYPE,
                cancelListener);
    }

    public void showRetryDialog(String retryMessage,
                                final ZPWOnEventConfirmDialogListener retryListener) {
        DialogManager.showSweetDialogRetry(getActivity(), retryMessage, retryListener);
    }

    public void showConfirmDialog(String pMessage,
                                  String pOKButton,
                                  String pCancelButton,
                                  final ZPWOnEventConfirmDialogListener callback) {
        DialogManager.showSweetDialogConfirm(getActivity(),
                pMessage,
                pOKButton,
                pCancelButton,
                callback);
    }

    public void showSuccessDialog(String message, ZPWOnEventDialogListener listener) {
        DialogManager.showSweetDialogCustom(getActivity(), message, getString(R.string.txt_close),
                DialogManager.NORMAL_TYPE, listener);
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    public ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    public void showToast(String message) {
        ToastUtil.showToast(getActivity(), message);
    }

    public void showToast(int message) {
        ToastUtil.showToast(getActivity(), message);
    }

    public boolean checkAndRequestPermission(String permission, int requestCode) {
        boolean hasPermission = true;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                requestPermissions(new String[]{permission}, requestCode);
            }
        }
        return hasPermission;
    }

    protected boolean isPermissionGranted(String permissions) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(getContext(), permissions) == PackageManager.PERMISSION_GRANTED;
    }

    public void checkAndRequestReadSMSPermission() {
        checkAndRequestPermission(Manifest.permission.READ_SMS, Constants.Permission.REQUEST_READ_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.Permission.REQUEST_READ_SMS: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (BuildConfig.DEBUG) {
                        showToast("Read sms permission granted");
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        showToast("Read sms permission didn't grante");
                    }
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void hideKeyboard() {
        if (getView() == null) {
            return;
        }
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}
