package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.RecoveryPinPresenter;
import vn.com.vng.zalopay.account.ui.view.IRecoveryPinView;
import vn.com.vng.zalopay.ui.widget.ClickableSpanNoUnderline;
import vn.com.vng.zalopay.ui.widget.IPasscodeChanged;
import vn.com.vng.zalopay.ui.widget.PassCodeView;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChangePinFragment.OnPinProfileFragmentListener} interface
 * to handle interaction events.
 * Use the {@link ChangePinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangePinFragment extends AbsProfileFragment implements IRecoveryPinView {
    private OnPinProfileFragmentListener mListener;

    @Inject
    RecoveryPinPresenter presenter;

    @BindView(R.id.passcodeInput)
    PassCodeView passCode;

    @BindView(R.id.oldPassCodeInput)
    PassCodeView mOldPassCodeView;

    @BindView(R.id.tvContact)
    TextView mContactView;

    IPasscodeChanged passCodeChanged = new IPasscodeChanged() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isValidPinView(passCode)) {
                passCode.hideError();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    IPasscodeChanged oldPassCodeChanged = new IPasscodeChanged() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isValidPinView(mOldPassCodeView)) {
                mOldPassCodeView.hideError();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private boolean isValidPinView(PassCodeView passCode) {
        String pin = passCode.getText();
        return !TextUtils.isEmpty(pin) && pin.length() == passCode.getMaxLength();
    }

    private boolean isDifferencePin() {
        String newPin = passCode.getText();
        if (TextUtils.isEmpty(newPin)) {
            return false;
        }
        return !newPin.equals(mOldPassCodeView.getText());
    }

    public ChangePinFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PinProfileFragment.
     */
    public static ChangePinFragment newInstance() {
        return new ChangePinFragment();
    }

    @Override
    public void onClickContinue() {

        if (!isValidPinView(mOldPassCodeView)) {
            mOldPassCodeView.showError(getString(R.string.invalid_pin));
            mOldPassCodeView.requestFocusView();
            return;
        } else {
            mOldPassCodeView.hideError();
        }

        if (!isValidPinView(passCode)) {
            passCode.showError(getString(R.string.invalid_pin));
            passCode.requestFocusView();
            return;
        } else {
            passCode.hideError();
        }

        if (!isDifferencePin()) {
            passCode.showError(getString(R.string.pin_not_change));
            passCode.requestFocusView();
            return;
        } else {
            passCode.hideError();
        }

        presenter.changePin(passCode.getText(), mOldPassCodeView.getText());

    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_change_pin;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        passCode.setPasscodeChanged(passCodeChanged);
        mOldPassCodeView.setPasscodeChanged(oldPassCodeChanged);

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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPinProfileFragmentListener) {
            mListener = (OnPinProfileFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void onRecoveryPinSuccess() {
        if (mListener != null) {
            mListener.onUpdatePinSuccess();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPinProfileFragmentListener {
        void onUpdatePinSuccess();

        void onUpdatePinFail();
    }
}
