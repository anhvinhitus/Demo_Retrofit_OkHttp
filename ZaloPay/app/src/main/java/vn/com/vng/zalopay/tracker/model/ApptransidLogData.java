package vn.com.vng.zalopay.tracker.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hieuvm on 5/11/17.
 * *
 */

public class ApptransidLogData extends AbstractLogData {

    final JSONArray json;

    public ApptransidLogData(JSONArray json) {
        super(TrackerType.APPTRANSID_TYPE);
        this.json = json;
    }

    @Override
    public String getMessage() {
        try {
            for (int i = 0; i < json.length(); i++) {
                JSONObject object = (JSONObject) json.get(i);
                object.put("type", eventType);
            }
        } catch (JSONException ignore) {
        }

        return json.toString();
    }
}
