package vn.com.vng.zalopay.data.cache;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.greenrobot.dao.async.AsyncSession;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.NotificationEntity;
import vn.com.vng.zalopay.data.cache.helper.ObservableHelper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.NotificationGD;
import vn.com.vng.zalopay.data.cache.model.NotificationGDDao;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.domain.model.User;

import static java.util.Collections.emptyList;

/**
 * Created by AnhHieu on 6/20/16.
 */
public class NotificationLocalStorage extends SqlBaseScopeImpl implements NotificationStore.LocalStorage {

    final JsonParser jsonParser;
    final AsyncSession asyncSession;

    final User user;
    final EventBus eventBus;

    public NotificationLocalStorage(DaoSession daoSession, User user, EventBus eventBus) {
        super(daoSession);
        jsonParser = new JsonParser();
        asyncSession = getDaoSession().getNotificationGDDao().getSession().startAsyncSession();
        this.user = user;
        this.eventBus = eventBus;
    }

    @Override
    public void put(List<NotificationEntity> val) {
        List<NotificationGD> list = transform(val);
        if (!Lists.isEmptyOrNull(list)) {
            getAsyncSession().insertOrReplaceInTx(NotificationGD.class, list);
        }


    }

    @Override
    public void put(NotificationEntity val) {
        NotificationGD item = transform(val);
        if (item != null) {
            Timber.d("Put item %s", item.getMessage());
            getAsyncSession().insertInTx(NotificationGD.class, item);
            if (!item.getRead()) {
                eventBus.post(new NotificationChangeEvent());
            }
        }
    }


    @Override
    public Observable<List<NotificationEntity>> get(int pageIndex, int limit) {
        return ObservableHelper.makeObservable(() -> queryList(pageIndex, limit));
    }

    AsyncSession getAsyncSession() {
        return asyncSession;
    }

    private List<NotificationGD> transform(Collection<NotificationEntity> notificationEntities) {
        if (Lists.isEmptyOrNull(notificationEntities)) {
            return emptyList();
        }

        List<NotificationGD> notificationGDs = new ArrayList<>(notificationEntities.size());
        for (NotificationEntity notificationEntity : notificationEntities) {
            NotificationGD notificationGD = transform(notificationEntity);
            if (notificationGD == null) {
                continue;
            }

            notificationGDs.add(notificationGD);
        }

        return notificationGDs;
    }

    private NotificationGD transform(NotificationEntity notificationEntity) {
        if (notificationEntity == null) {
            return null;
        }

        NotificationGD _notification = new NotificationGD();
        _notification.setAppid(notificationEntity.getAppid());
        _notification.setDestuserid(notificationEntity.getDestuserid());
        _notification.setMessage(notificationEntity.getMessage());
        _notification.setTimestamp(notificationEntity.getTimestamp());
        JsonObject embeddataJson = notificationEntity.getEmbeddata();

        int notificationtype = -1;
        int transtype = -1;

        try {
            if (embeddataJson.has(Constants.PARAM_RESPONSE_NOTIFICATION_TYPE)) {
                notificationtype = embeddataJson.getAsJsonObject(Constants.PARAM_RESPONSE_NOTIFICATION_TYPE).getAsInt();
            }

            if (embeddataJson.has(Constants.TRANSTYPE)) {
                transtype = embeddataJson.getAsJsonObject(Constants.TRANSTYPE).getAsInt();
            }
        } catch (Exception ex) {
        }

        boolean isRead;

        if (notificationtype != transtype || !user.uid.equals(notificationEntity.userid)) {
            isRead = false;
        } else {
            isRead = true;
        }

        String embeddata = embeddataJson.toString();

        Timber.d("embeddata put %s isRead %s  ", embeddata, isRead);

        _notification.setEmbeddata(embeddata);
        _notification.setUserid(notificationEntity.getUserid());
        _notification.setTransid(notificationEntity.getTransid());
        _notification.setRead(isRead);

        _notification.setId(null);

        return _notification;
    }

    private NotificationEntity transform(NotificationGD notificationGD) {
        if (notificationGD == null) {
            return null;
        }

        NotificationEntity _notification = new NotificationEntity();

        _notification.setNotificationId(notificationGD.getId()); // FIXME: 6/22/16 Change Id

        _notification.setAppid(notificationGD.getAppid());
        _notification.setDestuserid(notificationGD.getDestuserid());
        _notification.setMessage(notificationGD.getMessage());
        _notification.setTimestamp(notificationGD.getTimestamp());
        String embeddata = notificationGD.getEmbeddata();

        Timber.d("embeddata get %s ", embeddata);
        try {
            _notification.setEmbeddata(jsonParser.parse(embeddata).getAsJsonObject());
        } catch (Exception ex) {
            _notification.setEmbeddata(new JsonObject());
            Timber.w(ex, " parse exception Notification Entity");
        }

        _notification.setUserid(notificationGD.getUserid());
        _notification.setTransid(notificationGD.getTransid());
        _notification.setRead(notificationGD.getRead());

        return _notification;
    }

    private List<NotificationEntity> transformEntity(Collection<NotificationGD> notificationGDs) {
        if (Lists.isEmptyOrNull(notificationGDs)) {
            return emptyList();
        }

        List<NotificationEntity> notificationEntities = new ArrayList<>();
        for (NotificationGD notificationGD : notificationGDs) {
            NotificationEntity entity = transform(notificationGD);
            if (entity == null) {
                continue;
            }

            notificationEntities.add(entity);
        }

        return notificationEntities;
    }

    private List<NotificationEntity> queryList(int pageIndex, int limit) {
        return transformEntity(
                getDaoSession()
                        .getNotificationGDDao()
                        .queryBuilder()
                        .limit(limit)
                        .offset(pageIndex * limit)
                        .orderDesc(NotificationGDDao.Properties.Timestamp)
                        .list());
    }

    private List<NotificationGD> queryList() {
        return getDaoSession()
                .getNotificationGDDao()
                .queryBuilder()
                .list();
    }

    private List<NotificationGD> queryListUnRead() {
        return getDaoSession()
                .getNotificationGDDao()
                .queryBuilder()
                .where(NotificationGDDao.Properties.Read.eq(false))
                .list();
    }

    private long totalUnRead() {
        return getDaoSession()
                .getNotificationGDDao()
                .queryBuilder()
                .where(NotificationGDDao.Properties.Read.eq(false))
                .count();
    }

    private List<NotificationGD> queryNotification(long id) {
        return getDaoSession()
                .getNotificationGDDao()
                .queryBuilder()
                .where(NotificationGDDao.Properties.Id.eq(id)) //// FIXME: 6/22/16 Change id
                .list();
    }

    @Override
    public void markAsRead(long nId) {

        List<NotificationGD> list = queryNotification(nId);
        for (NotificationGD notify : list) {
            notify.setRead(true);
        }

        if (!Lists.isEmptyOrNull(list)) {
            getAsyncSession().insertOrReplaceInTx(NotificationGD.class, list);
            eventBus.post(new NotificationChangeEvent());
        }
    }

    @Override
    public void markAsReadAll() {
        List<NotificationGD> list = queryListUnRead();
        for (NotificationGD notify : list) {
            notify.setRead(true);
        }

        if (!Lists.isEmptyOrNull(list)) {
            getAsyncSession().insertOrReplaceInTx(NotificationGD.class, list);
            eventBus.post(new NotificationChangeEvent());
        }
    }

    @Override
    public Observable<Integer> totalNotificationUnRead() {
        return ObservableHelper.makeObservable(() -> (int) totalUnRead());
    }
}
