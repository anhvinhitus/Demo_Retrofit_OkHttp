package vn.com.vng.zalopay.data.notification;

import android.database.sqlite.SQLiteConstraintException;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.NotificationGD;
import vn.com.vng.zalopay.data.cache.model.NotificationGDDao;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.Enums;

import static java.util.Collections.emptyList;
import static vn.com.vng.zalopay.data.Constants.MANIFEST_RECOVERY_NOTIFY;

/**
 * Created by AnhHieu on 6/20/16.
 * *
 */
public class NotificationLocalStorage extends SqlBaseScopeImpl implements NotificationStore.LocalStorage {

    private final JsonParser jsonParser;

    public NotificationLocalStorage(DaoSession daoSession) {
        super(daoSession);
        jsonParser = new JsonParser();
    }

    /**
     * Phải Insert từng item. Vì : Có trường hợp item đầu tiền mà lỗi (trùng mtuid),
     * GreenDao sẽ bỏ qua các item sau.
     */

    @Override
    public void put(List<NotificationData> val) {
        List<NotificationGD> list = transform(val);

        if (Lists.isEmptyOrNull(list)) {
            return;
        }

        for (NotificationGD item : list) {
            insertOrUpgrade(item);
        }
    }

    @Override
    public void put(NotificationData val) {
        insertOrUpgrade(transform(val));
    }

    @Override
    public long putSync(NotificationData val) {
        return insertOrUpgrade(transform(val));
    }

    private NotificationGD getNotification(long mtaid, long mtuid) {
        List<NotificationGD> list = null;
        if (mtaid > 0) {
            list = getDaoSession().getNotificationGDDao()
                    .queryBuilder()
                    .where(NotificationGDDao.Properties.Mtaid.eq(mtaid))
                    .list();
        } else if (mtuid > 0) {
            list = getDaoSession().getNotificationGDDao()
                    .queryBuilder()
                    .where(NotificationGDDao.Properties.Mtuid.eq(mtuid))
                    .list();
        }
        if (list == null || list.size() <= 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private boolean shouldUpgrade(NotificationGD oldNotify, NotificationGD newNotify) {
        return newNotify != null && !TextUtils.isEmpty(newNotify.message) && !(oldNotify.notificationtype != null && oldNotify.message != null);
    }

    private long upgrade(NotificationGD newNotify) {
        if (newNotify == null) {
            return -1;
        }
        NotificationGD oldNotify = getNotification(newNotify.mtaid, newNotify.mtuid);
        if (!shouldUpgrade(oldNotify, newNotify)) {
            return -1;
        }
        if (oldNotify != null) {
            newNotify.notificationstate = oldNotify.notificationstate;
        }
        Timber.d("upgrade notification success, type[%s] message[%s] state[%s]",
                newNotify.notificationtype, newNotify.message, newNotify.notificationstate);
        return getDaoSession().getNotificationGDDao().insertOrReplace(newNotify);
    }

    private long insertOrUpgrade(NotificationGD val) {
        if (val == null) {
            return -1;
        }

        try {
            //  Timber.d("Put item message [%s]", val.message);
            return getDaoSession().getNotificationGDDao().insert(val);
        } catch (Exception e) {
            Timber.d(e, "insert notification failed [id: %s, mtuid: %s, mtaid: %s]", val.id, val.mtuid, val.mtaid);
            if (e instanceof SQLiteConstraintException) {
                return upgrade(val);
            } else {
                return -1;
            }
        }
    }

    @Override
    public List<NotificationData> get(int pageIndex, int limit) {
        if (pageIndex < 0 || limit <= 0) {
            return Collections.emptyList();
        }

        return queryList(pageIndex, limit);
    }

    private List<NotificationGD> transform(Collection<NotificationData> entities) {
        return Lists.transform(entities, this::transform);
    }

    private NotificationGD transform(NotificationData entity) {
        if (entity == null) {
            return null;
        }

        if (entity.mtaid <= 0 && entity.mtuid <= 0) {
            Timber.e("Notification is invalid transactionId [%s] userid [%s] destuserid [%s]", entity.transid, entity.userid, entity.destuserid);
            return null;
        }

        NotificationGD notify = new NotificationGD();
        notify.appid = (entity.appid);
        notify.destuserid = (entity.destuserid);
        notify.message = (entity.message);
        notify.timestamp = (entity.timestamp);
        notify.notificationtype = (entity.notificationtype);
        JsonObject embeddataJson = entity.getEmbeddata();

        String embeddata = "";
        if (embeddataJson != null) {
            embeddata = embeddataJson.toString();
        }

        Timber.d("put embeddata [%s] notification state [%s]  ", embeddata, entity.notificationstate);

        notify.embeddata = (embeddata);
        notify.area = (entity.area);
        notify.userid = (entity.userid);
        notify.transid = (entity.transid);
        notify.notificationstate = (entity.notificationstate);

        if (entity.notificationId > 0) {
            notify.id = (entity.notificationId);
        } else {
            notify.id = (null);
        }

        notify.mtaid = (entity.mtaid);
        notify.mtuid = (entity.mtuid);

        return notify;
    }

    private NotificationData transform(NotificationGD notify) {
        if (notify == null) {
            return null;
        }

        NotificationData entity = new NotificationData();

        entity.notificationId = (notify.id);

        entity.appid = (notify.appid);
        entity.destuserid = (notify.destuserid);
        entity.message = (notify.message);
        entity.timestamp = (notify.timestamp);
        entity.notificationtype = (notify.notificationtype);

        String embeddata = notify.embeddata;

        if (TextUtils.isEmpty(embeddata)) {
            entity.setEmbeddata(new JsonObject());
        } else {
            try {
                entity.setEmbeddata(jsonParser.parse(embeddata).getAsJsonObject());
            } catch (Exception ex) {
                entity.setEmbeddata(new JsonObject());
                Timber.w(ex, " parse exception Notification Entity");
            }
        }

        entity.userid = (notify.userid);
        entity.transid = (notify.transid);
        entity.area = notify.area == null ? 0L : notify.area;
        entity.notificationstate = (notify.notificationstate);
        entity.mtuid = notify.mtuid;
        entity.mtaid = notify.mtaid;

        return entity;
    }

    private List<NotificationData> transformEntity(Collection<NotificationGD> notifications) {
        return Lists.transform(notifications, this::transform);
    }

    private List<NotificationData> queryList(int pageIndex, int limit) {
        return transformEntity(
                getDaoSession()
                        .getNotificationGDDao()
                        .queryBuilder()
                        .limit(limit)
                        .offset(pageIndex * limit)
                        .orderDesc(NotificationGDDao.Properties.Timestamp)
                        .where(NotificationGDDao.Properties.Message.isNotNull())
                        .list());
    }

    private List<NotificationGD> queryListUnRead() {
        return getDaoSession()
                .getNotificationGDDao()
                .queryBuilder()
                .where(NotificationGDDao.Properties.Notificationstate.notEq(Enums.NotificationState.READ.getId()))
                .list();
    }

    @Override
    public void markAsRead(long nId) {
        NotificationGD notify = getDaoSession().load(NotificationGD.class, nId);
        if (notify != null) {
            notify.notificationstate = (long) (Enums.NotificationState.READ.getId());
            Timber.d("markAsRead: nId %s", nId);
            getDaoSession().getNotificationGDDao().insertOrReplaceInTx(notify);
        }
    }

    @Override
    public void markAsReadAll() {
        List<NotificationGD> list = queryListUnRead();
        for (NotificationGD notify : list) {
            notify.notificationstate = (long) (Enums.NotificationState.READ.getId());
        }

        if (!Lists.isEmptyOrNull(list)) {
            getDaoSession().getNotificationGDDao().insertOrReplaceInTx(list);
        }
    }


    @Override
    public NotificationData get(long notifyId) {
        return transform(getDaoSession().getNotificationGDDao().load(notifyId));
    }

    @Override
    public int totalNotificationUnRead() {
        return (int) getDaoSession().getNotificationGDDao()
                .queryBuilder()
                .where(NotificationGDDao.Properties.Notificationstate.eq(Enums.NotificationState.UNREAD.getId()))
                .count();
    }

    @Override
    public void delete(long id) {
        getDaoSession().getNotificationGDDao().deleteByKey(id);
    }

    @Override
    public void deleteAll() {
        getDaoSession().getNotificationGDDao().deleteAll();
    }

    /**
     * Check notification exist.
     * If notification has same mtaid/mtuid (only mtaid or mtuid) then return true.
     * else return false.
     *
     * @param mtaid if has mtaid then hasn't mtuid.
     * @param mtuid if has mtuid then hasn't mtaid.
     * @return notification exist or didn't exist.
     */
    @Override
    public boolean isNotificationExisted(long mtaid, long mtuid) {
        if (mtaid > 0) {
            long count = getDaoSession().getNotificationGDDao()
                    .queryBuilder()
                    .where(NotificationGDDao.Properties.Mtaid.eq(mtaid))
                    .count();
            return (count > 0);
        } else if (mtuid > 0) {
            long count = getDaoSession().getNotificationGDDao()
                    .queryBuilder()
                    .where(NotificationGDDao.Properties.Mtuid.eq(mtuid))
                    .count();
            return (count > 0);
        }
        return false;
    }

    @Override
    public void markViewAllNotify() {

        List<NotificationGD> list = getDaoSession().getNotificationGDDao()
                .queryBuilder()
                .where(NotificationGDDao.Properties.Notificationstate.eq(Enums.NotificationState.UNREAD.getId()))
                .list();

        if (Lists.isEmptyOrNull(list)) {
            return;
        }

        for (NotificationGD notify : list) {
            notify.notificationstate = (long) (Enums.NotificationState.VIEW.getId());
        }
        getDaoSession().getNotificationGDDao().updateInTx(list);
    }

    @Override
    public long getOldestTimeNotification() {
        long timeUpdate = 0;

        List<NotificationGD> list = getDaoSession().getNotificationGDDao().queryBuilder()
                .orderAsc(NotificationGDDao.Properties.Timestamp)
                .limit(1).list();

        if (!Lists.isEmptyOrNull(list)) {
            timeUpdate = list.get(0).timestamp;
        }

        Timber.d("getOldestTimeNotification time stamp %s", timeUpdate);
        return timeUpdate;
    }

    @Override
    public void delete(long notifyType, long appId, long transid) {
        NotificationGDDao mDao = getDaoSession().getNotificationGDDao();
        List<NotificationGD> notifications = mDao.queryBuilder()
                .where(NotificationGDDao.Properties.Appid.eq(appId),
                        NotificationGDDao.Properties.Notificationtype.eq(notifyType),
                        NotificationGDDao.Properties.Transid.eq(transid))
                .limit(1)
                .list();

        if (Lists.isEmptyOrNull(notifications)) {
            return;
        }
        mDao.delete(notifications.get(0));
    }

    @Override
    public void delete(int mtuid, int mtaid) {
        NotificationGDDao mDao = getDaoSession().getNotificationGDDao();
        List<NotificationGD> notifications = mDao.queryBuilder()
                .where(NotificationGDDao.Properties.Mtuid.eq(mtuid),
                        NotificationGDDao.Properties.Mtaid.eq(mtaid))
                .limit(1)
                .list();

        if (Lists.isEmptyOrNull(notifications)) {
            return;
        }
        mDao.delete(notifications.get(0));
    }

    @Override
    public void setRecovery(boolean recovery) {
        if (recovery) {
            insertDataManifest(MANIFEST_RECOVERY_NOTIFY, "recovery");
        } else {
            deleteByKey(MANIFEST_RECOVERY_NOTIFY);
        }
    }

    @Override
    public boolean isRecovery() {
        return !TextUtils.isEmpty(getDataManifest(MANIFEST_RECOVERY_NOTIFY));
    }
}
