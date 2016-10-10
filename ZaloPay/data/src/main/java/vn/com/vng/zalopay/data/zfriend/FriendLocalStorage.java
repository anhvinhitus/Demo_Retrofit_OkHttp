package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.com.vng.zalopay.data.api.entity.ZaloFriendEntity;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGDDao;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.LocalStorage
 */
public class FriendLocalStorage extends SqlBaseScopeImpl implements FriendStore.LocalStorage {
    private final ZaloFriendGDDao mDao;

    public FriendLocalStorage(DaoSession daoSession) {
        super(daoSession);
        mDao = daoSession.getZaloFriendGDDao();
    }

    @Override
    public void put(List<ZaloFriendEntity> val) {
        List<ZaloFriendGD> list = transform(val);
        mDao.insertOrReplaceInTx(list);
    }

    @Override
    public void put(ZaloFriendEntity val) {
        ZaloFriendGD item = transform(val);
        if (item != null) {
            mDao.insertOrReplaceInTx(item);
        }
    }

    @Override
    public boolean isHaveZaloFriendDb() {
        return mDao.count() > 0;
    }

    @Override
    public Cursor zaloFriendList() {
        return mDao.queryBuilder()
                .orderDesc(ZaloFriendGDDao.Properties.UsingApp)
                .orderAsc(ZaloFriendGDDao.Properties.Fulltextsearch)
                .buildCursor()
                .forCurrentThread()
                .query();
    }

    @Override
    public Cursor searchZaloFriendList(String s) {
        return mDao.queryBuilder()
                .where(ZaloFriendGDDao.Properties.Fulltextsearch.like("%" + Strings.stripAccents(s).toLowerCase() + "%"))
                .orderDesc(ZaloFriendGDDao.Properties.UsingApp)
                .orderAsc(ZaloFriendGDDao.Properties.Fulltextsearch)
                .buildCursor()
                .forCurrentThread()
                .query();
    }

    @Override
    public List<ZaloFriendEntity> get() {
        List<ZaloFriendGD> list = mDao.queryBuilder()
                .orderAsc(ZaloFriendGDDao.Properties.Fulltextsearch)
                .list();
        return transformEntity(list);
    }

    private ZaloFriendGD transform(ZaloFriendEntity entity) {
        ZaloFriendGD ret = new ZaloFriendGD(entity.getUserId());
        ret.setUserName(entity.userName);
        ret.setDisplayName(entity.displayName);
        ret.setAvatar(entity.avatar);
        ret.setFulltextsearch(entity.normalizeDisplayName);
        ret.setUsingApp(entity.usingApp);
        return ret;
    }

    private ZaloFriendEntity transform(ZaloFriendGD entity) {
        ZaloFriendEntity ret = new ZaloFriendEntity();
        ret.userId = entity.getId();
        ret.userName = entity.getUserName();
        ret.displayName = entity.getDisplayName();
        ret.avatar = entity.getAvatar();
        ret.normalizeDisplayName = entity.getFulltextsearch();
        ret.usingApp = entity.getUsingApp();
        return ret;
    }

    private List<ZaloFriendGD> transform(List<ZaloFriendEntity> entities) {
        if (Lists.isEmptyOrNull(entities)) {
            return Collections.emptyList();
        }
        List<ZaloFriendGD> list = new ArrayList<>();
        for (ZaloFriendEntity entity : entities) {
            ZaloFriendGD dao = transform(entity);
            if (dao != null) {
                list.add(dao);
            }
        }
        return list;
    }

    private List<ZaloFriendEntity> transformEntity(List<ZaloFriendGD> entities) {
        if (Lists.isEmptyOrNull(entities)) {
            return Collections.emptyList();
        }
        List<ZaloFriendEntity> list = new ArrayList<>();
        for (ZaloFriendGD dao : entities) {
            ZaloFriendEntity entity = transform(dao);
            if (dao != null) {
                list.add(entity);
            }
        }
        return list;
    }
}
