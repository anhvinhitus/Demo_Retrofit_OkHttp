package vn.com.vng.zalopay.data.repository.datasource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.Observable;
import vn.com.vng.zalopay.data.api.PassportService;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.api.response.LogoutResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;

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
        return passportService.login(payAppId, zuid, zAuthCode);
    }

    public Observable<LogoutResponse> logout(String uid, String accesstoken) {

        //K nen lay uid,accesstoken  tu userconfig.
        return passportService.logout(payAppId, uid, accesstoken)
                .doOnNext(logoutResponse -> userConfig.clearConfig());
    }
//
//    public Observable<BaseResponse> verifyAccessToken(String uid, String token) {
//        return passportService.verifyAccessToken(payAppId, uid, token);
//    }
}
