package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.SweetAlertDialog;


/**
 * Created by AnhHieu on 3/24/16.
 */
public abstract class BaseFragment extends Fragment {

    protected abstract void setupFragmentComponent();

    protected abstract int getResLayoutId();

    public final String TAG = getClass().getSimpleName();

    private Snackbar mSnackBar;
    private SweetAlertDialog mProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getResLayoutId(), container, false);
        ButterKnife.bind(this, view);
        setupFragmentComponent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgressDialog();
        mProgressDialog = null;
        ButterKnife.unbind(this);
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
        if(mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(getContext(), 5);
        }

        if(!mProgressDialog.isShowing()) {
            try {
                mProgressDialog.getProgressHelper().setBarColor(this.getResources().getColor(R.color.color_primary));
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

    public void hideProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.hide();
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
