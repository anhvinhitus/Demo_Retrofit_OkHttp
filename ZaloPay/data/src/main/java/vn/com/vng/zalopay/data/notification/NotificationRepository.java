package vn.com.vng.zalopay.data.notification;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
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
    public Observable<Long> putNotify(NotificationData notify) {
        return ObservableHelper.makeObservable(() -> {

            long rowId = localStorage.putSync(notify);

            Timber.d("put notification rowId  [%s]", rowId);

            if (rowId >= 0) {
                if (!notify.read) {
                    localStorage.increaseTotalNotify();
                }
                eventBus.post(new NotificationChangeEvent(notify.read));
            }

            return rowId;
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
