package vn.com.vng.zalopay.data.notification;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 6/20/16.
 * Notification repository
 */
public class NotificationRepository implements NotificationStore.Repository {

    private final NotificationStore.LocalStorage localStorage;
    private final EventBus mEventBus;
    private final RxBus mRxBus;
    private final NotificationStore.RequestService mRequestService;
    private final User mCurrentUser;

    public NotificationRepository(NotificationStore.LocalStorage localStorage,
                                  EventBus eventBus,
                                  RxBus rxBus,
                                  NotificationStore.RequestService requestService,
                                  User currentUser) {
        this.localStorage = localStorage;
        this.mEventBus = eventBus;
        this.mRxBus = rxBus;
        mRequestService = requestService;
        mCurrentUser = currentUser;
        Timber.d("accessToken[%s]", mCurrentUser.accesstoken);
    }

    @Override
    public Observable<List<NotificationData>> getNotification(int pageIndex, int count) {
        return ObservableHelper.makeObservable(() -> localStorage.get(pageIndex, count));
    }

    @Override
    public Observable<Integer> totalNotificationUnRead() {
        return ObservableHelper.makeObservable(localStorage::totalNotificationUnRead);
    }

    @Override
    public void markAsRead(long nId) {
        localStorage.markAsRead(nId);
    }


    /*
    * emit error MTAID, MTUID đã tồn tại trong db
    * */
    @Override
    public Observable<Long> putNotify(NotificationData notify) {
        return ObservableHelper.makeObservable(() -> {

            long rowId = localStorage.putSync(notify);

            Timber.d("put notification rowId [%s] read [%s]", rowId, notify.notificationstate);

            if (rowId >= 0) {
                mEventBus.post(new NotificationChangeEvent(notify.notificationstate));

                if (mRxBus.hasObservers()) {
                    mRxBus.send(new NotificationChangeEvent(notify.notificationstate));
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
    public Observable<Boolean> markViewAllNotify() {
        return ObservableHelper.makeObservable(() -> {
            localStorage.markViewAllNotify();
            mEventBus.post(new ReadNotifyEvent());
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

    @Override
    public Observable<BaseResponse> sendNotification(String receiverid, String embededdata) {
        if (mCurrentUser == null || TextUtils.isEmpty(mCurrentUser.zaloPayId) || TextUtils.isEmpty(mCurrentUser.getSession())) {
            return Observable.error(new IllegalArgumentException("Current user is null"));
        }

        return mRequestService.sendNotification(
                mCurrentUser.zaloPayId,
                mCurrentUser.getSession(),
                receiverid,
                embededdata);
    }
}
