package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.view.IChangePinView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.IPasscodeChanged;
import vn.com.vng.zalopay.ui.widget.IPasscodeFocusChanged;
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
    PassCodeView passCode;

    @BindView(R.id.oldPassCodeInput)
    PassCodeView mOldPassCodeView;

    @BindView(R.id.tvContact)
    TextView mContactView;

    @BindView(R.id.scrollView)
    ScrollView mScrollView;

    @Inject
    IChangePinPresenter presenter;

    IPasscodeChanged passCodeChanged = new IPasscodeChanged() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (passCode.isRequestFocus() && isValidPinView(passCode)) {
                passCode.hideError();
            }

            if (mOldPassCodeView.isRequestFocus() && isValidPinView(mOldPassCodeView)) {
                mOldPassCodeView.hideError();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkPinValid();
        }
    };

    private boolean isValidPinView(PassCodeView passCode) {
        String pin = passCode.getText();
        return !TextUtils.isEmpty(pin) && pin.length() == passCode.getMaxLength();
    }

    private boolean isDifferencePin() {
        String newPin = passCode.getText();

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
        passCode.setPasscodeChanged(passCodeChanged);
        passCode.setPasscodeFocusChanged(new IPasscodeFocusChanged() {
            @Override
            public void onFocusChangedPin(boolean isFocus) {
                if (isFocus) {
                    mScrollView.smoothScrollTo(0, mScrollView.getBottom());
                }
            }
        });

        mOldPassCodeView.setPasscodeChanged(passCodeChanged);

        passCode.setBackgroundEdittext(R.drawable.bg_pass_code_bottom_style);
        mOldPassCodeView.setBackgroundEdittext(R.drawable.bg_pass_code_bottom_style);

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
        boolean valid = isValidPinView(passCode) && isValidPinView(mOldPassCodeView);
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
            passCode.hideError();
            presenter.changePin(mOldPassCodeView.getText(), passCode.getText());
        } else {
            passCode.showError(getString(R.string.pin_not_change));
            passCode.requestFocusView();
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

