package vn.com.vng.zalopay.utils;

import android.content.Context;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;

/**
 * Created by huuhoa on 12/11/16.
 * Call to Zalo SDK
 */

public class ZaloHelper {
    public static void getZaloProfileInfo(Context context, final UserConfig userConfig) {
        ZaloSDK.Instance.getProfile(context, new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject profile) {
                try {
                    Timber.d("Got Zalo Profile: %s", profile);
                    userConfig.saveZaloUserInfo(profile);
                } catch (Exception ex) {
                    Timber.w(ex, " Exception :");
                }
            }
        });
    }
}
