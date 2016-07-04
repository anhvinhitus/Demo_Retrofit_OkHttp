package vn.com.vng.zalopay.transfer;

import android.database.Cursor;

import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Declaration for FriendStore.Repository
 */
public interface FriendStoreRepository {
    void retrieveZaloFriendsAsNeeded(FriendRepository.IZaloFriendListener listener);

    void fetchListFromServer(FriendRepository.IZaloFriendListener listener);

    public ZaloFriend getZaloFriendFrom(Cursor cursor);
}
