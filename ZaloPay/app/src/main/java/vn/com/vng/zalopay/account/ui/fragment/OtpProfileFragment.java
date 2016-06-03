package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.widget.ClearableEditText;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOTPFragmentListener} interface
 * to handle interaction events.
 * Use the {@link OtpProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OtpProfileFragment extends AbsProfileFragment {
    private OnOTPFragmentListener mListener;

    @BindView(R.id.edtOTP)
    ClearableEditText edtOTP;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PinProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OtpProfileFragment newInstance() {
        OtpProfileFragment fragment = new OtpProfileFragment();
        return fragment;
    }

    @Override
    public void onClickContinue() {

        navigator.startHomeActivity(getContext(), true);
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_otp;
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

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOTPFragmentListener) {
            mListener = (OnOTPFragmentListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnOTPFragmentListener");
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
    public interface OnOTPFragmentListener {
        // TODO: Update argument type and name
        void onConfirmOTPSucess();
        void onConfirmOTPFail();
    }
}
