package vn.com.vng.zalopay.react.iap;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.zalopay.apploader.network.NetworkService;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.DynamicUrlService;
import vn.com.vng.zalopay.data.exception.FormatException;
import vn.com.vng.zalopay.react.model.RawContentHttp;

/**
 * Created by AnhHieu on 9/20/16.
 * *
 */
public class NetworkServiceImpl implements NetworkService {

    private final DynamicUrlService mRequestService;

    public NetworkServiceImpl(DynamicUrlService retrofit) {
        this.mRequestService = retrofit;
    }

    public Observable<String> request(String baseUrl, ReadableMap content) {
        RawContentHttp rawContentHttp = null;

        try {
            rawContentHttp = convert(baseUrl, content);
        } catch (Exception e) {
            Timber.d(e, "exception");
        }

        if (rawContentHttp == null) {
            return Observable.error(new FormatException());
        }

        return process(baseUrl, rawContentHttp.getMethod(), rawContentHttp.getHeaders(), rawContentHttp.getQuery(), rawContentHttp.getBody());
    }

    private Observable<String> process(String baseUrl, @NonNull String method,@NonNull Map<String, String> headers, @NonNull Map<String, String> query, @Nullable String body) {
        if (method.equalsIgnoreCase("GET")) {
            return get(baseUrl, headers, query);
        } else {
            return post(baseUrl, headers, query, body);
        }
    }

    private Observable<String> get(String url, Map<String, String> headers, Map<String, String> query) {
        return mRequestService.get(url, headers, query);
    }

    private Observable<String> post(String url, Map<String, String> headers, Map<String, String> query, String body) {
        if (TextUtils.isEmpty(body)) {
            return mRequestService.post(url, headers, query);
        } else {
            return mRequestService.post(url, headers, query, body);
        }
    }

    private RawContentHttp convert(String baseUrl, ReadableMap content) throws Exception {

        if (TextUtils.isEmpty(baseUrl)) {
            return null;
        }

        if (!content.hasKey("method")) {
            return null;
        }

        String method = content.getString("method");

        if (TextUtils.isEmpty(method)) {
            return null;
        }

        if (!method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("POST")) {
            return null;
        }


        RawContentHttp rawContentHttp = new RawContentHttp();
        rawContentHttp.method = method;

        //option
        if (content.hasKey("headers")) {
            rawContentHttp.headers = toMap(content.getMap("headers"));

        }
        if (content.hasKey("query")) {
            rawContentHttp.query = toMap(content.getMap("query"));
        }

        if (content.hasKey("body")) {
            rawContentHttp.body = content.getString("body");
        }

        return rawContentHttp;
    }

    private Map<String, String> toMap(@Nullable ReadableMap readableMap) {
        if (readableMap == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        if (!iterator.hasNextKey()) {
            return result;
        }

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            result.put(key, toString(readableMap, key));
        }

        return result;
    }

    private String toString(@Nullable ReadableMap readableMap, String key) {
        if (readableMap == null) {
            return null;
        }

        Object result = null;

        ReadableType readableType = readableMap.getType(key);
        switch (readableType) {
            case Null:
                result = key;
                break;
            case Boolean:
                result = readableMap.getBoolean(key);
                break;
            case Number:
                double tmp = readableMap.getDouble(key);
                if (tmp == (int) tmp) {
                    result = (int) tmp;
                } else {
                    result = tmp;
                }
                break;
            case String:
                result = readableMap.getString(key);
                break;
            case Map:
                break;
            case Array:
                break;
            default:
                break;
        }

        return String.valueOf(result); //maybe `null`
    }
}
