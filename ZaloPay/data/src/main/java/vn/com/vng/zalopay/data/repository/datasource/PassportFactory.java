package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.Observable;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.api.ParamRequestProvider;
import vn.com.vng.zalopay.data.api.PassportService;
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
                           ParamRequestProvider paramRequestProvider,
                           UserConfig userConfig, @Named("payAppId") int payAppId) {
        if (context == null || passportService == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.passportService = passportService;
        this.params = paramRequestProvider.paramsDefault;
        this.authZaloParams = paramRequestProvider.paramsZalo;

        this.userConfig = userConfig;

        this.payAppId = payAppId;
    }

    public Observable<LoginResponse> login() {
        return passportService.login(authZaloParams, params)
                .doOnNext(loginResponse -> userConfig.saveConfig(loginResponse));
    }

    public Observable<LoginResponse> login(long zuid, String zAuthCode) {
        return passportService.login(payAppId, zuid, zAuthCode, params)
                .doOnNext(loginResponse -> userConfig.saveConfig(loginResponse));
    }

    public Observable<LogoutResponse> logout() {
        return passportService.logout(userConfig.getCurrentUser().accesstoken, authZaloParams, params)
                .doOnNext(logoutResponse -> userConfig.clearConfig());
    }
}
