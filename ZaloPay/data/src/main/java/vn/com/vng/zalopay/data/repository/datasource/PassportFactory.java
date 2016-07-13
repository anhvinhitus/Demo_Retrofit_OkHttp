package vn.com.vng.zalopay.data.repository.datasource;

import android.text.TextUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.PassportService;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.api.response.LogoutResponse;
import vn.com.vng.zalopay.data.api.response.VerifyInvitationCodeResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;

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

    public Observable<LoginResponse> login(long zuid, String zAuthCode) {
        return passportService.login(payAppId, zuid, zAuthCode)
                .doOnNext(response -> checkIfOldAccount(response.userid))
                .doOnError(throwable -> {
                    if (throwable instanceof InvitationCodeException) {
                        Timber.d("login: InvitationCodeException");
                        LoginResponse loginResponse = (LoginResponse) ((InvitationCodeException) throwable).response;
                        userConfig.saveInvitationInfo(loginResponse.userid, loginResponse.accesstoken);
                    }
                })
                ;
    }

    public Observable<LogoutResponse> logout() {
        return passportService.logout(payAppId, userConfig.getUserId(), userConfig.getSession())
                ;
    }

    //
    public Observable<VerifyInvitationCodeResponse> verifyInvitationCode(String code) {
        return passportService.verifyCode(userConfig.getUserIdInvitation(), userConfig.getSessionInvitation(), code)
                .doOnNext(response -> checkIfOldAccount(response.userid))
                ;
    }

    private void checkIfOldAccount(String userId) {
        String lastUid = userConfig.getLastUid();

        Timber.d("checkIfOldAccount: user %s lastUid %s", userId, lastUid);

        if (!TextUtils.isEmpty(lastUid)) {
            if (!lastUid.equals(userId)) {
                clearAllUserDB();
            }
        }
        
        userConfig.setLastUid(userId);
    }

    private void clearAllUserDB() {
        applicationSession.clearAllUserDB();
    }
}
