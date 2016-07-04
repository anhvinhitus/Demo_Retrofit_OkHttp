package vn.com.vng.zalopay.transfer;

/**
 * Created by huuhoa on 7/4/16.
 * Declaration for FriendStore.Repository
 */
public interface FriendStoreRepository {
    void retrieveZaloFriendsAsNeeded(FriendRepository.IZaloFriendListener listener);

    void fetchListFromServer(FriendRepository.IZaloFriendListener listener);
}
