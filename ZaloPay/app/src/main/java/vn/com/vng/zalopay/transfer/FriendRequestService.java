package vn.com.vng.zalopay.transfer;

import android.content.Context;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.exception.GetZaloFriendException;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.RequestService
 */
public class FriendRequestService {
    private final int OFFSET_FRIEND_LIST = 50;

    List<ZaloFriend> zaloFriends(final JSONArray jsonArray) {
        List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends = new ArrayList<>();
        if (jsonArray == null || jsonArray.length() <= 0) {
            return zaloFriends;
        }
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                vn.com.vng.zalopay.transfer.models.ZaloFriend zaloFriend = new vn.com.vng.zalopay.transfer.models.ZaloFriend(jsonObject);
                if (zaloFriend.getUserId() > 0 /*&& zaloFriend.isUsingApp()*/) {
                    zaloFriends.add(zaloFriend);
                }
            }
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        return zaloFriends;
    }

    public Observable<List<ZaloFriend>> getFriendListServer(final Context context) {
        Timber.d("getFriendListServer context:%s pageIndex:%s", context, 0);

        return Observable.create(new Observable.OnSubscribe<List<ZaloFriend>>() {
            @Override
            public void call(final Subscriber<? super List<ZaloFriend>> subscriber) {
                zaloRequestFriendList(context, 0, subscriber);
            }
        });
    }

    private void zaloRequestFriendList(final Context context, final int pageIndex, final Subscriber<? super List<ZaloFriend>> subscriber) {
        ZaloSDK.Instance.getFriendList(context, pageIndex, OFFSET_FRIEND_LIST, new ZaloOpenAPICallback() {
            @Override
            public void onResult(final JSONObject arg0) {
                JSONArray data;
                try {
                    data = arg0.getJSONArray("result");
                    Timber.d("getFriendListServer, result: %s", data.toString());
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(new GetZaloFriendException(pageIndex, e));
                    }
                    return;
                }

                if (subscriber.isUnsubscribed()) {
                    return;
                }

                if (data.length() <= 0) {
                    subscriber.onCompleted();
                } else {
                    List<ZaloFriend> zaloFriends = zaloFriends(data);
                    subscriber.onNext(zaloFriends);

                    if (data.length() >= OFFSET_FRIEND_LIST) {
                        zaloRequestFriendList(context, (pageIndex + OFFSET_FRIEND_LIST), subscriber);
                    } else {
                        subscriber.onCompleted();
                    }
                }
            }
        });
    }
}
