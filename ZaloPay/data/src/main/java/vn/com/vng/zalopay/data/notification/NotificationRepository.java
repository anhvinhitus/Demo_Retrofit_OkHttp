package vn.com.vng.zalopay.data.notification;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
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
    public Observable<Boolean> markAsRead(long nId) {
        return ObservableHelper.makeObservable(() -> {
            localStorage.markAsRead(nId);
            return Boolean.TRUE;
        });
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
    public Observable<Void> putNotify(List<NotificationData> notify) {
        return ObservableHelper.makeObservable(() -> {
            localStorage.put(notify);
            return null;
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
    public Observable<Boolean> sendNotification(String receiverid, String embededdata) {
        if (mCurrentUser == null || TextUtils.isEmpty(mCurrentUser.zaloPayId) || TextUtils.isEmpty(mCurrentUser.getSession())) {
            return Observable.error(new IllegalArgumentException("Current user is null"));
        }

        return mRequestService.sendNotification(
                mCurrentUser.zaloPayId,
                mCurrentUser.getSession(),
                receiverid,
                embededdata)
                .map(BaseResponse::isSuccessfulResponse);
    }

    @Override
    public Observable<Long> getOldestTimeNotification() {
        return Observable.just(localStorage.getDataManifest(Constants.MANIFEST_RECOVERY_NOTIFICATION, 0L))
                .filter(lastTime -> lastTime > 0L)
                .map(aLong -> localStorage.getOldestTimeNotification());
    }

    @Override
    public Observable<Void> recoveryNotify(List<NotificationData> notify) {
        return putNotify(notify)
                .doOnNext(aVoid -> localStorage.insertDataManifest(Constants.MANIFEST_RECOVERY_NOTIFICATION,
                        String.valueOf(System.currentTimeMillis() / 1000)));
    }

    public Observable<Boolean> isNotificationExisted(long mtaid, long mtuid) {
        return ObservableHelper.makeObservable(() -> localStorage.isNotificationExisted(mtaid, mtuid));
    }

    @Override
    public Observable<Boolean> removeNotify(long notificationId) {
        return ObservableHelper.makeObservable(() -> {
                    localStorage.delete(notificationId);
                    return Boolean.TRUE;
                }
        );
    }

    @Override
    public Observable<Boolean> removeNotifyByType(int notifyType, int appId, long transid) {
        return ObservableHelper.makeObservable(() -> {
            localStorage.delete(notifyType, appId, transid);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> removeNotifyByMsgId(int mtuid, int mtaid) {
        return ObservableHelper.makeObservable(() -> {
            localStorage.delete(mtuid, mtaid);
            return Boolean.TRUE;
        });
    }
}
