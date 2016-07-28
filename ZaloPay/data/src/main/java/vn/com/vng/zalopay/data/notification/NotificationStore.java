package vn.com.vng.zalopay.data.notification;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.ws.model.NotificationData;

/**
 * Created by AnhHieu on 6/20/16.
 */
public interface NotificationStore {

    interface LocalStorage {
        void put(List<NotificationData> val);

        void put(NotificationData val);

        void markAsRead(long nId);

        void markAsReadAll();

        List<NotificationData> get(int pageIndex, int limit);

        int totalNotificationUnRead();

        NotificationData get(long notifyId);
    }

    interface RequestService {
    }

    interface Repository {
        Observable<List<NotificationData>> getNotification(int pageIndex, int count);

        Observable<Integer> totalNotificationUnRead();

        void markAsRead(long nId);

        void put(NotificationData notify);

        Observable<NotificationData> getNotify(long id);
    }
}
