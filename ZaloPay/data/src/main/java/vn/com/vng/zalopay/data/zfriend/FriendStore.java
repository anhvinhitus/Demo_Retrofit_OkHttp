package vn.com.vng.zalopay.data.zfriend;

import java.util.List;

import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;

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

    }

    interface Repository {

    }
}
