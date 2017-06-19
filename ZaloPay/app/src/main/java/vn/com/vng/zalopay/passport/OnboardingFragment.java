package vn.com.vng.zalopay.passport;

import android.Manifest;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import butterknife.BindView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.IconFontTextView;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import vn.com.vng.zalopay.passport.widget.CustomKeyboardView;

import com.zalopay.ui.widget.util.TimeUtils;

import timber.log.Timber;

import static vn.com.vng.zalopay.Constants.ARGUMENT_KEY_OAUTHTOKEN;
import static vn.com.vng.zalopay.Constants.ARGUMENT_KEY_ZALOPROFILE;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;
import vn.com.vng.zalopay.passport.widget.InputEnteredListener;
import vn.com.vng.zalopay.passport.widget.OnboardingEditTextView;
import vn.com.vng.zalopay.passport.widget.OnboardingPasswordRoundView;
import vn.com.vng.zalopay.ui.activity.BaseToolBarActivity;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.widget.validate.VNPhoneValidate;

import javax.inject.Inject;


/**
 * Created by hieuvm on 6/9/17.
 * *
 * *
 */
public class OnboardingFragment extends RuntimePermissionFragment implements IOnboardingView {

    public static OnboardingFragment newInstance() {

        Bundle args = new Bundle();

        OnboardingFragment fragment = new OnboardingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARGUMENT_TAB_POSITION = "position";
    private static final String ARGUMENT_PHONE_NUMBER = "phone";
    public static final long RESEND_OTP_INTERVAL = 60000L;
    public static final int INPUT_PASSWORD = 0;
    public static final int REINPUT_PASSWORD = 1;
    public static final int INPUT_PHONE = 2;
    public static final int INPUT_OTP = 3;

    public void setupFragmentComponent() {
        getAppComponent().inject(this);
    }

    public int getResLayoutId() {
        return R.layout.fragment_onboarding;
    }

    @BindView(R.id.ivAvatar)
    SimpleDraweeView mAvatarView;

    @BindView(R.id.tvDisplayName)
    TextView mDisplayNameView;

    @BindView(R.id.tvGender)
    IconFontTextView mGenderView;

    @BindView(R.id.tvBirthday)
    IconFontTextView mBirthDayView;

    @BindView(R.id.keyboard)
    CustomKeyboardView mKeyboardView;

    @Inject
    OnboardingPresenter mPresenter;

    private ZaloProfile mProfile;
    private String oauthcode;

    @BindView(R.id.flipper)
    ViewFlipper mFlipperView;

    @BindView(R.id.flipper1)
    OnboardingPasswordRoundView mInputPwdView;

    @BindView(R.id.flipper2)
    OnboardingPasswordRoundView mReInputPwdView;

    @BindView(R.id.flipper3)
    OnboardingEditTextView mInputPhoneView;

    @BindView(R.id.flipper4)
    OnboardingEditTextView mInputOtpView;

    private CountDownTimer mCountDownTime = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs(savedInstanceState == null ? getActivity().getIntent().getExtras() : savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        setProfile(mProfile);

        mInputPwdView.setEnteredListener(new InputPasswordListener());

        mReInputPwdView.setEnteredListener(new ReInputPasswordListener());
        mReInputPwdView.setOnClick(v -> previousPage());

        mInputPhoneView.setOnClick(v -> {
            if (mInputPhoneView.validate()) {
                register();
            }
        });

        mInputPhoneView.setLengthToActiveButton(1);
        mInputPhoneView.setInputLength(getResources().getInteger(R.integer.max_length_phone));
        mInputPhoneView.addValidator(new VNPhoneValidate().getValidates());

        mInputOtpView.setOnClick(v -> authenticate());
        int pinLength = getResources().getInteger(R.integer.pin_length);
        mInputOtpView.setLengthToActiveButton(pinLength);
        mInputOtpView.setInputLength(pinLength);
        mInputOtpView.setOnSecondClick(v -> register());

        shouldShowBackButton();
        keyboardActive(getCurrentPage());
    }

    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    public boolean onBackPressed() {

        switch (getCurrentPage()) {
            case INPUT_PASSWORD:
                return false;
            case REINPUT_PASSWORD:
                previousPage();
                return true;
            case INPUT_PHONE:
                return true;
            case INPUT_OTP:
                previousToPhone();
                return true;
        }

        return super.onBackPressed();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARGUMENT_KEY_ZALOPROFILE, mProfile);
        outState.putString(ARGUMENT_KEY_OAUTHTOKEN, oauthcode);
        outState.putInt(ARGUMENT_TAB_POSITION, getCurrentPage());

        String phoneNumber = mInputPhoneView.getInputText();
        if (!TextUtils.isEmpty(phoneNumber)) {
            outState.putString(ARGUMENT_PHONE_NUMBER, phoneNumber);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreState(savedInstanceState);
    }

    public void onDestroyView() {
        stopCountDown();
        mPresenter.detachView();
        super.onDestroyView();
    }

    public void finish() {
        getActivity().finish();
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }


        int flipperPosition = savedInstanceState.getInt(ARGUMENT_TAB_POSITION, -1);
        if (flipperPosition <= REINPUT_PASSWORD) {
            flipperPosition = 0;
        }

        Timber.d("restoreState [flipperPosition: %s]", flipperPosition);

        if (savedInstanceState.containsKey(ARGUMENT_PHONE_NUMBER)) {
            String phoneNumber = savedInstanceState.getString(ARGUMENT_PHONE_NUMBER);
            mInputPhoneView.setInputText(phoneNumber);
        }

        if (flipperPosition >= 0) {
            mFlipperView.setDisplayedChild(flipperPosition);
        }

        if (flipperPosition == INPUT_OTP) {
            setSubTitleOtp(mInputPhoneView.getInputText());
        }

        shouldShowBackButton();
        keyboardActive(getCurrentPage());
    }

    private void initArgs(Bundle bundle) {
        mProfile = bundle.getParcelable(ARGUMENT_KEY_ZALOPROFILE);
        oauthcode = bundle.getString(ARGUMENT_KEY_OAUTHTOKEN);
    }

    private class InputPasswordListener implements InputEnteredListener {
        public void onPinEntered(@NonNull String pinCode) {
            nextPage();
        }
    }

    private class ReInputPasswordListener implements InputEnteredListener {
        public void onPinEntered(@NonNull String pinCode) {
            if (pinCode.equals(mInputPwdView.getInputText())) {
                nextPage();
            } else {
                //mReInputPwdView.showPrimaryButton(true)
                mReInputPwdView.setError(getString(R.string.password_not_match), true);
            }
        }
    }

    private void register() {
        mPresenter.register(mProfile.userId, oauthcode, mReInputPwdView.getInputText(), mInputPhoneView.getInputText());
    }

    private void authenticate() {
        mPresenter.authenticate(mProfile.userId, oauthcode, mInputOtpView.getInputText());
    }

    public void gotoHomePage() {
        navigator.startHomeActivity(getContext());
    }

    public void setProfile(ZaloProfile user) {
        mAvatarView.setImageURI(user.avatar);
        mDisplayNameView.setText(user.displayName);
        mBirthDayView.setText(TimeUtils.toDate(user.birthDate));
        mGenderView.setText(user.getGender());
    }

    public void nextPage() {
        if (getCurrentPage() >= INPUT_OTP) {
            return;
        }

        mFlipperView.showNext();
        onPageChange(getCurrentPage(), true);
    }

    public void previousPage() {
        if (getCurrentPage() <= INPUT_PASSWORD) {
            return;
        }

        mFlipperView.showPrevious();
        onPageChange(getCurrentPage(), false);
    }

    private void previousToPhone() {
        showConfirmDialog(getString(R.string.ip_desc_confirm_change_phone),
                getString(R.string.accept),
                getString(R.string.cancel), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKevent() {
                        if (!isAdded()) {
                            return;
                        }

                        previousPage();
                    }
                });
    }

    private void onPageChange(int position, Boolean next) {
        stopCountDown();
        switch (position) {
            case INPUT_PASSWORD:
                onPasswordPageActive();
                break;
            case REINPUT_PASSWORD:
                onReInputPasswordActive();
                break;
            case INPUT_PHONE:
                onPhonePageActive(next);
                break;
            case INPUT_OTP:
                onOtpPageActive();
                break;
        }

        shouldShowBackButton();
        keyboardActive(position);
    }

    private int getCurrentPage() {
        return mFlipperView.getDisplayedChild();
    }

    private void onReInputPasswordActive() {

    }

    private void onOtpPageActive() {
        isPermissionGrantedAndRequest(new String[]{Manifest.permission.RECEIVE_SMS}, RuntimePermissionFragment.PERMISSION_CODE.RECEIVE_SMS);
        setSubTitleOtp(mInputPhoneView.getInputText());
        startOTPCountDown();
    }

    private void onPasswordPageActive() {
        mReInputPwdView.reset();
        mInputPwdView.reset();
    }

    private void onPhonePageActive(Boolean next) {
        if (next) {
            mReInputPwdView.clearError();
            //mReInputPwdView.showPrimaryButton(false)
        } else {
            mInputOtpView.setInputText("");
        }

    }

    private void setSubTitleOtp(String phone) {
        String subtitle = String.format(getString(R.string.ip_desc_format_conform_otp), phone);
        mInputOtpView.setSubTitle(subtitle);
    }

    private void keyboardActive(int position) {

        switch (position) {
            case INPUT_PASSWORD:
                mKeyboardView.setEditable(mInputPwdView.getNumberEditable());
                break;
            case REINPUT_PASSWORD:
                mKeyboardView.setEditable(mReInputPwdView.getNumberEditable());
                break;
            case INPUT_PHONE:
                mKeyboardView.setEditable(mInputPhoneView.getNumberEditable());
                break;
            case INPUT_OTP:
                mKeyboardView.setEditable(mInputOtpView.getNumberEditable());
                break;
        }
    }

    public void showLoading() {
        showProgressDialog();
    }

    public void hideLoading() {
        hideProgressDialog();
    }

    public void showError(String msg) {
        showErrorDialog(msg);
    }

    @Override
    public void showIncorrectOtp(String msg) {
        if (getCurrentPage() != INPUT_OTP) {
            return;
        }

        mInputOtpView.setError(msg);
    }

    public void setOtp(String otp) {

        if (getCurrentPage() != INPUT_OTP) {
            return;
        }

        mInputOtpView.setInputText(otp);
    }

    private void shouldShowBackButton() {
        int flipperPosition = getCurrentPage();
        boolean showButtonBack = flipperPosition == REINPUT_PASSWORD || flipperPosition == INPUT_OTP;
        setDisplayHomeAsUpEnabled(showButtonBack);
    }

    private void setDisplayHomeAsUpEnabled(Boolean isShow) {
        ActionBar actionbar = ((BaseToolBarActivity) getActivity()).getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(isShow);
        }
    }

    public void permissionGranted(int permissionRequestCode, boolean isGranted) {

    }

    private void stopCountDown() {
        if (mCountDownTime != null) {
            mCountDownTime.cancel();
        }
        mCountDownTime = null;
    }

    public void startOTPCountDown() {
        stopCountDown();
        mCountDownTime = new ResendCountDown(RESEND_OTP_INTERVAL, 1000);
        mCountDownTime.start();
    }

    private class ResendCountDown extends CountDownTimer {

        ResendCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            mInputOtpView.setTextSecondButton(getString(R.string.resend_otp));
            mInputOtpView.setEnableSecondButton(true);
        }

        public void onTick(long millisUntilFinished) {
            String msgFormat = getString(R.string.resend_otp_count_down_format);
            mInputOtpView.setTextSecondButton(String.format(msgFormat, TimeUtils.formatCountDownTime(millisUntilFinished)));
            mInputOtpView.setEnableSecondButton(false);
        }

    }
}