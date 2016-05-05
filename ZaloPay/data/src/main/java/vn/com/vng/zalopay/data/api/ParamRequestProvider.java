package vn.com.vng.zalopay.data.api;

import android.os.Build;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by AnhHieu on 4/26/16.
 */

@Singleton
public class ParamRequestProvider {
    public HashMap<String, String> paramsDefault;

    public HashMap<String, String> paramsZalo;

    @Inject
    public ParamRequestProvider() {
        paramsDefault = buildParamDefault();
        paramsZalo = new HashMap<>();
    }


    private HashMap buildParamDefault() {
        HashMap<String, String> param = new HashMap<>();
        param.put("devicemodel", Build.MODEL);
        return param;
    }


    public HashMap<String, String> getParamsDefault() {
        return paramsDefault;
    }

    public void setParamsDefault(HashMap<String, String> paramsDefault) {
        this.paramsDefault = paramsDefault;
    }

    public HashMap<String, String> getParamsZalo() {
        return paramsZalo;
    }

    public void setParamsZalo(HashMap<String, String> paramsZalo) {
        this.paramsZalo = paramsZalo;
    }
}
