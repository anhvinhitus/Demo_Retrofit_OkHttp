package vn.com.vng.zalopay.react.iap;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zalopay.apploader.network.NetworkService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
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

    public Observable<String> request(String baseUrl, String content) {

        if (!isValidFormat(baseUrl, content)) {
            return Observable.error(new FormatException());
        }

        RawContentHttp rawContentHttp = mGson.fromJson(content, RawContentHttp.class);
        return process(baseUrl, rawContentHttp.method, rawContentHttp.headers, rawContentHttp.query, rawContentHttp.body);
    }

    private Observable<String> process(String baseUrl, String method, Map<String, String> headers, Map<String, String> query, String body) {
        String url;
        if (query == null || query.isEmpty()) {
            url = baseUrl;
        } else {
            url = baseUrl + buildQueryString(query);
        }

        Timber.d("process url %s", url);

        if (method.equals("GET")) {
            return get(url, headers);
        } else {
            return post(url, headers, body);
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

    private boolean isValidFormat(String baseUrl, String content) throws JsonSyntaxException {
        if (TextUtils.isEmpty(baseUrl)) {
            return false;
        }

        if (TextUtils.isEmpty(content)) {
            return false;
        }

        RawContentHttp contentData = mGson.fromJson(content, RawContentHttp.class);
        return contentData.hasMethod();
    }
}
