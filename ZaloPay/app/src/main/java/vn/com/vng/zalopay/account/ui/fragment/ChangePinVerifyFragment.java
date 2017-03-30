package vn.com.vng.zalopay.account.ui.fragment;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.account.ui.view.IChangePinVerifyView;
import vn.com.vng.zalopay.event.ReceiveOTPEvent;
import vn.com.vng.zalopay.event.ReceiveSmsEvent;
import vn.com.vng.zalopay.scanners.ui.FragmentLifecycle;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.widget.validate.DigitsOnlyValidate;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public class ChangePinVerifyFragment extends RuntimePermissionFragment implements IChangePinVerifyView, FragmentLifecycle {

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
        return R.layout.fragment_change_pin_verify;
    }

    @Inject
    IChangePinPresenter presenter;

    @Inject
    EventBus mEventBus;

    @BindView(R.id.edtOTP)
    ZPEditText mEdtOTPView;

    @BindView(R.id.btnConfirm)
    View mBtnConfirmView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setVerifyView(this);
        mEdtOTPView.addValidator(new DigitsOnlyValidate(getString(R.string.exception_otp_invaild)));
        mBtnConfirmView.setEnabled(mEdtOTPView.isValid());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        presenter.destroyVerifyView();
        super.onDestroyView();
    }

    @OnClick(R.id.btnConfirm)
    public void onClickContinue() {

        if (!mEdtOTPView.validate()) {
            return;
        }

        presenter.verify(mEdtOTPView.getText().toString());
    }

    @OnTextChanged(value = R.id.edtOTP, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onEdtOTPChanged() {
        mBtnConfirmView.setEnabled(mEdtOTPView.isValid());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
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
        showErrorDialog(message);
    }

    @Override
    public void onStartFragment() {
        isPermissionGrantedAndRequest(new String[]{Manifest.permission.RECEIVE_SMS}, PERMISSION_CODE.RECEIVE_SMS);
    }

    @Override
    public void onStopFragment() {
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

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
    }
}
