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
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.ChangePinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.view.IChangePinView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.IPassCodeFocusChanged;
import vn.com.vng.zalopay.ui.widget.PassCodeView;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by AnhHieu on 8/25/16.
 */
public class ChangePinFragment extends BaseFragment implements IChangePinView {

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

    @Inject
    IChangePinPresenter presenter;

    TextWatcher passCodeChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mNewPassCodeView.isValid()) {
                mNewPassCodeView.hideError();
            }

        /*    if (isValidPinView(mOldPassCodeView)) {
                mOldPassCodeView.hideError();
                Timber.d("mOldPassCodeView isRequestFocus");
            }*/
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkPinValid();
        }
    };

    private boolean isDifferencePin() {
        String newPin = mNewPassCodeView.getText();
        return !TextUtils.isEmpty(newPin) && !newPin.equals(mOldPassCodeView.getText());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter.setChangePassView(this);
        mNewPassCodeView.addTextChangedListener(passCodeChanged);
        mNewPassCodeView.setPassCodeFocusChanged(new IPassCodeFocusChanged() {
            @Override
            public void onFocusChangedPin(boolean isFocus) {
                if (isFocus) {
                    mScrollView.smoothScrollTo(0, mScrollView.getBottom());
                }
            }
        });

        mOldPassCodeView.addTextChangedListener(passCodeChanged);

        mOldPassCodeView.requestFocusView();

        AndroidUtils.setSpannedMessageToView(mContactView,
                getString(R.string.lbl_note_forget_pin),
                getString(R.string.phone_support), false, false,
                ContextCompat.getColor(getContext(), R.color.colorPrimary), new ClickableSpanNoUnderline() {
                    @Override
                    public void onClick(View widget) {
                        navigator.startDialSupport(getContext());
                    }
                });

        checkPinValid();
    }

    private void checkPinValid() {
        boolean valid = mNewPassCodeView.isValid() && mOldPassCodeView.isValid();
        Timber.d("checkPinValid: valid %s", valid);
        presenter.pinValid(valid);
    }

    @Override
    public void onDestroyView() {
        presenter.destroyChangePassView();
        super.onDestroyView();
    }

    @Override
    public void checkPinValidAndSubmit() {
        if (isDifferencePin()) {
            mNewPassCodeView.hideError();
            presenter.changePin(mOldPassCodeView.getText(), mNewPassCodeView.getText());
        } else {
            mNewPassCodeView.showError(getString(R.string.pin_not_change));
            mNewPassCodeView.requestFocusView();
        }
    }

    @Override
    public void requestFocusOldPin() {
        if (mOldPassCodeView != null) {
            mOldPassCodeView.requestFocusView();
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
        showToast(message);
    }
}

