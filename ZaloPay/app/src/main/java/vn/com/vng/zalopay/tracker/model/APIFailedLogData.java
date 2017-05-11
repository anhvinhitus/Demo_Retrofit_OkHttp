package vn.com.vng.zalopay.tracker.model;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hieuvm on 5/10/17.
 * *
 */

public class APIFailedLogData extends AbstractLogData {

    private final String api_name;
    private final int http_code;
    private final int server_code;
    private final int network_code;

    public APIFailedLogData(String api_name, int http_code, int server_code, int network_code) {
        super(TrackerType.API_FAILED_TYPE);
        this.api_name = api_name;
        this.http_code = http_code;
        this.server_code = server_code;
        this.network_code = network_code;
    }

    @Override
    public String getMessage() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("type", eventType);
            jsonObject.put("api_name", api_name == null ? "" : api_name);
            jsonObject.put("http_code", http_code);
            jsonObject.put("server_code", server_code);
            jsonObject.put("network_code", network_code);
            jsonObject.put("timestamp", timestamp);
        } catch (JSONException ignore) {
        }

        return jsonObject.toString();
    }
}
