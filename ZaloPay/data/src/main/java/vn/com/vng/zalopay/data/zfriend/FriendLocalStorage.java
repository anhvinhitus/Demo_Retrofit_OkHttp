package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.UserExistEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloFriendEntity;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGDDao;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.User;

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
        List<ZaloFriendGD> list = new ArrayList<>();

        for (ZaloFriendEntity entity : val) {
            ZaloFriendGD item = transform(entity);
            if (item != null) {
                list.add(item);
            }
        }

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
        ZaloFriendGD item = mDao.load(entity.userId);
        if (item == null) {
            item = new ZaloFriendGD();
            item.setZaloId(entity.userId);
            item.setZaloPayId(entity.zaloPayId);
            item.setPhoneNumber(entity.numberPhone);
            item.setStatus(entity.status);
            item.setZaloPayName(entity.zaloPayName);
        }

        item.setUserName(entity.userName);
        item.setDisplayName(entity.displayName);
        item.setAvatar(entity.avatar);
        item.setFulltextsearch(entity.normalizeDisplayName);
        item.setUsingApp(entity.usingApp);

        return item;
    }

    private ZaloFriendEntity transform(ZaloFriendGD entity) {
        ZaloFriendEntity ret = new ZaloFriendEntity();
        ret.userId = entity.getZaloId();
        ret.userName = entity.getUserName();
        ret.displayName = entity.getDisplayName();
        ret.avatar = entity.getAvatar();
        ret.normalizeDisplayName = entity.getFulltextsearch();
        ret.usingApp = entity.getUsingApp();

        ret.zaloPayId = entity.getZaloPayId();
        ret.numberPhone = entity.getPhoneNumber() == null ? 0 : entity.getPhoneNumber();
        ret.zaloPayName = entity.getZaloPayName();
        ret.status = entity.getStatus() == null ? 0 : entity.getStatus();
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

    @Override
    public void mergeZaloPayId(@Nullable List<UserExistEntity> list) {
        if (Lists.isEmptyOrNull(list)) {
            return;
        }

        ArrayList<ZaloFriendGD> listUserDb = new ArrayList<>();
        for (UserExistEntity entity : list) {
            ZaloFriendGD zaloFriendGD = getZFriendDb(entity.zaloid);
            if (zaloFriendGD != null) {
                zaloFriendGD.setStatus(entity.status);
                zaloFriendGD.setPhoneNumber(entity.phonenumber);
                zaloFriendGD.setZaloPayId(entity.userid);
                zaloFriendGD.setZaloPayName(entity.zalopayname);
                listUserDb.add(zaloFriendGD);
            }
        }
        Timber.d("merge ZaloPay Id %s", listUserDb.size());
        mDao.insertOrReplaceInTx(listUserDb);
    }

    private ZaloFriendGD getZFriendDb(String zId) {
        long zaloId = -1;
        try {
            zaloId = Long.valueOf(zId);
        } catch (NumberFormatException e) {
            //empty
        }
        return mDao.load(zaloId);
    }

    @Override
    public List<ZaloFriendEntity> getZaloFriendWithoutZpId() {
        List<ZaloFriendGD> list = listFriendUsingAppWithoutZpId();
        return transformEntity(list);
    }

    @Override
    public List<ZaloFriendEntity> listZaloFriend(List<Long> list) {
        List<ZaloFriendGD> ret = listFriendUsingApp(list);
        return transformEntity(ret);
    }

    private List<ZaloFriendGD> listFriendUsingAppWithoutZpId() {
        return mDao.queryBuilder().where(ZaloFriendGDDao.Properties.UsingApp.eq(true),
                ZaloFriendGDDao.Properties.ZaloPayId.isNull())
                .build()
                .list();
    }

    private List<ZaloFriendGD> listFriendUsingApp(List<Long> zaloIds) {
        return mDao.queryBuilder().where(ZaloFriendGDDao.Properties.UsingApp.eq(true),
                ZaloFriendGDDao.Properties.ZaloId.in(zaloIds))
                .build()
                .list();
    }

}
