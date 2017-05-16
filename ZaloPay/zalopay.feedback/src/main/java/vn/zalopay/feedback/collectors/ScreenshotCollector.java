package vn.zalopay.feedback.collectors;

import android.support.annotation.Nullable;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import vn.zalopay.feedback.CollectorSetting;
import vn.zalopay.feedback.IFeedbackCollector;

/**
 * Created by khattn on 26/12/2016.
 */

public class ScreenshotCollector implements IFeedbackCollector {

    private static CollectorSetting sSetting;

    static {
        sSetting = new CollectorSetting();
        sSetting.userVisibility = true;
        sSetting.displayName = "Screenshot Information";
        sSetting.dataKeyName = "screenshot";
    }

    @Nullable
    public byte[] mScreenshot;

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
    public JSONObject doInBackground() throws JSONException {

        /*if (mScreenshot == null) {
            return null;
        }

        JSONObject retVal = new JSONObject();
        retVal.put("image", Base64.encode(mScreenshot, Base64.DEFAULT));
        return retVal;*/
        
        return null;
    }

    public ScreenshotCollector() {
    }

    @Override
    public void cleanUp() {
        mScreenshot = null;
    }
}
