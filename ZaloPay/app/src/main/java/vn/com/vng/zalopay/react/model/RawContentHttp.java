package vn.com.vng.zalopay.react.model;

import java.util.Map;

/**
 * Created by AnhHieu on 9/20/16.
 * *
 */
public class RawContentHttp {

    public String method;

    public Map<String, String> headers;

    public String body;

    public Map<String, String> query;

    public boolean hasMethod() {
        return "POST".equals(method) || "GET".equals(method);
    }
}
