package vn.zalopay.feedback.collectors;

import org.json.JSONException;
import org.json.JSONObject;

import vn.zalopay.feedback.CollectorSetting;
import vn.zalopay.feedback.IFeedbackCollector;

/**
 * Created by khattn on 26/12/2016.
 */

public class DynamicCollector implements IFeedbackCollector {
    private JSONObject mRetVal = new JSONObject();

    private static CollectorSetting sSetting;
    static {
        sSetting = new CollectorSetting();
        sSetting.userVisibility = true;
        sSetting.displayName = "Dynamic Information";
        sSetting.dataKeyName = "dynamicinfo";
    }

    public void put(String name, Object value) throws JSONException {
        mRetVal.put(name, value);
    }

    /**
     * Get pre-config settings for data collector
     */
    @Override
    public CollectorSetting getSetting() {
        return sSetting;
    }

    /**
     * Start collecting data. If data is collected, then return JSONObject of the encoded data
     *
     * @return JSONObject value, null if data is not collected
     */
    @Override
    public JSONObject doInBackground() {
        return mRetVal;
    }
}
