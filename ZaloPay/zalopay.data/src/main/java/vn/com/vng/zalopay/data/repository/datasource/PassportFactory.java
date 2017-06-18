package vn.com.vng.zalopay.data.repository.datasource;

import android.text.TextUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import retrofit2.http.Field;
import retrofit2.http.Query;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.ServerErrorMessage;
import vn.com.vng.zalopay.data.api.PassportService;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.api.response.LogoutResponse;
import vn.com.vng.zalopay.data.api.response.VerifyInvitationCodeResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.RequirePhoneException;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;

import static vn.com.vng.zalopay.data.util.Utils.sha256Base;

/**
 * Created by AnhHieu on 3/30/16.
 */
@Singleton
public class PassportFactory {

    private PassportService passportService;

    private UserConfig userConfig;

    private final int payAppId;

    private ApplicationSession applicationSession;

    @Inject
    public PassportFactory(PassportService passportService, ApplicationSession applicationSession,
                           UserConfig userConfig, @Named("payAppId") int payAppId) {
        if (passportService == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.passportService = passportService;
        this.userConfig = userConfig;
        this.payAppId = payAppId;
        this.applicationSession = applicationSession;
    }

    public Observable<LoginResponse> login(long zaloid, String zAuthCode) {
        return passportService.login(payAppId, zaloid, zAuthCode)
                .doOnNext(response -> checkIfOldAccount(response.userid))
                .onErrorResumeNext(throwable -> {
                    if (!(throwable instanceof BodyException)) {
                        return Observable.error(throwable);
                    }

                    if (((BodyException) throwable).errorCode != ServerErrorMessage.REQUEST_PHONE_NUMBER) {
                        return Observable.error(throwable);
                    }

                    return Observable.error(new RequirePhoneException());
                })
                .doOnError(throwable -> {
                    if (throwable instanceof InvitationCodeException) {
                        LoginResponse loginResponse = (LoginResponse) ((InvitationCodeException) throwable).response;
                        userConfig.saveInvitationInfo(loginResponse.userid, loginResponse.accesstoken);
                    }
                })
                ;
    }

    public Observable<LogoutResponse> logout() {
        return passportService.logout(payAppId, userConfig.getUserId(), userConfig.getSession())
                .doOnTerminate(() -> applicationSession.cancelAllRequest());
    }

    public Observable<VerifyInvitationCodeResponse> verifyInvitationCode(String code) {
        return passportService.verifyCode(userConfig.getUserIdInvitation(), userConfig.getSessionInvitation(), code)
                .doOnNext(response -> checkIfOldAccount(response.userid))
                ;
    }

    public Observable<Boolean> registerPhoneNumber(long zaloid, String oauthcode, String paymentPassword, String phonenumber) {
        String pwdSha = sha256Base(paymentPassword);
        return passportService.registerPhoneNumber(zaloid, oauthcode, pwdSha, phonenumber)
                .map(BaseResponse::isSuccessfulResponse)
                ;
    }

    public Observable<LoginResponse> authenticatePhoneNumber(long zaloid, String oauthcode, String otp) {
        return passportService.authenticatePhoneNumber(zaloid, oauthcode, otp)
                .doOnNext(response -> checkIfOldAccount(response.userid))
                ;
    }

    private void checkIfOldAccount(String userId) {
        String lastUid = userConfig.getLastUid();

        Timber.d("checkIfOldAccount: user %s lastUid %s", userId, lastUid);

        if (!TextUtils.isEmpty(lastUid)) {
            if (!lastUid.equals(userId)) {
                clearAllUserDB();
                userConfig.removeFingerprint();
            }
        }

        userConfig.setLastUid(userId);
    }

    private void clearAllUserDB() {
        applicationSession.clearAllUserDB();
    }
}
