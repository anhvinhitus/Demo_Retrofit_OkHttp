package vn.com.vng.zalopay.ui.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.utils.ToastUtil;
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

    protected final ZPAnalytics zpAnalytics = AndroidApplication.instance().getAppComponent().zpAnalytics();

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
        super.onDestroyView();
        hideProgressDialog();
        mProgressDialog = null;
        unbinder.unbind();
    }

    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        Glide.with(getActivity()).onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        Glide.with(getActivity()).onStart();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.with(getActivity()).onLowMemory();
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
            mProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
            mProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            mProgressDialog.setContentText("Loading");
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    public void showErrorDialog(String message, String cancelText, final SweetAlertDialog.OnSweetClickListener cancelListener) {
        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                .setContentText(message)
                .setConfirmText(cancelText)
                .setConfirmClickListener(cancelListener)
                .show();
    }

    public void showRetryDialog(String retryMessage, String cancelBtnText, final SweetAlertDialog.OnSweetClickListener cancelListener, String retryBtnText, final SweetAlertDialog.OnSweetClickListener retrylListener) {
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setContentText(retryMessage)
                .setCancelText(cancelBtnText)
                .setCancelClickListener(cancelListener)
                .setConfirmText(retryBtnText)
                .setConfirmClickListener(retrylListener)
                .show();
    }


    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
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

    public void hideKeyboard() {
        if (getView() == null) {
            return;
        }
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}
