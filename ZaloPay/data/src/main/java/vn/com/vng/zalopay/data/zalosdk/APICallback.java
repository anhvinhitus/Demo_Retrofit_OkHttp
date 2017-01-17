package vn.com.vng.zalopay.data.zalosdk;

import org.json.JSONObject;

/**
 * Created by hieuvm on 1/17/17.
 */

public interface APICallback {
    void onResult(JSONObject result);
}
