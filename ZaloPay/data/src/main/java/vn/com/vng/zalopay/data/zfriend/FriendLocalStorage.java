package vn.com.vng.zalopay.data.zfriend;

import java.util.List;

import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ZaloFriend;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendDao;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.LocalStorage
 */
public class FriendLocalStorage implements FriendStore.LocalStorage {
    private final DaoSession mDaoSession;

    public FriendLocalStorage(DaoSession daoSession) {

        mDaoSession = daoSession;
    }
    @Override
    public void writeZaloFriends(List<ZaloFriend> val) {
        mDaoSession.getZaloFriendDao().insertOrReplaceInTx(val);
    }

    @Override
    public void writeZaloFriend(ZaloFriend val) {
        mDaoSession.getZaloFriendDao().insertOrReplaceInTx(val);
    }

    @Override
    public List<ZaloFriend> listZaloFriend() {
        return mDaoSession.getZaloFriendDao().queryBuilder().where(ZaloFriendDao.Properties.UsingApp.eq("true")).list();
    }

    @Override
    public List<ZaloFriend> listZaloFriend(int limit) {
        return mDaoSession.getZaloFriendDao().queryBuilder().where(ZaloFriendDao.Properties.UsingApp.eq("true")).limit(limit).list();
    }

    @Override
    public boolean isHaveZaloFriendDb() {
        return mDaoSession.getZaloFriendDao().queryBuilder().count() > 0;
    }
}
