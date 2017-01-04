package vn.com.vng.zalopay.utils;

import android.content.Context;
import android.os.CountDownTimer;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;

/**
 * Created by huuhoa on 12/11/16.
 * Call to Zalo SDK
 */

public class ZaloHelper {
    private static ZaloOpenAPICallback mCallback = null;


    public static void getZaloProfileInfo(final Context context, final UserConfig userConfig) {
        Timber.d("request Zalo Profile");
        if (mCallback != null) {
            Timber.w("Get Zalo Profile is already in progress");
            return;
        }

        startTimeout(5000);

        mCallback = new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject profile) {
                try {
                    Timber.d("Got Zalo Profile: %s", profile);
                    userConfig.saveZaloUserInfo(profile);
                } catch (Exception ex) {
                    Timber.w(ex, " Exception :");
                }

                stopTimeout();
                mCallback = null;
            }
        };

        ZaloSDK.Instance.getProfile(context, mCallback);
    }

    private static CountDownTimer mCountDownTimer;

    private static void startTimeout(long timeMillis) {
        stopTimeout();
        mCountDownTimer = new CountDownTimer(timeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Timber.d("Get zalo profile timeout");
                mCallback = null;
            }
        };
        mCountDownTimer.start();
    }

    private static void stopTimeout() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }

    }
}
