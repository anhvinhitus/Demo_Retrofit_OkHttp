package vn.com.vng.zalopay.data.zfriend;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.exception.GetZaloFriendException;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

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

    private List<ZaloFriend> zaloFriends(final JSONArray jsonArray) {
        List<ZaloFriend> zaloFriends = new ArrayList<>();
        if (jsonArray == null || jsonArray.length() <= 0) {
            return zaloFriends;
        }
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ZaloFriend zaloFriend = new ZaloFriend(jsonObject);
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

    public Observable<List<ZaloFriend>> fetchFriendList() {
        Timber.d("fetchFriendList pageIndex:%s", 0);

        return Observable.create(new Observable.OnSubscribe<List<ZaloFriend>>() {
            @Override
            public void call(final Subscriber<? super List<ZaloFriend>> subscriber) {
                zaloRequestFriendList(0, subscriber);
            }
        });
    }

    private void zaloRequestFriendList(final int pageIndex, final Subscriber<? super List<ZaloFriend>> subscriber) {
        mSDKApi.getFriendList(pageIndex, OFFSET_FRIEND_LIST, arg0 -> handleApiCallback(pageIndex, subscriber, arg0));
    }

    private void handleApiCallback(int pageIndex, Subscriber<? super List<ZaloFriend>> subscriber, JSONObject arg0) {
        JSONArray data;
        try {
            data = arg0.getJSONArray("result");
            Timber.d("fetchFriendList, result: %s friends", data.length());
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
            Timber.d("Subscriber is unsubscribed");
            return;
        }

        if (data.length() <= 0) {
            Timber.d("Emit Completed on empty response");
            subscriber.onCompleted();
        } else {
            List<ZaloFriend> zaloFriends = zaloFriends(data);
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
