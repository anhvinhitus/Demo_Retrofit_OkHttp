package vn.com.vng.zalopay.data.notification;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.model.NotificationData;

/**
 * Created by AnhHieu on 6/20/16.
 */
public class NotificationRepository implements NotificationStore.Repository {

    final NotificationStore.LocalStorage localStorage;
    final EventBus eventBus;
    final RxBus mRxBus;

    public NotificationRepository(NotificationStore.LocalStorage localStorage, EventBus eventBus, RxBus rxBus) {
        this.localStorage = localStorage;
        this.eventBus = eventBus;
        this.mRxBus = rxBus;
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


    /*
    * emit error MTAID, MTUID đã tồn tại trong db
    * */
    @Override
    public Observable<Long> putNotify(NotificationData notify) {
        return ObservableHelper.makeObservable(() -> {

            long rowId = localStorage.putSync(notify);

            Timber.d("put notification rowId [%s] read [%s]", rowId, notify.read);

            if (rowId >= 0) {
                if (!notify.read) {
                    localStorage.increaseTotalNotify();
                }

                eventBus.post(new NotificationChangeEvent(notify.read));

                if (mRxBus.hasObservers()) {
                    mRxBus.send(new NotificationChangeEvent(notify.read));
                }
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

    @Override
    public Observable<Boolean> removeNotification(long id) {
        return ObservableHelper.makeObservable(() -> {
            localStorage.delete(id);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> removeAllNotification() {
        return ObservableHelper.makeObservable(() -> {
            localStorage.deleteAll();
            return Boolean.TRUE;
        });
    }
}
