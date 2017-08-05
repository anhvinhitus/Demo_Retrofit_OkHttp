package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.FavoriteEntity;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.FavoriteZPC;
import vn.com.vng.zalopay.data.cache.model.FavoriteZPCDao;
import vn.com.vng.zalopay.data.cache.model.UCB;
import vn.com.vng.zalopay.data.cache.model.UCBDao;
import vn.com.vng.zalopay.data.cache.model.ZFL;
import vn.com.vng.zalopay.data.cache.model.ZFLDao;
import vn.com.vng.zalopay.data.cache.model.ZPC;
import vn.com.vng.zalopay.data.cache.model.ZPCDao;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.contactloader.Contact;

/**
 * Created by hieuvm on 7/20/17.
 * *
 */

public class ZPCLocalStorage extends SqlBaseScopeImpl implements ZPCStore.LocalStorage {

    private final ZPCDao mZPCDao;
    private final ZFLDao mZFLDao;
    private final UCBDao mUCBDao;
    private final ZPCMapper mMapper;
    private final FavoriteZPCDao mFavoriteZPCDao;

    public ZPCLocalStorage(DaoSession daoSession) {
        super(daoSession);
        mZFLDao = daoSession.getZFLDao();
        mZPCDao = daoSession.getZPCDao();
        mUCBDao = daoSession.getUCBDao();
        mMapper = new ZPCMapper();
        mFavoriteZPCDao = daoSession.getFavoriteZPCDao();
    }

    @Override
    public void putContacts(@Nullable List<Contact> contacts) {
        List<UCB> list = mMapper.transform(contacts);
        if (Lists.isEmptyOrNull(list)) {
            return;
        }

        mUCBDao.insertOrReplaceInTx(list);
    }

    @Override
    public void putZaloUser(List<ZaloUserEntity> val) {
        List<ZFL> list = Lists.transform(val, mMapper::transform);

        if (Lists.isEmptyOrNull(list)) {
            return;
        }

        mZFLDao.insertOrReplaceInTx(list);
    }

    @Override
    public void putZaloPayUser(@Nullable List<ZaloPayUserEntity> entities) {
        List<ZPC> list = Lists.transform(entities, mMapper::transform);
        if (Lists.isEmptyOrNull(list)) {
            return;
        }
        mZPCDao.insertOrReplaceInTx(list);
    }

    @Nullable
    @Override
    public ZaloPayUserEntity getZaloPayUserByZaloId(long zaloId) {
        ZPC zpc = mZPCDao.queryBuilder()
                .where(ZPCDao.Properties.ZaloId.eq(zaloId))
                .unique();
        return mMapper.transform(zpc);
    }

    @NonNull
    @Override
    public List<RedPacketUserEntity> getRedPacketUsersEntity(List<Long> zaloids) {
        List<ZPC> listZPC = mZPCDao.queryBuilder()
                .where(ZPCDao.Properties.ZaloId.in(zaloids))
                .list();

        if (Lists.isEmptyOrNull(listZPC)) {
            return Collections.emptyList();
        }

        List<ZFL> listZFL = mZFLDao.queryBuilder()
                .where(ZFLDao.Properties.ZaloId.in(zaloids))
                .list();

        if (!Lists.isEmptyOrNull(listZFL)) {
            mMapper.replaceNameZFLToNameZPC(listZPC, listZFL);
        }

        listZFL.clear();
        return Lists.transform(listZPC, mMapper::transformToRedPacket);
    }

    @Override
    public Cursor getZaloUserCursor(boolean enableContact, boolean isWithPhone) {
        return findFriends("", enableContact, isWithPhone);
    }

    @Override
    public Cursor findFriends(String s, boolean enableContact, boolean isWithPhone) {
        String sql = ZPCSqlHelper.createSqlContactQuery(s, enableContact, isWithPhone);
        return getDaoSession().getDatabase().rawQuery(sql, null);
    }

    @NonNull
    @Override
    public List<Long> getZaloUserWithoutZaloPayId() {
        List<ZFL> listUsingApp = mZFLDao.queryBuilder()
                .where(ZFLDao.Properties.UsingApp.eq(true))
                .list();

        List<Long> listIdUsingApp = Lists.transform(listUsingApp, zfl -> zfl.zaloId);

        if (Lists.isEmptyOrNull(listIdUsingApp)) {
            return Collections.emptyList();
        }

        List<ZPC> listZPC = mZPCDao.queryBuilder()
                .where(ZPCDao.Properties.ZaloId.in(listIdUsingApp))
                .list();
        List<Long> listIdZPC = Lists.transform(listZPC, zpc -> zpc.zaloId);

        if (Lists.isEmptyOrNull(listIdZPC)) {
            return listIdUsingApp;
        }

        listIdUsingApp.removeAll(listIdZPC);

        listUsingApp.clear();
        listZPC.clear();
        return listIdUsingApp;
    }

    @Override
    public long getLastTimeSyncContact() {
        return getDataManifest(Constants.MANIFEST_LASTTIME_SYNC_CONTACT, 0);
    }

    @Override
    public void setLastTimeSyncContact(long time) {
        insertDataManifest(Constants.MANIFEST_LASTTIME_SYNC_CONTACT, String.valueOf(time));
    }

    @Override
    public boolean addFavorite(String phoneNumber, long zaloId) {

        //Timber.d("addFavorite [phone:%s, zaloid:%s]", phoneNumber, zaloid);
        FavoriteZPC zpc = mMapper.create(phoneNumber, zaloId);
        if (zpc == null) {
            return false;
        }

        try {
            long rowId = mFavoriteZPCDao.insert(zpc);
            //Timber.d("addFavorite: [rowId:%s]", rowId);
            return true;
        } catch (Exception ignore) {
            //Timber.d(ignore);
        }
        return false;
    }

    @Override
    public boolean removeFavorite(String phoneNumber, long zaloid) {

        WhereCondition deleteCondition = null;
        if (!TextUtils.isEmpty(phoneNumber)) {
            deleteCondition = FavoriteZPCDao.Properties.PhoneNumber.eq(phoneNumber);
        } else if (zaloid > 0) {
            deleteCondition = FavoriteZPCDao.Properties.ZaloId.eq(zaloid);
        }

        if (deleteCondition == null) {
            return false;
        }

        //Timber.d("removeFavorite [phone:%s, zaloid:%s]", phoneNumber, zaloid);

        mFavoriteZPCDao.queryBuilder()
                .where(deleteCondition)
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
        return true;
    }

    @Override
    public long getUserContactBookCount() {
        return mUCBDao.count();
    }

    @Override
    public long getZaloFriendListCount() {
        return mZFLDao.count();
    }

    @Override
    public List<String> getAvatarContacts(int limit) {
        List<UCB> contacts = mUCBDao.queryBuilder()
                .where(UCBDao.Properties.PhotoUri.isNotNull())
                .limit(limit)
                .list();

        return Lists.transform(contacts, ucb -> ucb.photoUri);
    }

    @Override
    public List<String> getAvatarZaloFriends(int limit) {
        List<ZFL> friends = mZFLDao.queryBuilder()
                .where(ZFLDao.Properties.Avatar.isNotNull())
                .limit(limit)
                .list();

        return Lists.transform(friends, zfl -> zfl.avatar);
    }

    @Override
    public List<FavoriteEntity> getFavorites(int limit) {
        List<FavoriteZPC> list = mFavoriteZPCDao.queryBuilder()
                .limit(limit)
                .orderAsc(FavoriteZPCDao.Properties.CreateTime)
                .list();
        if (Lists.isEmptyOrNull(list)) {
            return Collections.emptyList();
        }

        List<String> listPhone = Lists.transform(list, favorite -> "\'" + favorite.phoneNumber + "\'");
        String phones = Strings.joinWithDelimiter(",", listPhone);

        List<Long> listZaloIds = Lists.transform(list, favorite -> favorite.zaloId == 0 ? null : favorite.zaloId);
        String zaloIds = Strings.joinWithDelimiter(",", listZaloIds);

        return getFavorites(phones, zaloIds);
    }

    private List<FavoriteEntity> getFavorites(String phones, String zaloIds) {
        if (TextUtils.isEmpty(phones) && TextUtils.isEmpty(zaloIds)) {
            return Collections.emptyList();
        }

        String sql = ZPCSqlHelper.createSqlContactQuery(phones, zaloIds, true, false);

        Cursor cursor = null;
        try {
            cursor = getDaoSession().getDatabase().rawQuery(sql, null);

            if (cursor == null || cursor.getCount() == 0) {
                //Timber.d("favorite cursor is empty.");
                return Collections.emptyList();
            }

            if (!cursor.moveToFirst()) {
                //Timber.d("Move to first : favorite cursor is empty.");
                return Collections.emptyList();
            }

            //Timber.d("favorite cursor [count:%s]", cursor.getCount());

            List<FavoriteEntity> ret = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                FavoriteEntity entity = mMapper.transform(cursor);
                if (entity == null) {
                    continue;
                }

                ret.add(entity);
                cursor.moveToNext();
            }
            //Timber.d("getFavorites: result %s", ret.size());
            return ret;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
