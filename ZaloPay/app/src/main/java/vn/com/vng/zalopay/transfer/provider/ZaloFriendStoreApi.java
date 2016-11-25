package vn.com.vng.zalopay.transfer.provider;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import timber.log.Timber;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONObject;


/**
 * Created by huuhoa on 7/5/16.
 * Bridge to Zalo SDK
 */
public class ZaloFriendStoreApi implements FriendStore.SDKApi {
    private final Context mContext;
    private final ThreadExecutor mThreadExecutor;

    public ZaloFriendStoreApi(Context context, ThreadExecutor threadExecutor) {
        mContext = context;
        mThreadExecutor = threadExecutor;
    }

    @Override
    public void getFriendList(int pageIndex, int totalCount, final FriendStore.APICallback callback) {
        try {
            ZaloSDK.Instance.getFriendList(mContext, pageIndex, totalCount, new ZaloOpenAPICallback() {
                @Override
                public void onResult(JSONObject data) {
                    Timber.d("Current thread: %s", Thread.currentThread().getName());
                    handleResult(callback, data);
                }
            });
        } catch (Throwable t) {
            Timber.w(t, "Caught error while calling ZaloSDK");
            callback.onResult(null);
        }
    }

    private void handleResult(final FriendStore.APICallback callback, final JSONObject data) {
        if (callback == null) {
            return;
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            mThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onResult(data);
                }
            });
        } else {
            callback.onResult(data);
        }
    }
}
