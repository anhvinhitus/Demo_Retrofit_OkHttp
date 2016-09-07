package vn.com.vng.zalopay.account.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
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
import vn.com.vng.zalopay.account.ui.presenter.ChangePinPresenter;
import vn.com.vng.zalopay.account.ui.view.IChangePinVerifyView;
import vn.com.vng.zalopay.event.ReceiveSmsEvent;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.ClearableEditText;

/**
 * Created by AnhHieu on 8/25/16.
 */
public class ChangePinVerifyFragment extends BaseFragment implements IChangePinVerifyView {

    public static ChangePinVerifyFragment newInstance() {

        Bundle args = new Bundle();

        ChangePinVerifyFragment fragment = new ChangePinVerifyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_otp;
    }

    @Inject
    ChangePinPresenter presenter;

    @Inject
    EventBus mEventBus;

    @BindView(R.id.textInputOTP)
    TextInputLayout textInputOTP;

    @BindView(R.id.edtOTP)
    ClearableEditText edtOTP;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setVerifyView(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        presenter.destroyVerifyView();
        super.onDestroyView();
    }

    @Subscribe
    public void onReceiveSmsMessages(ReceiveSmsEvent event) {
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

    private void showOTPError() {
        textInputOTP.setErrorEnabled(true);
        if (TextUtils.isEmpty(ClearableEditText.optText(edtOTP))) {
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
        String otp = ClearableEditText.optText(edtOTP);
        return !TextUtils.isEmpty(otp);
    }

    @Override
    public void checkOtpValidAndSubmit() {
        if (!isValidOTP()) {
            showOTPError();
            return;
        } else {
            hideOTPError();
        }
        presenter.verify(ClearableEditText.optText(edtOTP));
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
