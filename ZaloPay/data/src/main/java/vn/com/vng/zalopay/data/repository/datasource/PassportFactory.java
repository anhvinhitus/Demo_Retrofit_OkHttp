package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.PassportService;

/**
 * Created by AnhHieu on 3/30/16.
 */
@Singleton
public class PassportFactory {

    private Context context;

    private PassportService passportService;

    private HashMap<String, String> params;

    @Inject
    public PassportFactory(Context context, PassportService passportService, HashMap<String, String> params) {
        if (context == null || passportService == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.passportService = passportService;
        this.params = params;
    }
/*
    public Observable<UserEntity> loginByEmail(String email, String pass) {
        return passportService.loginByEmail(email, pass, params)
                .flatMap(response -> Observable.just(response.data));
    }

    public Observable<UserEntity> registerByEmail(String email, String pass) {
        return passportService.registerByEmail(email, pass, params)
                .flatMap(response -> Observable.just(response.data));
    }*/
}
