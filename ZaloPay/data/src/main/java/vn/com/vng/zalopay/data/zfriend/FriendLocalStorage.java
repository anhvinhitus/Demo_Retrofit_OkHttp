package vn.com.vng.zalopay.data.zfriend;

import java.util.List;

import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGDDao;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.LocalStorage
 */
public class FriendLocalStorage implements FriendStore.LocalStorage {
    private final ZaloFriendGDDao mDao;

    public FriendLocalStorage(DaoSession daoSession) {
        mDao = daoSession.getZaloFriendGDDao();
    }

    @Override
    public void put(List<ZaloFriendGD> val) {
        mDao.insertOrReplaceInTx(val);
    }

    @Override
    public void writeZaloFriend(ZaloFriendGD val) {
        mDao.insertOrReplaceInTx(val);
    }

    @Override
    public List<ZaloFriendGD> listZaloFriend() {
        return mDao.queryBuilder().where(ZaloFriendGDDao.Properties.UsingApp.eq("true")).list();
    }

    @Override
    public List<ZaloFriendGD> listZaloFriend(int limit) {
        return mDao.queryBuilder().where(ZaloFriendGDDao.Properties.UsingApp.eq("true")).limit(limit).list();
    }

    @Override
    public boolean isHaveZaloFriendDb() {
        return mDao.queryBuilder().count() > 0;
    }
}
