package vn.com.vng.zalopay.data.notification;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.model.NotificationData;

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
       // eventBus.post(new ReadNotifyEvent());
    }

    @Override
    public Observable<Boolean> putNotify(NotificationData notify) {
        return ObservableHelper.makeObservable(() -> {
            localStorage.put(notify);
            if (!notify.read) {
                localStorage.increaseTotalNotify();
            }
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<NotificationData> getNotify(long id) {
        return ObservableHelper.makeObservable(() -> localStorage.get(id));
    }

    @Override
    public Observable<Boolean> markReadAllNotify() {
        return ObservableHelper.makeObservable(() -> {
            localStorage.markReadAllNotify();
            eventBus.post(new ReadNotifyEvent());
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> increaseTotalNotify() {
        return ObservableHelper.makeObservable(() -> {
            localStorage.increaseTotalNotify();
            return Boolean.TRUE;
        });
    }
}
