package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.widget.IPasscodeChanged;
import vn.com.vng.zalopay.ui.widget.IPasscodeFocusChanged;
import vn.com.vng.zalopay.ui.widget.PassCodeView;
import vn.com.zalopay.wallet.view.animation.ActivityAnimator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PinProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PinProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PinProfileFragment extends AbsProfileFragment {
    private OnFragmentInteractionListener mListener;

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
        navigator.startHomeActivity(getContext(), true);
        ActivityAnimator anim = new ActivityAnimator();
        anim.fadeAnimation(getActivity());
    }

    private IPasscodeChanged passcodeChanged = new IPasscodeChanged() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextChangePin(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private IPasscodeChanged confirmPasscodeChanged = new IPasscodeChanged() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextChangePinCompare(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private IPasscodeFocusChanged passcodeFocusChanged = new IPasscodeFocusChanged() {
        @Override
        public void onFocusChangedPin(boolean isFocus) {
            onFocusChangedPin(isFocus);
        }
    };

    private IPasscodeFocusChanged confirmPasscodeFocusChanged = new IPasscodeFocusChanged() {
        @Override
        public void onFocusChangedPin(boolean isFocus) {
            onFocusChangedPinCompare(isFocus);
        }
    };

    public void onFocusChangedPin(boolean isFocus) {
        if (!isFocus) {
            if (isValidPin()) {
                passCode.hideError();
            } else {
                passCode.showError(getString(R.string.invalid_pin));
            }
        }
    }

    public void onFocusChangedPinCompare(boolean isFocus) {
        if (!isFocus) {
            if (isValidPinCompare()) {
                passCodeConfirm.hideError();
            } else {
                passCode.showError(getString(R.string.invalid_pin));
            }
        }
    }

    public void onTextChangePin(CharSequence text) {
        if (isValidPin()) {
            passCode.hideError();
            if (isValidPinCompare()) {
                passCodeConfirm.hideError();
            }
        }
    }

    public void onTextChangePinCompare(CharSequence text) {
        if (isValidPinCompare()) {
            passCode.hideError();
            passCodeConfirm.hideError();
        } else {
            passCodeConfirm.showError(getString(R.string.invalid_pin_compare));
        }
    }

    private boolean isValidPinCompare() {
        String pin = passCode.getText().toString();
        String pinCompare = passCodeConfirm.getText().toString();
        if (TextUtils.isEmpty(pinCompare) || !pinCompare.equals(pin)) {
            return false;
        }
        return true;
    }

    private boolean isValidPin() {
        String pin = passCode.getText().toString();
        if (TextUtils.isEmpty(pin)) {
            return false;
        }
        return true;
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
    // TODO: Rename and change types and number of parameters
    public static PinProfileFragment newInstance() {
        PinProfileFragment fragment = new PinProfileFragment();
        return fragment;
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

        navigator.startHomeActivity(getContext(), true);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
