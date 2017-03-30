package vn.com.vng.zalopay.account.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.zalopay.ui.widget.edittext.ZPEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.OTPProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IOTPProfileView;
import vn.com.vng.zalopay.event.ReceiveOTPEvent;
import vn.com.vng.zalopay.event.ReceiveSmsEvent;
import vn.com.vng.zalopay.scanners.ui.FragmentLifecycle;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.widget.validate.DigitsOnlyValidate;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOTPFragmentListener} interface
 * to handle interaction events.
 * Use the {@link OtpProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OtpProfileFragment extends RuntimePermissionFragment implements IOTPProfileView, FragmentLifecycle {

    public static OtpProfileFragment newInstance() {
        return new OtpProfileFragment();
    }

    private OnOTPFragmentListener mListener;

    @Inject
    OTPProfilePresenter presenter;

    @Inject
    EventBus mEventBus;

    @BindView(R.id.edtOTP)
    ZPEditText mEdtOTPView;

    @BindView(R.id.btnContinue)
    View btnContinue;

    @OnTextChanged(value = R.id.edtOTP, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onEdtOTPChanged() {
        btnContinue.setEnabled(mEdtOTPView.isValid());
    }

    @OnClick(R.id.btnContinue)
    public void onClickContinue() {
        if (!mEdtOTPView.validate()) {
            return;
        }
        presenter.verifyOtp(mEdtOTPView.getText().toString());
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
        presenter.attachView(this);
        mEdtOTPView.addValidator(new DigitsOnlyValidate(getString(R.string.exception_otp_invaild)));
        mEdtOTPView.requestFocus();
        btnContinue.setEnabled(mEdtOTPView.isValid());
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
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        presenter.detachView();
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
        super.onDestroy();
    }

    @Override
    public void confirmOTPSuccess() {
        if (mListener != null) {
            mListener.onConfirmOTPSuccess();
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
    public void showError(String message) {
        super.showErrorDialog(message, getContext().getString(R.string.txt_close), () -> {
            if (mEdtOTPView != null && !mEdtOTPView.isFocused()) {
                mEdtOTPView.requestFocus();
            }
        });
    }

    public void showError(int messageResource) {
        showErrorDialog(getString(messageResource));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onReceiveSmsMessages(ReceiveSmsEvent event) {
        String pattern = "(.*)(\\d{6})(.*)";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        for (ReceiveSmsEvent.SmsMessage message : event.messages) {
            Timber.d("Receive SMS: [%s: %s]", message.from, message.body);
            Matcher m = r.matcher(message.body);
            if (m.find()) {
                Timber.d("Found OTP: %s", m.group(2));
                mEdtOTPView.setText(m.group(2));
            }
        }
        mEventBus.removeStickyEvent(ReceiveSmsEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiveOTP(ReceiveOTPEvent event) {

        Timber.d("On receive otp : %s", event.otp);

        if (getUserVisibleHint()) {
            if (mEdtOTPView != null) {
                mEdtOTPView.setText(event.otp);
            }
        }

        mEventBus.removeStickyEvent(ReceiveOTPEvent.class);
    }

    public interface OnOTPFragmentListener {
        void onConfirmOTPSuccess();
    }

    @Override
    public void onStartFragment() {
        isPermissionGrantedAndRequest(Manifest.permission.RECEIVE_SMS, PERMISSION_CODE.RECEIVE_SMS);
    }

    @Override
    public void onStopFragment() {
    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
    }

    public boolean onBackPressed() {
        hideKeyboard();
        return super.onBackPressed();
    }
}
