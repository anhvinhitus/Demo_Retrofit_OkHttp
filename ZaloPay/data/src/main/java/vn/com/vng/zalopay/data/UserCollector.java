package vn.com.vng.zalopay.data;

import org.json.JSONException;
import org.json.JSONObject;

import vn.zalopay.feedback.CollectorSetting;
import vn.zalopay.feedback.IFeedbackCollector;

import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by khattn on 12/27/16.
 * User collector
 */

public class UserCollector implements IFeedbackCollector {
    private UserConfig mUserConfig;

    private static CollectorSetting sSetting;

    static {
        sSetting = new CollectorSetting();
        sSetting.userVisibility = true;
        sSetting.displayName = "User Information";
        sSetting.dataKeyName = "userinfo";
    }

    public UserCollector(UserConfig userConfig) {
        mUserConfig = userConfig;
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

            if (!mUserConfig.hasCurrentUser()) {
                return null;
            }

            User user = mUserConfig.getCurrentUser();

            JSONObject retVal = new JSONObject();
            retVal.put("zpid", user.zaloPayId);
            retVal.put("zaloid", user.zaloId);
            retVal.put("display_name", user.displayName);
            retVal.put("avatar", user.avatar);
            retVal.put("zalopayid", user.zalopayname);

            return retVal;
        } catch (Exception e) {
            return null;
        }
    }
}
