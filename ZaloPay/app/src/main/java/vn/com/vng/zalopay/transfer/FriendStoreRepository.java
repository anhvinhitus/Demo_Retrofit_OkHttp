package vn.com.vng.zalopay.transfer;

import android.database.Cursor;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Declaration for FriendStore.Repository
 */
public interface FriendStoreRepository {
    Observable<List<ZaloFriend>> retrieveZaloFriendsAsNeeded();
    Observable<List<ZaloFriend>> fetchListFromServer();

    ZaloFriend getZaloFriendFrom(Cursor cursor);
}
