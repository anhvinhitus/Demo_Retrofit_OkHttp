package vn.com.vng.zalopay.utils;

import android.content.Context;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

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

        final Subscription mTimeoutSubscription = timeoutSubscription();

        mCallback = new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject profile) {
                try {
                    Timber.d("Got Zalo Profile: %s", profile);
                    userConfig.saveZaloUserInfo(profile);
                } catch (Exception ex) {
                    Timber.w(ex, " Exception :");
                }
                mTimeoutSubscription.unsubscribe();
                mCallback = null;
            }
        };

        ZaloSDK.Instance.getProfile(context, mCallback);
    }

    private static Subscription timeoutSubscription() {
        return Observable.timer(5, TimeUnit.SECONDS)
                .subscribe(new DefaultSubscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        mCallback = null;
                    }
                });
    }
}
