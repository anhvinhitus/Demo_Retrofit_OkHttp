package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.NotificationEntity;

/**
 * Created by AnhHieu on 6/20/16.
 */
public interface NotificationStore {
    interface LocalStorage {
        void put(List<NotificationEntity> val);

        void put(NotificationEntity val);

        Observable<List<NotificationEntity>> get(int pageIndex, int limit);
    }

    interface RequestService {
    }

    interface Repository {
        Observable<List<NotificationEntity>> getNotification(int pageIndex, int count);
    }
}
