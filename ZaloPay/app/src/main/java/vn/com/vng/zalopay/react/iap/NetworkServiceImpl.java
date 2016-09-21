package vn.com.vng.zalopay.react.iap;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zalopay.apploader.network.NetworkService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    final DynamicUrlService mRequestService;
    final Gson mGson;

    public NetworkServiceImpl(DynamicUrlService retrofit, Gson gson) {
        this.mRequestService = retrofit;
        this.mGson = gson;
    }

    public Observable<String> request(String baseUrl, ReadableMap content) {

        RawContentHttp rawContentHttp = convert(baseUrl, content);

        if (rawContentHttp == null) {
            return Observable.error(new FormatException());
        }

        return process(baseUrl, rawContentHttp.method, rawContentHttp.headers, rawContentHttp.query, rawContentHttp.body);
    }

    private Observable<String> process(String baseUrl, String method, Map<String, String> headers, @Nullable Map<String, String> query, @Nullable String body) {
        StringBuilder url = new StringBuilder();
        url.append(baseUrl);
        if (query != null && !query.isEmpty()) {
            url.append("?");
            url.append(buildQueryString(query));
        }

        String real_url = url.toString();
        Timber.d("real_url [%s]", real_url);

        if (method.equals("GET")) {
            return get(real_url, headers);
        } else {
            return post(real_url, headers, body);
        }
    }

    private String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private String buildQueryString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(entry.getKey()),
                    urlEncodeUTF8(entry.getValue())
            ));
        }
        return sb.toString();
    }

    private Observable<String> get(String url, Map<String, String> headers) {
        return mRequestService.get(url, headers);
    }

    private Observable<String> post(String url, Map<String, String> headers, String body) {
        if (TextUtils.isEmpty(body)) {
            return mRequestService.post(url, headers);
        } else {
            return mRequestService.post(url, headers, body);
        }
    }

    private RawContentHttp convert(String baseUrl, ReadableMap content) throws JsonSyntaxException {
        if (TextUtils.isEmpty(baseUrl)) {
            return null;
        }

        String method = content.getString("method");

        if (TextUtils.isEmpty(method)) {
            return null;
        }

        if (!method.equals("GET") && !method.equals("POST")) {
            return null;
        }

        Map<String, String> headers = toMap(content.getMap("headers"));
        if (headers == null) {
            return null;
        }

        Map<String, String> query = toMap(content.getMap("query"));
        String body = content.getString("body");

        RawContentHttp rawContentHttp = new RawContentHttp();
        rawContentHttp.body = body;
        rawContentHttp.query = query;
        rawContentHttp.method = method;
        rawContentHttp.headers = headers;

        return rawContentHttp;
    }

    Map<String, String> toMap(@javax.annotation.Nullable ReadableMap readableMap) {
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

    String toString(@javax.annotation.Nullable ReadableMap readableMap, String key) {
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
