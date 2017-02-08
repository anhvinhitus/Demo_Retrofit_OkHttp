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
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.Enums;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 6/20/16.
 * Notification repository
 */
public class NotificationRepository implements NotificationStore.Repository {

    private final NotificationStore.LocalStorage mLocalStorage;
    private final EventBus mEventBus;
    private final RxBus mRxBus;
    private final NotificationStore.RequestService mRequestService;
    private final User mCurrentUser;

    public NotificationRepository(NotificationStore.LocalStorage localStorage,
                                  EventBus eventBus,
                                  RxBus rxBus,
                                  NotificationStore.RequestService requestService,
                                  User currentUser) {
        this.mLocalStorage = localStorage;
        this.mEventBus = eventBus;
        this.mRxBus = rxBus;
        mRequestService = requestService;
        mCurrentUser = currentUser;
        Timber.d("accessToken[%s]", mCurrentUser.accesstoken);
    }

    @Override
    public Observable<List<NotificationData>> getNotification(int pageIndex, int count) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.get(pageIndex, count));
    }

    @Override
    public Observable<Integer> totalNotificationUnRead() {
        return ObservableHelper.makeObservable(mLocalStorage::totalNotificationUnRead);
    }

    @Override
    public Observable<Boolean> markAsRead(long nId) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.markAsRead(nId);
            return Boolean.TRUE;
        });
    }


    /*
    * emit error MTAID, MTUID đã tồn tại trong db
    * */
    @Override
    public Observable<Long> putNotify(NotificationData notify) {
        return ObservableHelper.makeObservable(() -> {

            long rowId = mLocalStorage.putSync(notify);

            Timber.d("put notification rowId [%s] read [%s]", rowId, notify.notificationstate);

            if (rowId >= 0) {
                mEventBus.post(new NotificationChangeEvent((int) notify.notificationstate));

                if (mRxBus.hasObservers()) {
                    mRxBus.send(new NotificationChangeEvent((int) notify.notificationstate));
                }
            }

            return rowId;
        });
    }

    @Override
    public Observable<Void> putNotify(List<NotificationData> notify) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.put(notify);
            return null;
        });
    }

    @Override
    public Observable<NotificationData> getNotify(long id) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.get(id));
    }

    @Override
    public Observable<Boolean> markViewAllNotify() {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.markViewAllNotify();
            mEventBus.post(new ReadNotifyEvent());
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> removeNotification(long id) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.delete(id);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> removeAllNotification() {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.deleteAll();
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> sendNotification(String receiverid, String embededdata) {
        if (mCurrentUser == null || !mCurrentUser.hasZaloPayId() || TextUtils.isEmpty(mCurrentUser.getSession())) {
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
        return Observable.just(mLocalStorage.getOldestTimeNotification());
    }

    @Override
    public Observable<Long> getOldestTimeRecoveryNotification() {
        return Observable.just(mLocalStorage.getDataManifest(Constants.MANIFEST_RECOVERY_TIME_NOTIFICATION, 0L));
    }

    @Override
    public Observable<Void> recoveryNotify(List<NotificationData> notify) {
        return putNotify(notify)
                .doOnNext(aVoid -> {
                    mLocalStorage.setRecovery(true);
                    saveTimeRecovery(notify);
                    Timber.d("post NotificationChangeEvent recovery");
                    mEventBus.post(new NotificationChangeEvent(Enums.NotificationState.UNREAD.getId()));
                });
    }

    private void saveTimeRecovery(List<NotificationData> notify) {
        long time = getMinTimeStamp(notify);
        Timber.d("Save time recovery [%s]", time);
        if (time > 0) {
            mLocalStorage.insertDataManifest(Constants.MANIFEST_RECOVERY_TIME_NOTIFICATION, String.valueOf(time));
        }
    }

    public Observable<Boolean> isNotificationExisted(long mtaid, long mtuid) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.isNotificationExisted(mtaid, mtuid));
    }

    @Override
    public Boolean isNotifyExisted(long mtaid, long mtuid) {
        return mLocalStorage.isNotificationExisted(mtaid, mtuid);
    }

    @Override
    public Observable<Boolean> removeNotify(long notificationId) {
        return ObservableHelper.makeObservable(() -> {
                    mLocalStorage.delete(notificationId);
                    return Boolean.TRUE;
                }
        );
    }

    @Override
    public Observable<Boolean> removeNotifyByType(long notifyType, long appId, long transid) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.delete(notifyType, appId, transid);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> removeNotifyByMsgId(int mtuid, int mtaid) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.delete(mtuid, mtaid);
            return Boolean.TRUE;
        });
    }

    private long getMinTimeStamp(List<NotificationData> notifications) {
        if (Lists.isEmptyOrNull(notifications)) {
            return -1;
        }

        long minTime = 0;
        for (NotificationData item : notifications) {

            if (item.timestamp == 0) {
                continue;
            }
            Timber.d("getMinTimeStamp: [%s] ", item.timestamp);

            if (minTime == 0 || minTime > item.timestamp) {
                minTime = item.timestamp;
            }
        }

        return minTime;
    }

    @Override
    public Boolean isRecovery() {
        return mLocalStorage.isRecovery();
    }
}
