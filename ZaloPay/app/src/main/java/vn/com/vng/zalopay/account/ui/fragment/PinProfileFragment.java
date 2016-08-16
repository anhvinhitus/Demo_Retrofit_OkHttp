package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.PinProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.ui.widget.ClearableEditText;
import vn.com.vng.zalopay.ui.widget.IPassCodeMaxLength;
import vn.com.vng.zalopay.ui.widget.IPasscodeChanged;
import vn.com.vng.zalopay.ui.widget.IPasscodeFocusChanged;
import vn.com.vng.zalopay.ui.widget.InputZaloPayNameListener;
import vn.com.vng.zalopay.ui.widget.InputZaloPayNameView;
import vn.com.vng.zalopay.ui.widget.PassCodeView;
import vn.com.vng.zalopay.utils.ValidateUtil;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

import static android.text.Html.fromHtml;

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

    @BindView(R.id.inputZaloPayName)
    InputZaloPayNameView inputZaloPayName;

    @OnTextChanged(R.id.edtPhone)
    public void onTextChangedPhone() {
        hidePhoneError();
        checkShowHideBtnContinue();
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
        return !TextUtils.isEmpty(phone) && ValidateUtil.isMobileNumber(phone);
    }

    @OnClick(R.id.tvShowPass)
    public void onClickShowPass() {
        boolean isChecked = chkShowPass.isChecked();
        chkShowPass.setChecked(!isChecked);
    }

    @Nullable
    @OnClick(R.id.btnContinue)
    public void onClickBtnContinute() {
        onClickContinue();
    }

    @OnClick(R.id.tvCancel)
    public void onClickCancel() {
        getActivity().finish();
    }

    private boolean isValidPin() {
        String pin = passCode.getText();
        return !TextUtils.isEmpty(pin) && pin.length() == passCode.getMaxLength();
    }

    IPasscodeFocusChanged passcodeFocusChanged = new IPasscodeFocusChanged() {
        @Override
        public void onFocusChangedPin(boolean isFocus) {
            if (isFocus) {
                return;
            }
            if (!isValidPin()) {
                passCode.showError(getString(R.string.invalid_pin));
            } else {
                passCode.hideError();
            }
        }
    };

    IPassCodeMaxLength passCodeMaxLength = new IPassCodeMaxLength() {
        @Override
        public void hasMaxLength() {
            if (edtPhone != null) {
                edtPhone.requestFocus();
            }
        }
    };

    IPasscodeChanged passcodeChanged = new IPasscodeChanged() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isValidPin()) {
                passCode.hideError();
            }
            checkShowHideBtnContinue();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private InputZaloPayNameListener mInputZaloPayNameListener = new InputZaloPayNameListener() {
        @Override
        public void onTextChanged(CharSequence s) {
            checkShowHideBtnContinue();
        }

        @Override
        public void onFocusChange(boolean isFocus) {
            if (isFocus) {
                return;
            }
            checkShowHideBtnContinue();
        }
    };

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

    private void checkShowHideBtnContinue() {
        if (mListener != null) {
            mListener.onChangeBtnConfirmState(isShowBtnContinue());
        }
    }

    private boolean isShowBtnContinue() {
        if (!isValidPin()) {
            //passCode.showError(getString(R.string.invalid_pin));
            return false;
        } else {
            passCode.hideError();
        }

        if (!isValidPhone()) {
            //showPhoneError();
            return false;
        } else {
            hidePhoneError();
        }
        Timber.d("onClickContinue inputZaloPayName.getCurrentState() [%s]", inputZaloPayName.getCurrentState());
        if (!validZaloPayName()) {
            return false;
        }
        return true;
    }

    @Override
    public void onClickContinue() {
        if (TextUtils.isEmpty(inputZaloPayName.getText())) {
            presenter.updateProfile(passCode.getText(), edtPhone.getString(), null);
        } else {
            showConfirmUpdateZaloPayName();
        }
    }

    private boolean validZaloPayName() {
        if (TextUtils.isEmpty(inputZaloPayName.getText())) {
            return true;
        } else if (inputZaloPayName.getCurrentState() != InputZaloPayNameView.ZPNameStateEnum.INVALID &&
                inputZaloPayName.validZPName()) {
            return true;
        } else {
            return false;
        }
    }

    private void showConfirmUpdateZaloPayName() {
        showDialog(getString(R.string.notification),
                getString(R.string.warning_update_zalopay_name),
                getString(R.string.cancel),
                getString(R.string.accept),
                new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        presenter.updateProfile(passCode.getText(), edtPhone.getString(), inputZaloPayName.getText());
                        sweetAlertDialog.dismiss();
                    }
                },
                SweetAlertDialog.NORMAL_TYPE);
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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        tvCancel.setPaintFlags(tvCancel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvCancel.setText(fromHtml(getString(R.string.txt_cancel)));

        chkShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passCode.showPasscode();
                } else {
                    passCode.hidePasscode();
                }
            }
        });

        passCode.requestFocus();
        passCode.setPassCodeMaxLength(passCodeMaxLength);
        passCode.setPasscodeChanged(passcodeChanged);
        passCode.setPasscodeFocusChanged(passcodeFocusChanged);

        inputZaloPayName.setOnClickBtnCheck(mOnClickCheckZaloPayName);
        inputZaloPayName.setOntextChangeListener(mInputZaloPayNameListener);

        edtPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }
                if (!isValidPhone()) {
                    showPhoneError();
                } else {
                    hidePhoneError();
                }
            }
        });
    }

    private View.OnClickListener mOnClickCheckZaloPayName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            presenter.checkZaloPayName(inputZaloPayName.getText());
        }
    };

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
    public void updateProfileSuccess(String phone) {
        if (mListener != null) {
            mListener.onUpdatePinSuccess(phone);
        }
    }

    @Override
    public void onCheckSuccess() {
        if (inputZaloPayName == null) {
            return;
        }
        inputZaloPayName.showCheckSuccess();
    }

    @Override
    public void onCheckFail() {
        inputZaloPayName.showCheckFail();
    }

    @Override
    public void hideInputZaloPayName() {
        inputZaloPayName.setVisibility(View.GONE);
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
        void onUpdatePinSuccess(String phone);

        void onUpdatePinFail();

        void onChangeBtnConfirmState(boolean isEnable);
    }
}
