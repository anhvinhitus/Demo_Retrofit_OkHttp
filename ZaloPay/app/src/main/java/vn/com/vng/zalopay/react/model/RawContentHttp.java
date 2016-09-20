package vn.com.vng.zalopay.react.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by AnhHieu on 9/20/16.
 * *
 */
public class RawContentHttp {

    @SerializedName("method")
    public String method;

    @SerializedName("headers")
    public Map<String, String> headers;

    @SerializedName("body")
    public String body;

    @SerializedName("query")
    public Map query;

    public boolean hasMethod() {
        return "POST".equals(method) || "GET".equals(method);
    }
}
