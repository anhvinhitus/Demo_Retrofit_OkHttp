package vn.com.zalopay.utility;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class GsonUtils {

    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        gson = gsonBuilder.create();
    }

    public static String toJsonString(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJsonString(String sJson, Class<T> t) {
        if (TextUtils.isEmpty(sJson)) {
            return null;
        }
        return gson.fromJson(sJson, t);
    }

    public static <T> T fromJsonString(String sJson, Type t) {
        if (TextUtils.isEmpty(sJson)) {
            return null;
        }
        return gson.fromJson(sJson, t);
    }

}