package vn.com.vng.zalopay.data.repository;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.NotificationEntity;
import vn.com.vng.zalopay.data.cache.NotificationStore;

/**
 * Created by AnhHieu on 6/20/16.
 */
public class NotificationRepository implements NotificationStore.Repository {

    final NotificationStore.LocalStorage localStorage;

    public NotificationRepository(NotificationStore.LocalStorage localStorage) {
        this.localStorage = localStorage;
    }

    @Override
    public Observable<List<NotificationEntity>> getNotification(int pageIndex, int count) {
        return localStorage.get(pageIndex, count);
    }

    @Override
    public Observable<Integer> totalNotificationUnRead() {
        return localStorage.totalNotificationUnRead();
    }

    @Override
    public void markAsRead(long nId) {
        localStorage.markAsRead(nId);
    }
}
