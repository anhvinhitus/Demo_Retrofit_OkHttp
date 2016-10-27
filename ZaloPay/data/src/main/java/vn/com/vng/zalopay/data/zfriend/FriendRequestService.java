package vn.com.vng.zalopay.data.zfriend;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.api.entity.ZaloFriendEntity;
import vn.com.vng.zalopay.data.exception.GetZaloFriendException;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.RequestService
 */
public class FriendRequestService implements FriendStore.RequestService {
    private final int OFFSET_FRIEND_LIST = 50;

    FriendStore.SDKApi mSDKApi;

    public FriendRequestService(FriendStore.SDKApi SDKApi) {
        mSDKApi = SDKApi;
    }

    private List<ZaloFriendEntity> zaloFriends(final JSONArray jsonArray) {
        List<ZaloFriendEntity> zaloFriends = new ArrayList<>();
        if (jsonArray == null || jsonArray.length() <= 0) {
            return zaloFriends;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                ZaloFriendEntity zaloFriend = new ZaloFriendEntity(jsonObject);
                if (zaloFriend.userId > 0 && !TextUtils.isEmpty(zaloFriend.displayName)) {
                    zaloFriends.add(zaloFriend);
                }
            }
        }

        return zaloFriends;
    }

    @Override
    public Observable<List<ZaloFriendEntity>> fetchFriendList() {
        Timber.d("fetchFriendList pageIndex:%s", 0);

        return Observable.create(new Observable.OnSubscribe<List<ZaloFriendEntity>>() {
            @Override
            public void call(final Subscriber<? super List<ZaloFriendEntity>> subscriber) {
                zaloRequestFriendList(0, subscriber);
            }
        });
    }

    private void zaloRequestFriendList(final int pageIndex, final Subscriber<? super List<ZaloFriendEntity>> subscriber) {
        mSDKApi.getFriendList(pageIndex, OFFSET_FRIEND_LIST, arg0 -> handleApiCallback(pageIndex, subscriber, arg0));
    }

    private void handleApiCallback(int pageIndex, Subscriber<? super List<ZaloFriendEntity>> subscriber, JSONObject arg0) {
        JSONArray data;
        if (arg0 == null) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(new GetZaloFriendException(pageIndex, new NullPointerException()));
            }
            return;
        }
        data = arg0.optJSONArray("result");

        if (subscriber.isUnsubscribed()) {
            Timber.d("Subscriber is unsubscribed");
            return;
        }

        if (data == null || data.length() <= 0) {
            Timber.d("Emit Completed on empty response");
            subscriber.onCompleted();
        } else {
            List<ZaloFriendEntity> zaloFriends = zaloFriends(data);
            Timber.d("Emit Next on response");
            subscriber.onNext(zaloFriends);

            if (data.length() >= OFFSET_FRIEND_LIST) {
                Timber.d("Request next batch of friend list");
                zaloRequestFriendList((pageIndex + OFFSET_FRIEND_LIST), subscriber);
            } else {
                Timber.d("Emit Completed on not full response");
                subscriber.onCompleted();
            }
        }
    }
}
