package vn.com.vng.zalopay.data.notification;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.SqlBaseScope;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 6/20/16.
 * *
 */
public interface NotificationStore {

    interface LocalStorage extends SqlBaseScope {
        void put(List<NotificationData> val);

        void put(NotificationData val);

        long putSync(NotificationData val);

        void markAsRead(long nId);

        void markAsReadAll();

        List<NotificationData> get(int pageIndex, int limit);

        int totalNotificationUnRead();

        void markViewAllNotify();

        NotificationData get(long notifyId);

        void delete(long id);

        void deleteAll();

        long getOldestTimeNotification();

        /**
         * Check notification exist.
         * If notification has same mtaid/mtuid (only mtaid or mtuid) then return true.
         * else return false.
         *
         * @param mtaid if has mtaid then hasn't mtuid.
         * @param mtuid if has mtuid then hasn't mtaid.
         * @return notification exist or didn't exist.
         */
        boolean isNotificationExisted(long mtaid, long mtuid);

        void delete(long notifyType, long appId, long transid);

        void delete(int mtuid, int mtaid);

        void setRecovery(boolean recovery);

        boolean isRecovery();
    }

    interface RequestService {
        /**
         * Send notification message to another zalopay user
         *
         * @param uid         zalopayid of current user
         * @param accesstoken access token of current user
         * @param receiverid  zalopayid of received user
         * @param embededdata stringify of json data sent to another user
         * @return basic response
         */
        @API_NAME(value = {ZPEvents.API_UM_SENDNOTIFICATION, ZPEvents.CONNECTOR_UM_SENDNOTIFICATION})
        @GET(Constants.UM_API.SENDNOTIFICATION)
        Observable<BaseResponse> sendNotification(@Query("userid") String uid,
                                                  @Query("accesstoken") String accesstoken,
                                                  @Query("receiverid") String receiverid,
                                                  @Query("embededdata") String embededdata);
    }

    interface Repository {
        Observable<List<NotificationData>> getNotification(int pageIndex, int count);

        Observable<Integer> totalNotificationUnRead();

        Observable<Boolean> markAsRead(long nId);

        Observable<Long> putNotify(NotificationData notify);

        Observable<Void> putNotify(List<NotificationData> notify);

        Observable<Void> recoveryNotify(List<NotificationData> notify);

        Observable<NotificationData> getNotify(long id);

        Observable<Boolean> markViewAllNotify();

        Observable<Boolean> removeNotification(long id);

        Observable<Boolean> removeAllNotification();

        Observable<Boolean> sendNotification(String receiverid, String embededdata);

        Observable<Long> getOldestTimeNotification();

        Observable<Long> getOldestTimeRecoveryNotification();

        Observable<Boolean> isNotificationExisted(long mtaid, long mtuid);

        Observable<Boolean> removeNotify(long notificationId);

        Observable<Boolean> removeNotifyByType(long notifyType, long appId, long transid);

        Observable<Boolean> removeNotifyByMsgId(int mtuid, int mtaid);

        Boolean isRecovery();

        Boolean isNotifyExisted(long mtaid, long mtuid);
    }
}
