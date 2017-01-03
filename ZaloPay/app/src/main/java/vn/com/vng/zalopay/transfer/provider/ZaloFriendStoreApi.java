package vn.com.vng.zalopay.transfer.provider;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import timber.log.Timber;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.fingerprint.FingerprintUiHelper;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Created by huuhoa on 7/5/16.
 * Bridge to Zalo SDK
 */
public class ZaloFriendStoreApi implements FriendStore.SDKApi {
    private final Context mContext;
    private final ThreadExecutor mThreadExecutor;
    private final List<FriendStore.APICallback> mListCallback;

    public ZaloFriendStoreApi(Context context, ThreadExecutor threadExecutor) {
        mContext = context;
        mThreadExecutor = threadExecutor;
        mListCallback = new ArrayList<>();
    }

    @Override
    public void getFriendList(int pageIndex, int totalCount, final FriendStore.APICallback callback) {
        Timber.d("getFriendList: pageIndex %s totalCount %s callback %s", pageIndex, totalCount, callback);
        mListCallback.add(callback);
        ZaloSDK.Instance.getFriendList(mContext, pageIndex, totalCount, mZaloOpenAPICallback);
    }

    private void handleResult(final JSONObject data) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    postResult(data);
                }
            });
        } else {
            postResult(data);
        }
    }

    private void postResult(JSONObject data) {
        Timber.d("post result %s data : %s", mListCallback.size(), data.toString());
        synchronized (mListCallback) {
            if (mListCallback.size() > 0) {
                FriendStore.APICallback callback = mListCallback.get(0);
                if (callback != null) {
                    callback.onResult(data);
                }
                mListCallback.remove(callback);
            }
        }
    }

    private ZaloOpenAPICallback mZaloOpenAPICallback = new ZaloOpenAPICallback() {
        @Override
        public void onResult(JSONObject jsonObject) {
            handleResult(jsonObject);
        }
    };
}
