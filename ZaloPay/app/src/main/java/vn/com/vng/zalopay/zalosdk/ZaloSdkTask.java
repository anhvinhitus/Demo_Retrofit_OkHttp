package vn.com.vng.zalopay.zalosdk;

import android.content.Context;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by hieuvm on 1/17/17.
 */

final class ZaloSdkTask {

    public Action action;
    public int position;
    public ZaloOpenAPICallback callback;
    private Context mContext;
    private Subscription mSubscription;
    private int totalCount = 50;


    ZaloSdkTask(Context context, Action action, int position, int totalCount, ZaloOpenAPICallback callback) {
        this.action = action;
        this.position = position;
        this.callback = callback;
        this.mContext = context;
        this.totalCount = totalCount;
    }

    ZaloSdkTask(Context context, ZaloOpenAPICallback callback) {
        this(context, Action.GET_PROFILE, 0, 0, callback);
    }

    enum Action {
        GET_PROFILE,
        GET_FRIEND
    }

    void execute() {
        if (action == Action.GET_FRIEND) {
            mSubscription = timeoutSubscription();
            ZaloSDK.Instance.getFriendList(mContext, position, totalCount, mZaloOpenAPICallback);
        } else if (action == Action.GET_PROFILE) {
            mSubscription = timeoutSubscription();
            ZaloSDK.Instance.getProfile(mContext, mZaloOpenAPICallback);
        }
    }

    private Subscription timeoutSubscription() {
        return Observable.timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Long>() {
                    @Override
                    public void onCompleted() {

                        Timber.d("zalo-sdk task timeout [action %s]", action);

                        JSONObject result = new JSONObject();
                        try {
                            result.put("timeout", 1);
                        } catch (Exception ignore) {
                            Timber.w("Get zalo profile timeout");
                        }

                        if (callback != null) {
                            callback.onResult(result);
                        }
                        callback = null;
                    }
                });
    }

    private ZaloOpenAPICallback mZaloOpenAPICallback = new ZaloOpenAPICallback() {
        @Override
        public void onResult(JSONObject jsonObject) {

            if (mSubscription != null) {
                mSubscription.unsubscribe();
            }

            if (callback != null) {
                callback.onResult(jsonObject);
            }
        }
    };
}
