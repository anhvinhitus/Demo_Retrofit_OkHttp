package vn.com.vng.zalopay.data.zfriend;

import android.content.Context;
import android.database.Cursor;

import org.json.JSONObject;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Declaration for friend local storage, friend request service, friend repository
 */
public interface FriendStore {
    interface LocalStorage {
        boolean isHaveZaloFriendDb();

        void writeZaloFriends(List<ZaloFriendGD> val);

        void writeZaloFriend(ZaloFriendGD val);

        List<ZaloFriendGD> listZaloFriend();

        List<ZaloFriendGD> listZaloFriend(int limit);
    }

    interface RequestService {
        Observable<List<ZaloFriend>> fetchFriendList();
    }

    interface APICallback {
        void onResult(JSONObject var1);
    }

    interface SDKApi {
        void getFriendList(int pageIndex, int totalCount, APICallback callback);
    }

    /**
     * Declaration for FriendStore.Repository
     */
    interface Repository {
        Observable<List<ZaloFriend>> retrieveZaloFriendsAsNeeded();
        Observable<List<ZaloFriend>> fetchListFromServer();

        ZaloFriend getZaloFriendFrom(Cursor cursor);
    }
}
