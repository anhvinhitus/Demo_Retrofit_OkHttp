package vn.com.vng.zalopay.data.repository.datasource;

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

/**
 * Created by AnhHieu on 3/30/16.
 */
@Singleton
public class PassportFactory {

    private PassportService passportService;

    private UserConfig userConfig;

    private final int payAppId;

    @Inject
    public PassportFactory(PassportService passportService,
                           UserConfig userConfig, @Named("payAppId") int payAppId) {
        if (passportService == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.passportService = passportService;
        this.userConfig = userConfig;
        this.payAppId = payAppId;
    }

    public Observable<LoginResponse> login(long zuid, String zAuthCode) {
        return passportService.login(payAppId, zuid, zAuthCode)
                .doOnError(throwable -> {
                    if (throwable instanceof InvitationCodeException) {
                        Timber.d("login: InvitationCodeException");
                        LoginResponse loginResponse = (LoginResponse) ((InvitationCodeException) throwable).response;
                        userConfig.saveInvitationInfo(loginResponse.userid, loginResponse.accesstoken);
                    }
                })
                ;
    }

    public Observable<LogoutResponse> logout(String uid, String accesstoken) {
        return passportService.logout(payAppId, uid, accesstoken)
                .doOnNext(logoutResponse -> userConfig.clearConfig());
    }

    //
    public Observable<VerifyInvitationCodeResponse> verifyInvitationCode(String code) {
        return passportService.verifyCode(userConfig.getUserIdInvitation(), userConfig.getSessionInvitation(), code);
    }
}
