package vn.com.vng.zalopay.data.notification;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.CustomObserver;
import vn.com.vng.zalopay.data.DefaultObserver;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.model.NotificationEmbedData;
import vn.com.vng.zalopay.domain.model.User;

public class NotificationRepositoryTest extends ApplicationTestCase {
    NotificationStore.Repository mRepository;
    NotificationStore.LocalStorage mLocalStorage;
    NotificationStore.RequestService mRequestService;
    User mUser;

    BaseResponse baseResponse;

    private final int TRANSACTION_SIZE = 20;

    private List<NotificationData> entities = new ArrayList<>();


    public class RequestService implements NotificationStore.RequestService {
        @Override
        public Observable<BaseResponse> sendNotification(@Query("userid") String uid, @Query("accesstoken") String accesstoken, @Query("receiverid") String receiverid, @Query("embededdata") String embededdata) {
            return Observable.just(baseResponse);
        }
    }

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new NotificationLocalStorage(daoSession);
        mUser = new User("id");
        mRepository = new NotificationRepository(mLocalStorage, EventBus.getDefault(), new RxBus(), mRequestService, mUser);
    }

    private void initData() {
        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            int j = i + 1;
            NotificationData entity = new NotificationData();

            entity.appid = j;
            entity.area = 124124L;
            entity.destuserid = "abc";
            entity.embeddata = new NotificationEmbedData(new JsonObject());
            entity.message = "message";
            entity.timestamp = 1241356L + j;
            entity.notificationId = j;
            entity.transid = j;
            entity.userid = "abc";
            entity.notificationstate = 1;
            entity.notificationtype = 1;
            entity.mtaid = 0;
            entity.mtuid = j;

            entities.add(entity);
        }

        baseResponse = new BaseResponse();
        baseResponse.accesstoken = "";
        baseResponse.err = 1;
    }

    @Test
    public void get() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = 2;
        int limit = 5;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        assertEquals(entities.subList(
                TRANSACTION_SIZE - (pageIndex + 1) * limit, TRANSACTION_SIZE - (pageIndex + 1) * limit + limit), result);
    }

    @Test
    public void getAllTransactions() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = 0;
        int limit = TRANSACTION_SIZE;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        assertEquals(entities, result);
    }

    @Test
    public void getZeroTransaction() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = 0;
        int limit = 0;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        Assert.assertEquals("getNotification 0 transaction", 0, result.size());
    }

    @Test
    public void getWithEmptyDB() {
        List<NotificationData> result = new ArrayList<>();
        int pageIndex = 0;
        int limit = TRANSACTION_SIZE;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        Assert.assertEquals("getNotification with empty DB", 0, result.size());
    }

    @Test
    public void getOversizedList() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = 5;
        int limit = 5;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        Assert.assertEquals("getNotification oversized list", 0, result.size());
    }

    @Test
    public void getTransactionWhenLimitIsANegativeNumber() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = 5;
        int limit = -1;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        Assert.assertEquals("getNotification with limit is a negative number", 0, result.size());
    }

    @Test
    public void getTransactionWhenPageIndexIsANegativeNumber() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = -1;
        int limit = 4;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        Assert.assertEquals("getNotification transaction with pageIndex is a negative number", 0, result.size());
    }

    @Test
    public void totalNotificationUnReadWithAllUnreadList() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<Integer> result = new ArrayList<>();

        mRepository.totalNotificationUnRead().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("totalNotificationUnRead with all unread",
                Integer.valueOf(TRANSACTION_SIZE), result.get(0));
    }

    @Test
    public void totalNotificationUnReadWithAllReadList() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<Integer> result = new ArrayList<>();

        for(int i = 0; i < entities.size(); i++) {
            mRepository.markAsRead(i + 1).subscribe();
        }

        mRepository.totalNotificationUnRead().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("totalNotificationUnRead with all read",
                Integer.valueOf(0), result.get(0));
    }

    @Test
    public void totalNotificationUnRead() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<Integer> result = new ArrayList<>();

        mRepository.markAsRead(1).subscribe();
        mRepository.markAsRead(7).subscribe();
        mRepository.markAsRead(3).subscribe();

        mRepository.totalNotificationUnRead().subscribe(new CustomObserver<>(result));
        Assert.assertEquals("totalNotificationUnRead",
                Integer.valueOf(TRANSACTION_SIZE - 3), result.get(0));
    }

    @Test
    public void markAsRead() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.markAsRead(3).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("markAsRead", 3, result.get(17).notificationstate);
    }

    @Test
    public void markAsReadWithUndefinedId() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.markAsRead(0).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("markAsRead with undefined ID", 1, result.get(i).notificationstate);
        }
    }

    @Test
    public void markAsReadWithEmptyDB() {
        mRepository.markAsRead(1).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, 1).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("markAsRead with empty DB", 0, result.size());
    }

    @Test
    public void markAsViewAll() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.markViewAllNotify().subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("markViewAllNotify", 2, result.get(i).notificationstate);
        }
    }

    @Test
    public void markAsViewAllWithEmptyDB() {
        mRepository.markViewAllNotify().subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("markViewAllNotify", 3, result.get(i).notificationstate);
        }
    }

    @Test
    public void putEmptyList() {
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("putNotify empty list", 0, result.size());
    }

    @Test
    public void putNotify() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        assertEquals(entities, result);
    }

    @Test
    public void putNotifyNullObject() {
        NotificationData notificationData = new NotificationData();
        mRepository.putNotify(notificationData).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, 1).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("putNotify empty list", 0, result.size());
    }

    @Test
    public void putNotifyObject() {
        NotificationData notificationData = new NotificationData();
        notificationData.appid = 1;
        notificationData.area = 124124L;
        notificationData.destuserid = "abc";
        notificationData.embeddata = new NotificationEmbedData(new JsonObject());
        notificationData.message = "message";
        notificationData.timestamp = 1241356L + 1;
        notificationData.notificationId = 1;
        notificationData.transid = 1;
        notificationData.userid = "abc";
        notificationData.notificationstate = 1;
        notificationData.notificationtype = 1;
        notificationData.mtaid = 1;
        notificationData.mtuid = 0;

        mRepository.putNotify(notificationData).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, 1).subscribe(new DefaultObserver<>(result));

        compare2Elements(notificationData, result.get(0));
    }

    @Test
    public void getNotify() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotify(2).subscribe(new CustomObserver<>(result));

        compare2Elements(entities.get(1), result.get(0));
    }

    @Test
    public void getNotifyWithOversizedTransId() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotify(21).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("getNotify with oversized transId",
                null, result.get(0));
    }

    @Test
    public void removeNotification() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotification(3).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotify(3).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("removeNotification",
                null, result.get(0));
    }

    @Test
    public void removeNotificationWithUndefinedId() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotification(21).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotify(21).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("removeNotification with undefined id", null, result.get(0));
    }

    @Test
    public void removeNotificationWithEmptyDB() {
        mRepository.removeNotification(1).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotification with empty DB", 0, result.size());
    }

    @Test
    public void removeNotificationSameId() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotification(3).subscribe();
        mRepository.removeNotification(3).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotify(3).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("removeNotification same id", null, result.get(0));
    }

    @Test
    public void removeAllNotification() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeAllNotification().subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeAllNotification", 0, result.size());
    }

    @Test
    public void removeAllNotificationWithEmptyDB() {
        mRepository.removeAllNotification().subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeAllNotification with empty DB", 0, result.size());
    }

    @Test
    public void getOldestTimeNotification() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<Long> result = new ArrayList<>();
        mRepository.getOldestTimeNotification().subscribe(new CustomObserver<>(result));

        Assert.assertEquals("getOldestTimeNotification",
                entities.get(0).timestamp, result.get(0).longValue());
    }

    @Test
    public void getOldestTimeNotificationWithEmptyDB() {
        List<Long> result = new ArrayList<>();
        mRepository.getOldestTimeNotification().subscribe(new CustomObserver<>(result));

        Assert.assertEquals("getOldestTimeNotification with empty DB",
                0, result.get(0).longValue());
    }

    @Test
    public void isNotificationExistedWithMtaid() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<Boolean> result = new ArrayList<>();
        mRepository.isNotificationExisted(1, 0).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("isNotificationExisted with mtaid", false, result.get(0));
    }

    @Test
    public void isNotificationExistedWithMtuid() {
        initData();
        mRepository.putNotify(entities).subscribe();

        List<Boolean> result = new ArrayList<>();
        mRepository.isNotificationExisted(0, 1).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("isNotificationExisted with mtuid", true, result.get(0));
    }

    @Test
    public void removeNotifyByType() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByType(1, 3, 3).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotify(3).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("removeNotifyByType", null, result.get(0));
    }

    @Test
    public void removeNotifyByTypeWithUndefinedNotiType() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByType(2, 3, 3).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByType", TRANSACTION_SIZE, result.size());
    }

    @Test
    public void removeNotifyByTypeWithUndefinedAppId() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByType(1, 0, 3).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByType with undefined app id", TRANSACTION_SIZE, result.size());
    }

    @Test
    public void removeNotifyByTypeWithUndefinedTransId() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByType(1, 3, 0).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByType with undefined trans id", TRANSACTION_SIZE, result.size());
    }

    @Test
    public void removeNotifyByTypeWithEmptyDB() {
        mRepository.removeNotifyByType(0, 0, 0);

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByType with empty DB", 0, result.size());
    }

    @Test
    public void removeNotifyByTypeSameId() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByType(1, 3, 3).subscribe();
        mRepository.removeNotifyByType(1, 3, 3).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByType  same id", 19, result.size());
    }

    @Test
    public void removeNotifyByMsgId() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByMsgId(1, 0).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByMsgId", 19, result.size());
    }

    @Test
    public void removeNotifyByMsgIdWithUndfinedMtaid() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByMsgId(1, 1).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByMsgId with undefined mtaid", TRANSACTION_SIZE, result.size());
    }

    @Test
    public void removeNotifyByMsgIdWithUndfinedMtuid() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByMsgId(0, 0).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByMsgId with undefined mtuid", TRANSACTION_SIZE, result.size());
    }

    @Test
    public void removeNotifyByMsgIdWithUndefined2Params() {
        initData();
        mRepository.putNotify(entities).subscribe();

        mRepository.removeNotifyByMsgId(2, 3).subscribe();

        List<NotificationData> result = new ArrayList<>();
        mRepository.getNotification(0, TRANSACTION_SIZE).subscribe(new DefaultObserver<>(result));

        Assert.assertEquals("removeNotifyByMsgId with undefined 2 params", TRANSACTION_SIZE, result.size());
    }

//    @Test
//    public void recoveryNotify() {
//        mRepository.recoveryNotify(entities).subscribe();
//
//        Assert.assertEquals("recoveryNotify", true, mRepository.isRecovery());
//    }
//
//    @Test
//    public void recoveryNullNotify() {
//        mRepository.recoveryNotify(null).subscribe();
//
//        Assert.assertEquals("recoveryNotify with null param", true, mRepository.isRecovery());
//    }
//
//    @Test
//    public void recoveryNotifyWithEmptyList() {
//        List<NotificationData> notificationDatas = new ArrayList<>();
//        mRepository.recoveryNotify(notificationDatas).subscribe();
//
//        Assert.assertEquals("recoveryNotify with empty list", true, mRepository.isRecovery());
//    }
//
//    @Test
//    public void isRecoveryWhenNotSetting() {
//        Assert.assertEquals("isRecovery when not setting", false, mRepository.isRecovery());
//    }
//
//    @Test
//    public void getOldestTimeRecoveryNotification() {
//        mRepository.recoveryNotify(entities).subscribe();
//
//        List<Long> result = new ArrayList<>();
//        mRepository.getOldestTimeRecoveryNotification().subscribe(new CustomObserver<>(result));
//
//        Assert.assertNotEquals("getOldestTimeRecoveryNotification", 0, result.get(0).longValue());
//    }
//
//    @Test
//    public void getOldestTimeRecoveryNotificationWithNotSetting() {
//        List<Long> result = new ArrayList<>();
//        mRepository.getOldestTimeRecoveryNotification().subscribe(new CustomObserver<>(result));
//
//        Assert.assertEquals("getOldestTimeRecoveryNotification with not setting", 0, result.get(0).longValue());
//    }

    private void assertEquals(List<NotificationData> list1, List<NotificationData> list2) {
        if(list2.size() == 0) {
            Assert.fail("object is null");
            return;
        }

        for (int i = 0; i < list2.size(); i++) {
            compare2Elements(list1.get(i), list2.get(list2.size() - i - 1));
        }
    }

    private void compare2Elements(NotificationData b1, NotificationData b2) {
        if (b1 == null && b2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("appid", b1.appid, b2.appid);
        Assert.assertEquals("area", b1.area, b2.area);
        Assert.assertEquals("destuserid", b1.destuserid, b2.destuserid);
//        Assert.assertEquals("embeddata", b1.embeddata, b2.embeddata);
        Assert.assertEquals("message", b1.message, b2.message);
        Assert.assertEquals("timestamp", b1.timestamp, b2.timestamp);
        Assert.assertEquals("notificationId", b1.notificationId, b2.notificationId);
        Assert.assertEquals("transid", b1.transid, b2.transid);
        Assert.assertEquals("userid", b1.userid, b2.userid);
        Assert.assertEquals("notificationstate", b1.notificationstate, b2.notificationstate);
        Assert.assertEquals("notificationtype", b1.notificationtype, b2.notificationtype);
        Assert.assertEquals("appid", b1.transtype, b2.transtype);
    }
}
