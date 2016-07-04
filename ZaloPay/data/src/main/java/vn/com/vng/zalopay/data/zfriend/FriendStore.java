package vn.com.vng.zalopay.data.zfriend;

import java.util.List;

import vn.com.vng.zalopay.data.cache.model.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Declaration for friend local storage, friend request service, friend repository
 */
public interface FriendStore {
    interface LocalStorage {
        boolean isHaveZaloFriendDb();

        void writeZaloFriends(List<ZaloFriend> val);

        void writeZaloFriend(ZaloFriend val);

        List<ZaloFriend> listZaloFriend();

        List<ZaloFriend> listZaloFriend(int limit);
    }

    interface RequestService {

    }

    interface Repository {

    }
}
