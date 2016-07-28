package vn.com.vng.zalopay.data.notification;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.notification.NotificationStore;

/**
 * Created by AnhHieu on 6/20/16.
 */
public class NotificationRepository implements NotificationStore.Repository {

    final NotificationStore.LocalStorage localStorage;
    final EventBus eventBus;

    public NotificationRepository(NotificationStore.LocalStorage localStorage, EventBus eventBus) {
        this.localStorage = localStorage;
        this.eventBus = eventBus;
    }

    @Override
    public Observable<List<NotificationData>> getNotification(int pageIndex, int count) {
        return ObservableHelper.makeObservable(() -> localStorage.get(pageIndex, count));
    }

    @Override
    public Observable<Integer> totalNotificationUnRead() {
        return ObservableHelper.makeObservable(() -> localStorage.totalNotificationUnRead());
    }

    @Override
    public void markAsRead(long nId) {
        localStorage.markAsRead(nId);
        eventBus.post(new ReadNotifyEvent(nId));
    }

    @Override
    public void putNotify(NotificationData notify) {
        localStorage.put(notify);
        if (!notify.read) {
            eventBus.post(new NotificationChangeEvent());
        }
    }

    @Override
    public Observable<NotificationData> getNotify(long id) {
        return ObservableHelper.makeObservable(() -> localStorage.get(id));
    }
}
