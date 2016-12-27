package vn.zalopay.feedback.collectors;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.json.JSONObject;

import vn.zalopay.feedback.CollectorSetting;
import vn.zalopay.feedback.IFeedbackCollector;

/**
 * Created by khattn on 27/12/2016.
 */

public class NetworkCollector implements IFeedbackCollector {
    private Activity mActivity;

    private static CollectorSetting sSetting;
    static {
        sSetting = new CollectorSetting();
        sSetting.userVisibility = true;
        sSetting.displayName = "Network Information";
        sSetting.dataKeyName = "networkinfo";
    }

    public NetworkCollector(Activity activity) {
        mActivity = activity;
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
            TelephonyManager telephonyManager = (TelephonyManager) mActivity.getBaseContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            ConnectivityManager connectivityManager = (ConnectivityManager) mActivity
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            JSONObject retVal = new JSONObject();
            retVal.put("connection_state", networkInfo.getState());
            retVal.put("connection_type", networkInfo.getTypeName());
            retVal.put("mno", telephonyManager.getNetworkOperatorName());

            return retVal;
        } catch (JSONException e) {
            return null;
        }
    }
}
