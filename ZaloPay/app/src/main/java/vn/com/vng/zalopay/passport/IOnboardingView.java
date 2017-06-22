package vn.com.vng.zalopay.passport;

import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;

/**
 * Created by hieuvm on 6/10/17.
 * *
 */
interface IOnboardingView {
    void gotoHomePage();

    void gotoLoginPage();

    void finish();

    void setProfile(ZaloProfile user);

    void nextPage();

    void previousPage();

    void showLoading();

    void hideLoading();

    void showError(String msg);

    void setOtp(String otp);

    void startOTPCountDown();

    void showIncorrectOtp(String msg);

    void showIncorrectPhone(String msg);

    void resendOTPSuccess();
}