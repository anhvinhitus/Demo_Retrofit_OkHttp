package vn.com.vng.zalopay.fingerprint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by hieuvm on 12/24/16.
 */

public class ProtectAccountFragment extends BaseFragment implements IProtectAccountView {
    public static ProtectAccountFragment newInstance() {

        Bundle args = new Bundle();

        ProtectAccountFragment fragment = new ProtectAccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_protect_account;
    }

    @BindView(R.id.swcTouchId)
    SwitchCompat mSwcTouchIdView;

    @BindView(R.id.swcProtectAccount)
    SwitchCompat mSwcProtectAccountView;

    @BindView(R.id.vgTouchId)
    View mVgTouchIdView;

    @BindView(R.id.lblTouchId)
    View mTvTouchIdView;

    @BindView(R.id.btnFingerprint)
    View mBtnFingerprint;

    @Inject
    ProtectAccountPresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mPresenter.onViewCreated();
        mBtnFingerprint.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @OnClick(R.id.vgChangePass)
    public void onClickChangePassword(View v) {
        navigator.startChangePinActivity(getActivity());
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void setCheckedFingerprint(boolean var) {
        mSwcTouchIdView.setChecked(var);
    }

    public void showFingerAuthentication() {
        Timber.d("show finger authentication");

        AuthenticationDialog fragment = AuthenticationDialog.newInstance();
        fragment.setStage(Stage.PASSWORD_SETTING);
        fragment.setAuthenticationCallback(new AuthenticationCallback() {
            @Override
            public void onAuthenticated() {
                setCheckedFingerprint(true);
                setCheckedProtectAccount(true);
                mPresenter.setUseProtectAccount(true);
            }

            @Override
            public void onAuthenticationFailure() {

            }
        });

        fragment.show(getActivity().getFragmentManager(), AuthenticationDialog.TAG);
    }

    @Override
    public void setCheckedTouchId(boolean checked) {
        if (mSwcTouchIdView != null) {
            mSwcTouchIdView.setChecked(checked);
        }
    }

    @Override
    public void setCheckedProtectAccount(boolean checked) {
        if (mSwcProtectAccountView != null) {
            mSwcProtectAccountView.setChecked(checked);
        }
    }

    @Override
    public void hideFingerprintLayout() {
        mVgTouchIdView.setVisibility(View.GONE);
        mTvTouchIdView.setVisibility(View.GONE);
    }

    @OnTouch(R.id.swcTouchId)
    public boolean onTouchTouchId(View view, MotionEvent event) {
        Timber.d("onTouchTouchId");
        mPresenter.useFingerprintToAuthenticate(!mSwcTouchIdView.isChecked());
        return false;
    }

    @OnTouch(R.id.swcProtectAccount)
    public boolean onTouchProtectAccount(View v, MotionEvent event) {
        Timber.d("onTouchProtectAccount");
        mPresenter.userProtectAccount(!mSwcProtectAccountView.isChecked());
        return false;
    }

    @OnClick(R.id.btnFingerprint)
    public void onClickFingerprint() {
        AuthenticationDialog fragment = AuthenticationDialog.newInstance();
        fragment.setStage(Stage.FINGERPRINT_DECRYPT);
        fragment.setAuthenticationCallback(null);
        fragment.show(getActivity().getFragmentManager(), AuthenticationDialog.TAG);
    }

}
