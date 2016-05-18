package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.Observable;
import vn.com.vng.zalopay.data.api.PassportService;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.api.response.LogoutResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;

/**
 * Created by AnhHieu on 3/30/16.
 */
@Singleton
public class PassportFactory {

    private Context context;

    private PassportService passportService;

    private HashMap<String, String> params;

    private HashMap<String, String> authZaloParams;

    private UserConfig userConfig;

    private final int payAppId;

    @Inject
    public PassportFactory(Context context, PassportService passportService,
                           UserConfig userConfig, @Named("payAppId") int payAppId) {
        if (context == null || passportService == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.passportService = passportService;
        this.userConfig = userConfig;
        this.payAppId = payAppId;
    }

    public Observable<LoginResponse> login(long zuid, String zAuthCode) {
        return passportService.login(payAppId, zuid, zAuthCode);
    }

    public Observable<LogoutResponse> logout(long uid, String accesstoken) {

        //K nen lay uid,accesstoken  tu userconfig.
        return passportService.logout(payAppId, uid, accesstoken)
                .doOnNext(logoutResponse -> userConfig.clearConfig());
    }

    public Observable<BaseResponse> verifyAccessToken(long uid, String token) {
        return passportService.verifyAccessToken(payAppId, uid, token);
    }
}
