package vn.zalopay.feedback.collectors;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONException;
import org.json.JSONObject;

import vn.zalopay.feedback.CollectorSetting;
import vn.zalopay.feedback.IFeedbackCollector;

/**
 * Created by khattn on 12/27/16.
 * Collect app information
 */

public class AppCollector implements IFeedbackCollector {
    private Context mContext;

    private static CollectorSetting sSetting;

    static {
        sSetting = new CollectorSetting();
        sSetting.userVisibility = true;
        sSetting.displayName = "Application Information";
        sSetting.dataKeyName = "appinfo";
    }

    public AppCollector(Context context) {
        mContext = context;
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
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            JSONObject retVal = new JSONObject();
            retVal.put("version", pInfo.versionName);
            retVal.put("build_number", pInfo.versionCode);

            return retVal;
        } catch (JSONException e) {
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void cleanUp() {

    }
}
