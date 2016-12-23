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
    private static ZaloOpenAPICallback mCallback = null;
    public static void getZaloProfileInfo(Context context, final UserConfig userConfig) {
        Timber.d("request Zalo Profile");
        if (mCallback != null) {
            Timber.w("Get Zalo Profile is already in progress");
            return;
        }

        mCallback = new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject profile) {
                try {
                    Timber.d("Got Zalo Profile: %s", profile);
                    userConfig.saveZaloUserInfo(profile);
                } catch (Exception ex) {
                    Timber.w(ex, " Exception :");
                }

                mCallback = null;
            }
        };

        ZaloSDK.Instance.getProfile(context, mCallback);
    }
}
