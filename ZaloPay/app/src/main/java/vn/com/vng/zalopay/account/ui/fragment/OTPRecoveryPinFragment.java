package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.OTPRecoveryPinPresenter;
import vn.com.vng.zalopay.account.ui.view.IOTPRecoveryPinView;
import vn.com.vng.zalopay.event.ReceiveSmsEvent;
import vn.com.vng.zalopay.ui.widget.ClearableEditText;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOTPFragmentListener} interface
 * to handle interaction events.
 * Use the {@link OTPRecoveryPinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OTPRecoveryPinFragment extends AbsProfileFragment implements IOTPRecoveryPinView {
    private OnOTPFragmentListener mListener;
    private int mRetryOtp = 0;

    @Inject
    OTPRecoveryPinPresenter presenter;

    @Inject
    EventBus mEventBus;

    @BindView(R.id.textInputOTP)
    TextInputLayout textInputOTP;
    @BindView(R.id.edtOTP)
    ClearableEditText edtOTP;

    private void showOTPError() {
        textInputOTP.setErrorEnabled(true);
        if (TextUtils.isEmpty(edtOTP.getText().toString())) {
            textInputOTP.setError(getString(R.string.invalid_otp_empty));
        } else {
            textInputOTP.setError(getString(R.string.invalid_otp));
        }
    }

    private void hideOTPError() {
        textInputOTP.setErrorEnabled(false);
        textInputOTP.setError(null);
    }

    public boolean isValidOTP() {
        String otp = edtOTP.getText().toString();
        return !TextUtils.isEmpty(otp);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PinProfileFragment.
     */
    public static OTPRecoveryPinFragment newInstance() {
        return new OTPRecoveryPinFragment();
    }

    @Override
    public void onClickContinue() {
        if (!isValidOTP()) {
            showOTPError();
            return;
        } else {
            hideOTPError();
        }
        presenter.verifyOtp(edtOTP.getText().toString());
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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOTPFragmentListener) {
            mListener = (OnOTPFragmentListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();

        Timber.d("Register OTPRecoveryPinFragment to eventbus");
        mEventBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        Timber.d("Unregister OTPRecoveryPinFragment from eventbus");
        super.onDestroy();
    }

    @Override
    public void confirmOTPSuccess() {
        if (mListener != null) {
            mListener.onConfirmOTPSucess();
        }
        navigator.startHomeActivity(getContext(), true);
    }

    @Override
    public void confirmOTPError(String msg) {
        showError(msg);
        if (mRetryOtp < 3) {
            mRetryOtp++;
        } else {
            navigator.startHomeActivity(getContext(), true);
        }
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

    public void showError(int messageResource) {
        showToast(messageResource);
    }


    @Subscribe
    void onReceiveSmsMessages(ReceiveSmsEvent event) {
        String pattern = "(.*)(\\d{6})(.*)";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        for (ReceiveSmsEvent.SmsMessage message : event.messages) {
            Timber.d("Receive SMS: [%s: %s]", message.from, message.body);
            Matcher m = r.matcher(message.body);
            if (m.find()) {
                Timber.d("Found OTP: %s", m.group(2));
                edtOTP.setText(m.group(2));
            }
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
    public interface OnOTPFragmentListener {
        void onConfirmOTPSucess();

        void onConfirmOTPFail();
    }
}
