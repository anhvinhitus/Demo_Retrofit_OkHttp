package vn.com.vng.zalopay.react.model;

import java.util.HashMap;
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

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers == null ? new HashMap<String, String>() : headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQuery() {
        return query == null ? new HashMap<String, String>() : query;
    }
}
