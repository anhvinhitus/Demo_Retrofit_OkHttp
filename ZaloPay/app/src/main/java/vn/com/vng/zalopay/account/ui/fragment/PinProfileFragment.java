package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.ui.widget.ClearableEditText;
import vn.com.vng.zalopay.ui.widget.IPasscodeChanged;
import vn.com.vng.zalopay.ui.widget.IPasscodeFocusChanged;
import vn.com.vng.zalopay.ui.widget.PassCodeView;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.animation.ActivityAnimator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PinProfileFragment.OnPinProfileFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PinProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PinProfileFragment extends AbsProfileFragment implements IPinProfileView {
    private OnPinProfileFragmentListener mListener;

    @Inject
    PinProfilePresenter presenter;

    @BindView(R.id.passcodeInput)
    PassCodeView passCode;

    @BindView(R.id.passcodeConfirm)
    PassCodeView passCodeConfirm;

    @BindView(R.id.checkbox)
    CheckBox chkShowPass;

    @BindView(R.id.tvShowPass)
    TextView tvShowPass;

    @BindView(R.id.tvCancel)
    TextView tvCancel;

    @BindView(R.id.layoutAction)
    View layoutAction;

    @BindView(R.id.textInputPhone)
    TextInputLayout textInputPhone;
    @BindView(R.id.edtPhone)
    ClearableEditText edtPhone;

    @OnTextChanged(R.id.edtPhone)
    public void onTextChangedPhone(CharSequence charSequence) {
        if (isValidPhone()) {
            hidePhoneError();
        } else {
            showPhoneError();
        }
    }

    private void showPhoneError() {
        textInputPhone.setErrorEnabled(true);
        if (TextUtils.isEmpty(edtPhone.getText().toString())) {
            textInputPhone.setError(getString(R.string.invalid_phone_empty));
        } else {
            textInputPhone.setError(getString(R.string.invalid_phone));
        }
    }

    private void hidePhoneError() {
        textInputPhone.setErrorEnabled(false);
        textInputPhone.setError(null);
    }

    public boolean isValidPhone() {
        String phone = edtPhone.getString();
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        return ValidateUtil.isMobileNumber(phone);
    }


    @OnClick(R.id.tvShowPass)
    public void onClickShowPass(View view) {
        boolean isChecked = chkShowPass.isChecked();
        chkShowPass.setChecked(!isChecked);
    }

    @Nullable
    @OnClick(R.id.btnContinue)
    public void onClickBtnContinute(View view) {
        onClickContinue();
    }

    @OnClick(R.id.tvCancel)
    public void onClickCancel(View view) {
        getActivity().finish();
    }

    private boolean isValidPinCompare() {
        String pin = passCode.getText();
        String pinCompare = passCodeConfirm.getText();
        return (!TextUtils.isEmpty(pinCompare) && pinCompare.equals(pin));
    }

    private boolean isValidPin() {
        String pin = passCode.getText();
        return !TextUtils.isEmpty(pin);
    }

    public PinProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PinProfileFragment.
     */
    public static PinProfileFragment newInstance() {
        return new PinProfileFragment();
    }

    @Override
    public void onClickContinue() {
        if (!isValidPin()) {
            passCode.showError(getString(R.string.invalid_pin));
            return;
        } else {
            passCode.hideError();
        }
        if (!isValidPinCompare()) {
            passCodeConfirm.showError(getString(R.string.invalid_pin_compare));
            return;
        } else {
            passCodeConfirm.hideError();
        }

        if (!isValidPhone()) {
            showPhoneError();
            return;
        } else {
            hidePhoneError();
        }
        presenter.updateProfile(passCode.getText(), edtPhone.getString());
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_pin_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        tvCancel.setPaintFlags(tvCancel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvCancel.setText(Html.fromHtml(getString(R.string.txt_cancel)));

        chkShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passCode.showPasscode();
                    passCodeConfirm.showPasscode();
                } else {
                    passCode.hidePasscode();
                    passCodeConfirm.hidePasscode();
                }
            }
        });

//        passCode.setPasscodeChanged(passcodeChanged);
//        passCode.setPasscodeFocusChanged(passcodeFocusChanged);
//        passCodeConfirm.setPasscodeChanged(confirmPasscodeChanged);
//        passCodeConfirm.setPasscodeFocusChanged(confirmPasscodeFocusChanged);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPinProfileFragmentListener) {
            mListener = (OnPinProfileFragmentListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnPinProfileFragmentListener");
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
    public void updateProfileSuccess() {
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
        // TODO: Update argument type and name
        void onUpdatePinSuccess();
        void onUpdatePinFail();
    }
}
