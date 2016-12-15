package vn.zalopay.feedback;

import org.json.JSONObject;

/**
 * Created by huuhoa on 12/15/16.
 * Interface for defining data collector
 */

public interface IFeedbackCollector {
    /**
     * Get pre-config settings for data collector
     */
    CollectorSetting getSetting();

    /**
     * Start collecting data. If data is collected, then return JSONObject of the encoded data
     * @return JSONObject value, null if data is not collected
     */
    JSONObject doInBackground();
}
