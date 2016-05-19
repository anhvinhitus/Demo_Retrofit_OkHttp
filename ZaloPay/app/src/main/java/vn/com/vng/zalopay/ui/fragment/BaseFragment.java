package vn.com.vng.zalopay.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.utils.ToastUtil;


/**
 * Created by AnhHieu on 3/24/16.
 */
public abstract class BaseFragment extends Fragment {

    protected abstract void setupFragmentComponent();

    protected abstract int getResLayoutId();

    public final String TAG = getClass().getSimpleName();

    private Snackbar mSnackBar;
    private ProgressDialog mProgressDialog;
    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);
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
            mProgressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.loading));
        }
        mProgressDialog.show();
    }


    public void hideProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
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


}
