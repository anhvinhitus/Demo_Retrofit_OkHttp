package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.view.IChangePinView;
import vn.com.vng.zalopay.scanners.ui.FragmentLifecycle;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.IPassCodeFocusChanged;
import vn.com.vng.zalopay.ui.widget.PassCodeView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public class ChangePinFragment extends BaseFragment implements IChangePinView, FragmentLifecycle {

    public static ChangePinFragment newInstance() {

        Bundle args = new Bundle();

        ChangePinFragment fragment = new ChangePinFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_change_pin;
    }

    @BindView(R.id.passcodeInput)
    PassCodeView mNewPassCodeView;

    @BindView(R.id.oldPassCodeInput)
    PassCodeView mOldPassCodeView;

    @BindView(R.id.tvContact)
    TextView mContactView;

    @BindView(R.id.scrollView)
    ScrollView mScrollView;

    @BindView(R.id.btnContinue)
    View mBtnContinueView;

    @Inject
    IChangePinPresenter mPresenter;

    TextWatcher mNewPassCodeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mNewPassCodeView.setError(null);
            mBtnContinueView.setEnabled(isValid());
        }
    };

    private TextWatcher mOldPassCodeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mOldPassCodeView.setError(null);
            mBtnContinueView.setEnabled(isValid());
        }
    };

    private boolean isDifferencePin() {
        String newPin = mNewPassCodeView.getText();
        return !TextUtils.isEmpty(newPin) && !newPin.equals(mOldPassCodeView.getText());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPresenter.setChangePassView(this);

        mOldPassCodeView.addTextChangedListener(mOldPassCodeWatcher);
        mOldPassCodeView.setPassCodeFocusChanged(mOldPassCodeFocusChanged);

        mNewPassCodeView.addTextChangedListener(mNewPassCodeWatcher);
        mNewPassCodeView.setPassCodeFocusChanged(mNewPassCodeFocusChanged);

        AndroidUtils.setSpannedMessageToView(mContactView,
                getString(R.string.lbl_note_forget_pin),
                getString(R.string.phone_support), false, false,
                ContextCompat.getColor(getContext(), R.color.colorPrimary), new ClickableSpanNoUnderline() {
                    @Override
                    public void onClick(View widget) {
                        navigator.startDialSupport(getContext());
                    }
                });

        mBtnContinueView.setEnabled(isValid());
        showKeyboard();
    }

    boolean isPaused = false;

    @Override
    public void onResume() {
        isPaused = false;
        super.onResume();
    }

    @Override
    public void onPause() {
        isPaused = true;
        mOldPassCodeView.clearFocusView();
        mNewPassCodeView.clearFocusView();
        hideKeyboard();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        AndroidUtils.cancelRunOnUIThread(mKeyboardRunnable);
        mOldPassCodeView.setPassCodeFocusChanged(null);
        mNewPassCodeView.setPassCodeFocusChanged(null);

        mPresenter.destroyChangePassView();
        super.onDestroyView();
    }

    @Override
    public void requestFocusOldPin() {
        Timber.d("requestFocusOldPin");
        if (mOldPassCodeView != null) {
            mOldPassCodeView.getEditText().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mOldPassCodeView.requestFocusView();
                    AndroidUtils.showKeyboard(mOldPassCodeView.getEditText());
                }
            }, 250);
        }
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }

    @Override
    public void showError(String message) {
        super.showErrorDialog(message);
    }

    @Override
    public void showError(String message, ZPWOnEventDialogListener listener) {
        super.showErrorDialog(message, listener);
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {

        if (!mOldPassCodeView.isValid()) {
            mOldPassCodeView.setError(getString(R.string.invalid_pin));
            //mOldPassCodeView.requestFocusView();
            return;
        }

        if (!mNewPassCodeView.isValid()) {
            mNewPassCodeView.setError(getString(R.string.invalid_pin));
            //  mNewPassCodeView.requestFocusView();
            return;
        }

        if (!isDifferencePin()) {
            mNewPassCodeView.setError(getString(R.string.pin_not_change));
            return;
        }

        if (mOldPassCodeView.isFocused()) {
            hideKeyboard(mOldPassCodeView.getEditText());
        }
        if (mNewPassCodeView.isFocused()) {
            hideKeyboard(mNewPassCodeView.getEditText());
        }

        mOldPassCodeView.clearFocusView();
        mNewPassCodeView.clearFocusView();

        mOldPassCodeView.setError(null);
        mNewPassCodeView.setError(null);

        ZPAnalytics.trackEvent(ZPEvents.OTP_CHANGEPASSWORD_REQUEST);
        mPresenter.changePin(mOldPassCodeView.getText(), mNewPassCodeView.getText());

    }

    public boolean isValid() {
        return mOldPassCodeView.isValid() && mNewPassCodeView.isValid();
    }

    private IPassCodeFocusChanged mOldPassCodeFocusChanged = new IPassCodeFocusChanged() {
        @Override
        public void onFocusChangedPin(boolean isFocus) {
            if (isPaused) {
                return;
            }

            Timber.d("old password view focus [%s]", isFocus);
            if (mBtnContinueView != null) {
                mBtnContinueView.setEnabled(isValid());
            }

            mOldPassCodeView.setError(mOldPassCodeView.isValid() || isFocus ? null : getString(R.string.invalid_pin));
        }
    };

    private IPassCodeFocusChanged mNewPassCodeFocusChanged = new IPassCodeFocusChanged() {
        @Override
        public void onFocusChangedPin(boolean isFocus) {
            if (isPaused) {
                return;
            }

            Timber.d("new password view focus [%s]", isFocus);

            if (isFocus) {
                mScrollView.smoothScrollTo(0, mScrollView.getBottom());
            }

            if (mBtnContinueView != null) {
                mBtnContinueView.setEnabled(isValid());
            }

            mNewPassCodeView.setError(mNewPassCodeView.isValid() || isFocus ? null : getString(R.string.invalid_pin));
        }
    };

    @Override
    public void onStartFragment() {

    }

    @Override
    public void onStopFragment() {

    }

    public void showKeyboard() {
        Timber.d("showKeyboard");
        AndroidUtils.runOnUIThread(mKeyboardRunnable, 250);
    }

    private Runnable mKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            if (mOldPassCodeView != null) {
                mOldPassCodeView.requestFocusView();
                AndroidUtils.showKeyboard(mOldPassCodeView.getEditText());
            }

        }
    };
}

