package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.ValidateUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreProfileFragment extends AbsProfileFragment {
    private OnFragmentInteractionListener mListener;

    @BindView(R.id.textInputPhone)
    TextInputLayout textInputPhone;

    @BindView(R.id.edtPhone)
    EditText edtPhone;

    @BindView(R.id.tvPhoneNote)
    TextView tvPhoneNote;

    @BindView(R.id.textInputEmail)
    TextInputLayout textInputEmail;

    @BindView(R.id.edtEmail)
    EditText edtEmail;

    @BindView(R.id.tvEmailNote)
    TextView tvEmailNote;

    @BindView(R.id.textInputCMND)
    TextInputLayout textInputCMND;

    @BindView(R.id.edtCmnd)
    EditText edtCmnd;

    @BindView(R.id.tvCancel)
    TextView tvCancel;

    @OnFocusChange(R.id.edtPhone)
    public void onFocusChangedPhone(boolean isFocus) {
        if (isFocus) {
            showPhoneNote();
        } else {
            if (isValidPhone()) {
                hidePhoneError();
            } else {
                showPhoneError();
            }
        }
    }

    @OnFocusChange(R.id.edtEmail)
    public void onFocusChangedEmail(boolean isFocus) {
        if (isFocus) {
            showEmailNote();
        } else {
            if (isValidEmail()) {
                hideEmailError();
            } else {
                showEmailError();
            }
        }
    }

    @OnFocusChange(R.id.edtCmnd)
    public void onFocusChangedCmnd(boolean isFocus) {
        if (!isFocus) {
            if (isValidCmnd()) {
                hideCmndError();
            } else {
                showCmndError();
            }
        }
    }

    @OnClick(R.id.tvCancel)
    public void onClickCancel(View view) {
        navigator.startPinProfileActivity(getActivity());
    }

    private void showPhoneError() {
        tvPhoneNote.setVisibility(View.GONE);
        textInputPhone.setErrorEnabled(true);
        textInputPhone.setError(getString(R.string.invalid_phone));
    }

    private void hidePhoneError() {
        tvPhoneNote.setVisibility(View.INVISIBLE);
        textInputPhone.setErrorEnabled(false);
        textInputPhone.setError(null);
    }

    private void showPhoneNote() {
        textInputPhone.setError(null);
        textInputPhone.setErrorEnabled(false);
        tvPhoneNote.setVisibility(View.VISIBLE);
    }

    private void showEmailError() {
        tvEmailNote.setVisibility(View.GONE);
        textInputEmail.setErrorEnabled(true);
        textInputEmail.setError(getString(R.string.invalid_email));
    }

    private void hideEmailError() {
        tvEmailNote.setVisibility(View.INVISIBLE);
        textInputEmail.setErrorEnabled(false);
        textInputEmail.setError(null);
    }

    private void showEmailNote() {
        textInputEmail.setErrorEnabled(false);
        textInputEmail.setError(null);
        tvEmailNote.setVisibility(View.VISIBLE);
    }

    private void showCmndError() {
        textInputCMND.setErrorEnabled(true);
        textInputCMND.setError(getString(R.string.invalid_cmnd));
    }

    private void hideCmndError() {
        textInputCMND.setErrorEnabled(false);
        textInputCMND.setError(null);
    }

    public PreProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PinProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PreProfileFragment newInstance() {
        PreProfileFragment fragment = new PreProfileFragment();
        return fragment;
    }

    @Override
    public void onClickContinue() {
        if (!isValidPhone()) {
            showPhoneError();
            return;
        } else {
            hidePhoneError();
        }
        if (!isValidEmail()) {
            showEmailError();
            return;
        } else {
            hideEmailError();
        }
        if (!isValidCmnd()) {
            showCmndError();
            return;
        } else {
            hideCmndError();
        }

        hidePhoneError();
        hideEmailError();
        hideCmndError();

        navigator.startPinProfileActivity(getActivity());
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_pre_profile;
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

    public boolean isValidPhone() {
        String phone = edtPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        return ValidateUtil.isMobileNumber(phone);
    }

    public boolean isValidEmail() {
        String email = edtEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return ValidateUtil.isEmailAddress(email);
    }

    public boolean isValidCmnd() {
        String cmnd = edtCmnd.getText().toString();
        if (TextUtils.isEmpty(cmnd)) {
            return false;
        }
        return true;
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
