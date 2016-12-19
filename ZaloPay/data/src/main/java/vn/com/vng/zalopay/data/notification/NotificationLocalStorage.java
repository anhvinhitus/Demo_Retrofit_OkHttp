package vn.com.vng.zalopay.data.notification;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collection;
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

    @Override
    public void put(List<NotificationData> val) {
        List<NotificationGD> list = transform(val);
        if (!Lists.isEmptyOrNull(list)) {
            try {
                getDaoSession().getNotificationGDDao().insertInTx(list);
            } catch (Exception ex) {
                //empty
            }
        }
    }

    @Override
    public void put(NotificationData val) {
        NotificationGD item = transform(val);
        if (item != null) {
            Timber.d("Put item %s", item.message);
            try {
                getDaoSession().getNotificationGDDao().insertInTx(item);
            } catch (Exception e) {
                //empty
            }
        }
    }

    @Override
    public long putSync(NotificationData val) {
        NotificationGD item = transform(val);
        if (item != null) {
            return getDaoSession().getNotificationGDDao().insert(item);
        }
        return -1;
    }

    @Override
    public void putSync(List<NotificationData> val) {
        List<NotificationGD> list = transform(val);
        if (Lists.isEmptyOrNull(list)) {
            return;
        }
        getDaoSession().getNotificationGDDao().insertInTx(list);
    }

    @Override
    public List<NotificationData> get(int pageIndex, int limit) {
        return queryList(pageIndex, limit);
    }

    private List<NotificationGD> transform(Collection<NotificationData> notificationEntities) {
        if (Lists.isEmptyOrNull(notificationEntities)) {
            return emptyList();
        }

        List<NotificationGD> notificationGDs = new ArrayList<>(notificationEntities.size());
        for (NotificationData notificationEntity : notificationEntities) {
            NotificationGD notificationGD = transform(notificationEntity);
            if (notificationGD == null) {
                continue;
            }

            notificationGDs.add(notificationGD);
        }

        return notificationGDs;
    }

    private NotificationGD transform(NotificationData entity) {
        if (entity == null) {
            return null;
        }

        if (entity.mtaid <= 0 && entity.mtuid <= 0) {
            Timber.e("Notification is invalid transactionId [%s] userid [%s] destuserid [%s]", entity.transid, entity.userid, entity.destuserid);
            return null;
        }

        NotificationGD _notification = new NotificationGD();
        _notification.appid = (entity.getAppid());
        _notification.destuserid = (entity.getDestuserid());
        _notification.message = (entity.getMessage());
        _notification.timestamp = (entity.getTimestamp());
        _notification.notificationtype = (entity.getNotificationType());
        JsonObject embeddataJson = entity.getEmbeddata();


        String embeddata = "";
        if (embeddataJson != null) {
            embeddata = embeddataJson.toString();
        }

        Timber.d("put embeddata [%s] notification state [%s]  ", embeddata, entity.notificationstate);

        _notification.embeddata = (embeddata);
        _notification.area = (entity.area);
        _notification.userid = (entity.getUserid());
        _notification.transid = (entity.getTransid());
        _notification.notificationstate = (entity.notificationstate);

        if (entity.notificationId > 0) {
            _notification.id = (entity.notificationId);
        } else {
            _notification.id = (null);
        }

        _notification.mtaid = (entity.mtaid);
        _notification.mtuid = (entity.mtuid);

        return _notification;
    }

    private NotificationData transform(NotificationGD notificationGD) {
        if (notificationGD == null) {
            return null;
        }

        NotificationData _notification = new NotificationData();

        _notification.setNotificationId(notificationGD.id);

        _notification.setAppid(notificationGD.appid);
        _notification.setDestuserid(notificationGD.destuserid);
        _notification.setMessage(notificationGD.message);
        _notification.setTimestamp(notificationGD.timestamp);
        _notification.setNotificationtype(notificationGD.notificationtype);

        String embeddata = notificationGD.embeddata;

        if (TextUtils.isEmpty(embeddata)) {
            _notification.setEmbeddata(new JsonObject());
        } else {
            try {
                _notification.setEmbeddata(jsonParser.parse(embeddata).getAsJsonObject());
            } catch (Exception ex) {
                _notification.setEmbeddata(new JsonObject());
                Timber.w(ex, " parse exception Notification Entity");
            }
        }

        _notification.setUserid(notificationGD.userid);
        _notification.setTransid(notificationGD.transid);
        _notification.area = notificationGD.area == null ? 0L : notificationGD.area;
        _notification.setNotificationState(notificationGD.notificationstate);

        return _notification;
    }

    private List<NotificationData> transformEntity(Collection<NotificationGD> notificationGDs) {
        if (Lists.isEmptyOrNull(notificationGDs)) {
            return emptyList();
        }

        List<NotificationData> notificationEntities = new ArrayList<>();
        for (NotificationGD notificationGD : notificationGDs) {
            NotificationData entity = transform(notificationGD);
            if (entity == null) {
                continue;
            }

            notificationEntities.add(entity);
        }

        return notificationEntities;
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
}
