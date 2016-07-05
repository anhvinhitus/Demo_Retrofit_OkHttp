package vn.com.vng.zalopay.transfer.provider;

import android.content.Context;

import vn.com.vng.zalopay.data.zfriend.FriendStore;
import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONObject;


/**
 * Created by huuhoa on 7/5/16.
 * Bridge to Zalo SDK
 */
public class ZaloFriendStoreApi implements FriendStore.SDKApi {
    private final Context mContext;

    public ZaloFriendStoreApi(Context context) {
        mContext = context;
    }

    @Override
    public void getFriendList(int pageIndex, int totalCount, final FriendStore.APICallback callback) {
        ZaloSDK.Instance.getFriendList(mContext, pageIndex, totalCount, new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject jsonObject) {
                callback.onResult(jsonObject);
            }
        });
    }
}
