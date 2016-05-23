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
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.R;
import vn.zing.pay.zmpsdk.view.animation.ActivityAnimator;

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

    @BindView(R.id.textInputPin)
    TextInputLayout textInputPin;

    @BindView(R.id.edtPin)
    EditText edtPin;

    @BindView(R.id.tvPinNote)
    TextView tvPinNote;

    @BindView(R.id.textInputPinCompare)
    TextInputLayout textInputPinCompare;

    @BindView(R.id.edtPinCompare)
    EditText edtPinCompare;

    @BindView(R.id.tvCancel)
    TextView tvCancel;

    @OnClick(R.id.tvCancel)
    public void onClickCancel(View view) {
        navigator.startHomeActivity(getContext(), true);
        ActivityAnimator anim = new ActivityAnimator();
        anim.fadeAnimation(getActivity());
    }

    @OnFocusChange(R.id.edtPin)
    public void onFocusChangedPin(boolean isFocus) {
        if (isFocus) {
            showPinNote();
        } else {
            if (isValidPin()) {
                hidePinError();
            } else {
                showPinError();
            }
        }
    }

    @OnFocusChange(R.id.edtPinCompare)
    public void onFocusChangedPinCompare(boolean isFocus) {
        if (!isFocus) {
            if (isValidPinCompare()) {
                hidePinCompareError();
            } else {
                showPinCompareError();
            }
        }
    }

    @OnTextChanged(R.id.edtPin)
    public void onTextChangePin(CharSequence text) {
        if (isValidPin()) {
            hidePinError();
            if (isValidPinCompare()) {
                hidePinCompareError();
            }
        }
    }

    @OnTextChanged(R.id.edtPinCompare)
    public void onTextChangePinCompare(CharSequence text) {
        if (isValidPinCompare()) {
            hidePinError();
            hidePinCompareError();
        } else {
            showPinCompareError();
        }
    }

    private boolean isValidPinCompare() {
        String pin = edtPin.getText().toString();
        String pinCompare = edtPinCompare.getText().toString();
        if (TextUtils.isEmpty(pinCompare) || !pinCompare.equals(pin)) {
            return false;
        }
        return true;
    }

    private boolean isValidPin() {
        String pin = edtPin.getText().toString();
        if (TextUtils.isEmpty(pin)) {
            return false;
        }
        return true;
    }

    private void hidePinError() {
        tvPinNote.setVisibility(View.INVISIBLE);
        textInputPin.setErrorEnabled(false);
        textInputPin.setError(null);
    }

    private void showPinNote() {
        textInputPin.setError(null);
        textInputPin.setErrorEnabled(false);
        tvPinNote.setVisibility(View.VISIBLE);
    }

    private void showPinError() {
        tvPinNote.setVisibility(View.GONE);
        textInputPin.setErrorEnabled(true);
        textInputPin.setError(getString(R.string.invalid_pin));
    }

    private void hidePinCompareError() {
        textInputPinCompare.setErrorEnabled(false);
        textInputPinCompare.setError(null);
    }

    private void showPinCompareError() {
        textInputPinCompare.setErrorEnabled(true);
        textInputPinCompare.setError(getString(R.string.invalid_pin_compare));
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
            showPinError();
            return;
        } else {
            hidePinError();
        }
        if (!isValidPinCompare()) {
            showPinCompareError();
            return;
        } else {
            hidePinCompareError();
        }

        hidePinError();
        hidePinCompareError();

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
