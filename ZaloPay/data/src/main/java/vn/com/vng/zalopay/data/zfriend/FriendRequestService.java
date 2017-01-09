package vn.com.vng.zalopay.data.zfriend;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.exception.GetZaloFriendException;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.RequestService
 */
public class FriendRequestService implements FriendStore.ZaloRequestService {
    private final int OFFSET_FRIEND_LIST = 50;

    private FriendStore.SDKApi mSDKApi;

    public FriendRequestService(FriendStore.SDKApi SDKApi) {
        mSDKApi = SDKApi;
    }

    private List<ZaloUserEntity> zaloFriends(final JSONArray jsonArray) {
        List<ZaloUserEntity> zaloFriends = new ArrayList<>();
        if (jsonArray == null || jsonArray.length() <= 0) {
            return zaloFriends;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                ZaloUserEntity zaloFriend = new ZaloUserEntity(jsonObject);
                if (zaloFriend.userId > 0 && !TextUtils.isEmpty(zaloFriend.displayName)) {
                    zaloFriends.add(zaloFriend);
                }
            }
        }

        return zaloFriends;
    }

    @Override
    public Observable<List<ZaloUserEntity>> fetchFriendList() {
        return Observable.create(new Observable.OnSubscribe<List<ZaloUserEntity>>() {
            @Override
            public void call(final Subscriber<? super List<ZaloUserEntity>> subscriber) {
                zaloRequestFriendList(0, subscriber);
            }
        });
    }

    private void zaloRequestFriendList(final int pageIndex, final Subscriber<? super List<ZaloUserEntity>> subscriber) {
        Timber.d("zalo Request FriendList pageIndex %s ", pageIndex);
        mSDKApi.getFriendList(pageIndex, OFFSET_FRIEND_LIST, arg0 -> handleApiCallback(pageIndex, subscriber, arg0));
    }

    private void handleApiCallback(int pageIndex, Subscriber<? super List<ZaloUserEntity>> subscriber, JSONObject arg0) {
        if (arg0 == null) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(new GetZaloFriendException(pageIndex, new NullPointerException()));
            }
            return;
        }
        
        JSONArray data = arg0.optJSONArray("result");

        if (subscriber.isUnsubscribed()) {
            Timber.d("Subscriber is unsubscribed");
            return;
        }

        if (data == null || data.length() <= 0) {
            Timber.d("Emit Completed on empty response");
            subscriber.onCompleted();
        } else {
            List<ZaloUserEntity> zaloFriends = zaloFriends(data);
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
