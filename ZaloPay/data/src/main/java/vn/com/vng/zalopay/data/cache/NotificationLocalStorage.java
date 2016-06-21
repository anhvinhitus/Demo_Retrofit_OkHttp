package vn.com.vng.zalopay.data.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.greenrobot.dao.async.AsyncSession;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.NotificationEntity;
import vn.com.vng.zalopay.data.cache.helper.ObservableHelper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.NotificationGD;
import vn.com.vng.zalopay.data.cache.model.NotificationGDDao;
import vn.com.vng.zalopay.data.util.Lists;

import static java.util.Collections.emptyList;

/**
 * Created by AnhHieu on 6/20/16.
 */
public class NotificationLocalStorage extends SqlBaseScopeImpl implements NotificationStore.LocalStorage {

    public NotificationLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void put(List<NotificationEntity> val) {
        List<NotificationGD> list = transform(val);
        if (Lists.isEmptyOrNull(list)) {
            getAsyncSession().insertOrReplaceInTx(NotificationGD.class, list);
        }
    }

    @Override
    public void put(NotificationEntity val) {
        NotificationGD item = transform(val);
        if (item != null) {
            Timber.d("Put item %s", item.getMessage());
            getAsyncSession().insertOrReplaceInTx(NotificationGD.class, item);
        }
    }


    @Override
    public Observable<List<NotificationEntity>> get(int pageIndex, int limit) {
        return ObservableHelper.makeObservable(() -> queryList(pageIndex, limit));
    }

    AsyncSession getAsyncSession() {
        AsyncSession asyncSession = getDaoSession().getNotificationGDDao().getSession().startAsyncSession();
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
        _notification.setTranstype(notificationEntity.getTranstype());
        _notification.setUserid(notificationEntity.getUserid());
        _notification.setTransid(notificationEntity.getTransid());
        _notification.setRead(notificationEntity.isRead());

        return _notification;
    }

    private NotificationEntity transform(NotificationGD notificationGD) {
        if (notificationGD == null) {
            return null;
        }

        NotificationEntity _notification = new NotificationEntity();
        _notification.setAppid(notificationGD.getAppid());
        _notification.setDestuserid(notificationGD.getDestuserid());
        _notification.setMessage(notificationGD.getMessage());
        _notification.setTimestamp(notificationGD.getTimestamp());
        _notification.setTranstype(notificationGD.getTranstype());
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

}
