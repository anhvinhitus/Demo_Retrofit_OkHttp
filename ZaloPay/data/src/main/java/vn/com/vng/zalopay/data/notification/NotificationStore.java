package vn.com.vng.zalopay.data.notification;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.ws.model.NotificationData;

/**
 * Created by AnhHieu on 6/20/16.
 */
public interface NotificationStore {

    interface LocalStorage {
        void put(List<NotificationData> val);

        void put(NotificationData val);

        long putSync(NotificationData val);

        void markAsRead(long nId);

        void markAsReadAll();

        List<NotificationData> get(int pageIndex, int limit);

        int totalNotificationUnRead();

        void markReadAllNotify();

        void increaseTotalNotify();

        NotificationData get(long notifyId);

        void delete(long id);

        void deleteAll();
    }

    interface RequestService {
        /**
         * Send notification message to another zalopay user
         * @param uid zalopayid of current user
         * @param accesstoken access token of current user
         * @param receiverid zalopayid of received user
         * @param embededdata stringify of json data sent to another user
         * @return basic response
         */
        @GET("um/sendnotification")
        Observable<BaseResponse> sendNotification(@Query("userid") String uid,
                                                  @Query("accesstoken") String accesstoken,
                                                  @Query("receiverid") String receiverid,
                                                  @Query("embededdata") String embededdata);
    }

    interface Repository {
        Observable<List<NotificationData>> getNotification(int pageIndex, int count);

        Observable<Integer> totalNotificationUnRead();

        void markAsRead(long nId);

        Observable<Long> putNotify(NotificationData notify);

        Observable<NotificationData> getNotify(long id);

        Observable<Boolean> markReadAllNotify();

        Observable<Boolean> increaseTotalNotify();

        Observable<Boolean> removeNotification(long id);

        Observable<Boolean> removeAllNotification();

        Observable<BaseResponse> sendNotification(String receiverid, String embededdata);
    }
}
