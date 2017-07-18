package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.internal.SqlUtils;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.FriendEntityDataMapper;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.ContactGD;
import vn.com.vng.zalopay.data.cache.model.ContactGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ZaloPayProfileGD;
import vn.com.vng.zalopay.data.cache.model.ZaloPayProfileGDDao;
import vn.com.vng.zalopay.data.cache.model.ZaloProfileGD;
import vn.com.vng.zalopay.data.cache.model.ZaloProfileGDDao;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.contactloader.Contact;

import static vn.com.vng.zalopay.data.Constants.MANIFEST_LASTTIME_SYNC_CONTACT;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.LocalStorage
 */

public class FriendLocalStorage extends SqlBaseScopeImpl implements FriendStore.LocalStorage {

    private ZaloProfileGDDao mZaloUserDao;
    private ZaloPayProfileGDDao mZaloPayUserDao;
    private ContactGDDao mContactDao;

    private FriendEntityDataMapper mDataMapper;

    public FriendLocalStorage(DaoSession daoSession) {
        super(daoSession);
        this.mZaloUserDao = getDaoSession().getZaloProfileGDDao();
        this.mZaloPayUserDao = getDaoSession().getZaloPayProfileGDDao();
        this.mContactDao = getDaoSession().getContactGDDao();
        this.mDataMapper = new FriendEntityDataMapper();
    }

    @Override
    public void putZaloUser(List<ZaloUserEntity> val) {
        Timber.d("Put list zalo user size [%s]", val.size());
        List<ZaloProfileGD> list = mDataMapper.transformZaloUser(val);
        if (!Lists.isEmptyOrNull(list)) {
            mZaloUserDao.insertOrReplaceInTx(list);
        }
    }

    @Override
    public void putZaloPayUser(@Nullable List<ZaloPayUserEntity> entities) {
        Timber.d("Put list zalopay user size [%s]", entities == null ? 0 : entities.size());
        List<ZaloPayProfileGD> list = mDataMapper.transformZaloPayUser(entities);
        if (!Lists.isEmptyOrNull(list)) {
            mZaloPayUserDao.insertOrReplaceInTx(list);
        }
    }

    @Override
    public void putContacts(List<Contact> contacts) {
        Timber.d("Put list contact size [%s]", contacts == null ? 0 : contacts.size());
        List<ContactGD> list = mDataMapper.transformContact(contacts);
        if (!Lists.isEmptyOrNull(list)) {
            mContactDao.insertOrReplaceInTx(list);
        }
    }

    /**
     * Trả về list user zalo using-app và đã có zalopayID
     */
    @NonNull
    @Override
    public List<RedPacketUserEntity> getRedPacketUsersEntity(List<Long> zaloids) {
        String strZaloIds = Strings.joinWithDelimiter(",", zaloids);
        List<ZaloProfileGD> list = mZaloUserDao.queryDeep("WHERE T.\"" + ZaloProfileGDDao.Properties.ZaloId.columnName + "\" IN (" + strZaloIds + ") AND T.\""
                + ZaloProfileGDDao.Properties.UsingApp.columnName + "\" = 1 AND T0.\""
                + ZaloPayProfileGDDao.Properties.ZaloPayId.columnName + "\" IS NOT NULL");
        Timber.d("Get redpacket user entity size [%s]", list.size());
        return mDataMapper.transformRedPacketEntity(list);
    }

    /**
     * Lấy danh sách friend zalo (close cursor ở adapter)
     */
    @Override
    public Cursor getZaloUserCursor(boolean enableContact) {
        return getDaoSession().getDatabase().rawQuery(getSelectDeep(enableContact), null);
    }

    /**
     * search friend zalo (close cursor ở adapter)
     */
    @Override
    public Cursor searchZaloFriendList(String s, boolean enableContact) {
        return getDaoSession().getDatabase().rawQuery(searchUserZalo(s, enableContact), null);
    }

    /**
     * Lấy danh sách user đã cài app. nhưng không có zalopayId;
     */
    @Override
    @NonNull
    public List<Long> getZaloUserWithoutZaloPayId() {
        List<ZaloProfileGD> list = mZaloUserDao.queryDeep("WHERE T.\""
                + ZaloProfileGDDao.Properties.UsingApp.columnName + "\" = 1 AND T0.\""
                + ZaloPayProfileGDDao.Properties.ZaloPayId.columnName + "\" IS NULL");

        Timber.d("Get zalo user without zalopayid size [%s] ", list.size());
        if (Lists.isEmptyOrNull(list)) {
            return Collections.emptyList();
        }

        return Lists.transform(list, profile -> profile.zaloId);
    }

    @Override
    public long getLastTimeSyncContact() {
        return getDataManifest(MANIFEST_LASTTIME_SYNC_CONTACT, 0);
    }

    @Override
    public void setLastTimeSyncContact(long time) {
        insertDataManifest(MANIFEST_LASTTIME_SYNC_CONTACT, String.valueOf(time));
    }

    private String selectDeep;
    private boolean enableContact;

    public String getSelectDeep(boolean enableContact) {
        if (selectDeep != null) {
            if (this.enableContact != enableContact) {
                selectDeep = null;
            }
        }
        if (selectDeep == null) {
            this.enableContact = enableContact;
            StringBuilder builder = sqlSelect(enableContact);
            builder.append(orderBy());
            selectDeep = builder.toString();
        }
        Timber.d("sql select [%s]", selectDeep);
        return selectDeep;
    }

    /**
     * ALIAS_DISPLAY_NAME : column trả về tên hiển thị, nếu tên trong contact == null thì lấy trên của zalo
     * ALIAS_FULLTEXTSEARCH : chuỗi ký tự không dấu ALIAS_DISPLAY_NAME
     */
    private StringBuilder sqlSelect(boolean enableContact) {
        StringBuilder builder = new StringBuilder("SELECT ");
        SqlUtils.appendColumns(builder, "T", mZaloUserDao.getAllColumns());
        builder.append(',');
        SqlUtils.appendColumns(builder, "T0", mZaloPayUserDao.getAllColumns());
        builder.append(',');
        SqlUtils.appendColumns(builder, "T1", mContactDao.getAllColumns());
        builder.append(',');

        if (enableContact) {
            builder.append(" IFNULL( T1.\"");
            builder.append(ContactGDDao.Properties.DisplayName.columnName);
            builder.append("\",T.\"");
            builder.append(ZaloProfileGDDao.Properties.DisplayName.columnName);
            builder.append("\") AS ");
            builder.append(ColumnIndex.ALIAS_DISPLAY_NAME);
            builder.append(',');
            builder.append(" IFNULL( T1.\"");
            builder.append(ContactGDDao.Properties.Fulltextsearch.columnName);
            builder.append("\",T.\"");
            builder.append(ZaloProfileGDDao.Properties.Fulltextsearch.columnName);
            builder.append("\") AS ");
            builder.append(ColumnIndex.ALIAS_FULL_TEXT_SEARCH);
        } else {
            builder.append(" T.");
            builder.append(ZaloProfileGDDao.Properties.DisplayName.columnName);
            builder.append(" AS ");
            builder.append(ColumnIndex.ALIAS_DISPLAY_NAME);
            builder.append(',');
            builder.append(" T.");
            builder.append(ContactGDDao.Properties.Fulltextsearch.columnName);
            builder.append(" AS ");
            builder.append(ColumnIndex.ALIAS_FULL_TEXT_SEARCH);
        }

        builder.append(" FROM ");
        builder.append(mZaloUserDao.getTablename());
        builder.append(" T");
        builder.append(" LEFT JOIN ");
        builder.append(mZaloPayUserDao.getTablename());
        builder.append(" T0");
        builder.append(" ON T.\"");
        builder.append(ZaloProfileGDDao.Properties.ZaloId.columnName);
        builder.append("\"=T0.\"");
        builder.append(ZaloPayProfileGDDao.Properties.ZaloId.columnName);
        builder.append("\"");
        builder.append(" LEFT JOIN ");
        builder.append(mContactDao.getTablename());
        builder.append(" T1");
        builder.append(" ON T0.\"");
        builder.append(ZaloPayProfileGDDao.Properties.PhoneNumber.columnName);
        builder.append("\"=T1.\"");
        builder.append(ContactGDDao.Properties.PhoneNumber.columnName);
        builder.append("\"");
        builder.append(' ');
        return builder;
    }

    private String orderBy() {
        String builder = "ORDER BY " +
                "T0." +
                ZaloPayProfileGDDao.Properties.Status.columnName +
                " DESC" +
                ", " +
                ColumnIndex.ALIAS_FULL_TEXT_SEARCH;
        return builder;
    }

    public String searchUserZalo(String key, boolean enableContact) {
        StringBuilder builder = sqlSelect(enableContact);
        builder.append("WHERE ");
        builder.append(ColumnIndex.ALIAS_FULL_TEXT_SEARCH);
        builder.append(" LIKE");
        builder.append(" '%");
        builder.append(key);
        builder.append("%'");
        builder.append(' ');
        builder.append(orderBy());
        return builder.toString();
    }


    @Nullable
    @Override
    public ZaloPayUserEntity getZaloPayUserByZaloId(long zaloId) {
        List<ZaloPayProfileGD> list = mZaloPayUserDao.queryBuilder()
                .where(ZaloPayProfileGDDao.Properties.ZaloId.eq(zaloId)).list();
        if (!Lists.isEmptyOrNull(list)) {
            return mDataMapper.transform(list.get(0));
        }
        return null;
    }
}
