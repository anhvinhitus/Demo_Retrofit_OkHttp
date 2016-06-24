package vn.com.vng.zalopay.data.cache;

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

        Observable<List<NotificationData>> get(int pageIndex, int limit);

        Observable<Integer> totalNotificationUnRead();
    }

    interface RequestService {
    }

    interface Repository {
        Observable<List<NotificationData>> getNotification(int pageIndex, int count);

        Observable<Integer> totalNotificationUnRead();

        void markAsRead(long nId);
    }
}
