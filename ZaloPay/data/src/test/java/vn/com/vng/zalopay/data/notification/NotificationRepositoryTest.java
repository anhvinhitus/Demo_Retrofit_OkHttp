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
        mUser = new User("uid");
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
        mRepository.putNotify(entities);

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
        mRepository.putNotify(entities);

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = 5;
        int limit = 5;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        Assert.assertEquals("getNotification oversized list", 0, result.size());
    }

    @Test
    public void getTransactionWhenLimitIsANegativeNumber() {
        initData();
        mRepository.putNotify(entities);

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = 5;
        int limit = -1;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        Assert.assertEquals("getNotification with limit is a negative number", 0, result.size());
    }

    @Test
    public void getTransactionWhenPageIndexIsANegativeNumber() {
        initData();
        mRepository.putNotify(entities);

        List<NotificationData> result = new ArrayList<>();
        int pageIndex = -1;
        int limit = 4;

        mRepository.getNotification(pageIndex, limit).subscribe(new DefaultObserver<>(result));
        Assert.assertEquals("getNotification transaction with pageIndex is a negative number", 0, result.size());
    }

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
