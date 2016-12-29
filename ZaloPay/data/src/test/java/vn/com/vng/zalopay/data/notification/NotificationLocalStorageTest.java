package vn.com.vng.zalopay.data.notification;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.JsonObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.model.NotificationEmbedData;

public class NotificationLocalStorageTest extends ApplicationTestCase {

    private NotificationStore.LocalStorage mLocalStorage;

    private final int TRANSACTION_SIZE = 20; //row count jsonObject

    private List<NotificationData> entities = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new NotificationLocalStorage(daoSession);
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
    }

    @Test
    public void insertDuplication() {
        NotificationData item = new NotificationData();
        //item.notificationId = 0;
        item.mtuid = 100;
        item.mtaid = 0;

        NotificationData item2 = new NotificationData();
        // item.notificationId = 0;
        item2.mtuid = 100;
        item2.mtaid = 0;

        NotificationData item3 = new NotificationData();
        item3.notificationId = 3;
        item3.mtuid = 0;
        item3.mtaid = 0;

        mLocalStorage.putSync(item);
        mLocalStorage.putSync(item2);
        mLocalStorage.putSync(item3);

        NotificationData notify = mLocalStorage.get(1);
        NotificationData notify1 = mLocalStorage.get(2);
        NotificationData notify2 = mLocalStorage.get(3);

        System.out.println(String.format("notify %s %s %s", notify, notify1, notify2));

        if (notify1 != null) {
            System.out.println(String.format("notifyId %s", notify1.notificationId));
        }

        Assert.assertTrue(notify != null && notify1 == null && notify2 == null);
    }

    @Test
    public void putEmptyList() {
        mLocalStorage.put(entities);
        Assert.assertEquals("put empty list", 0, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void put() {
        initData();
        mLocalStorage.put(entities);
        assertEquals(entities, mLocalStorage.get(0, TRANSACTION_SIZE));
    }

    @Test
    public void putNullObject() {
        NotificationData notificationData = new NotificationData();
        mLocalStorage.put(notificationData);
        Assert.assertEquals("put empty list", 0, mLocalStorage.get(0, 1).size());
    }

    @Test
    public void putObject() {
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

        mLocalStorage.put(notificationData);
        compare2Elements(notificationData, mLocalStorage.get(0, 1).get(0));
    }

    @Test
    public void putSyncNullObject() {
        NotificationData notificationData = new NotificationData();
        Assert.assertEquals("putSync null object", -1, mLocalStorage.putSync(notificationData));
    }

    @Test
    public void putSyncObject() {
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

        Assert.assertNotEquals("putSyncObject", -1, mLocalStorage.putSync(notificationData));
    }

    @Test
    public void markAsRead() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.markAsRead(3);
        Assert.assertEquals("markAsRead", 3, mLocalStorage.get(3).notificationstate);
    }

    @Test
    public void markAsReadWithUndefinedId() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.markAsRead(0);

        List<NotificationData> result = mLocalStorage.get(0, TRANSACTION_SIZE);
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("markAsRead with undefined ID", 1, result.get(i).notificationstate);
        }
    }

    @Test
    public void markAsReadWithEmptyDB() {
        mLocalStorage.markAsRead(1);
        Assert.assertEquals("markAsRead with empty DB", 0, mLocalStorage.get(0, 1).size());
    }

    @Test
    public void markAsReadAll() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.markAsReadAll();

        List<NotificationData> result = mLocalStorage.get(0, TRANSACTION_SIZE);
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("markAsReadAll", 3, result.get(i).notificationstate);
        }
    }

    @Test
    public void markAsReadAllWithEmptyDB() {
        mLocalStorage.markAsReadAll();

        List<NotificationData> result = mLocalStorage.get(0, TRANSACTION_SIZE);
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("markAsReadAll", 3, result.get(i).notificationstate);
        }
    }

    @Test
    public void get() {
        initData();
        mLocalStorage.put(entities);

        List<NotificationData> result;
        int pageIndex = 2;
        int limit = 5;

        result = mLocalStorage.get(pageIndex, limit);
        assertEquals(entities.subList(
                TRANSACTION_SIZE - (pageIndex + 1) * limit, TRANSACTION_SIZE - (pageIndex + 1) * limit + limit), result);
    }

    @Test
    public void getAllTransactions() {
        initData();
        mLocalStorage.put(entities);

        int pageIndex = 0;
        int limit = TRANSACTION_SIZE;

        assertEquals(entities, mLocalStorage.get(pageIndex, limit));
    }

    @Test
    public void getZeroTransaction() {
        initData();
        mLocalStorage.put(entities);

        List<NotificationData> result;
        int pageIndex = 0;
        int limit = 0;

        result = mLocalStorage.get(pageIndex, limit);
        Assert.assertEquals("get 0 transaction", 0, result.size());
    }

    @Test
    public void getWithEmptyDB() {
        List<NotificationData> result;
        int pageIndex = 0;
        int limit = TRANSACTION_SIZE;

        result = mLocalStorage.get(pageIndex, limit);
        Assert.assertEquals("get with empty DB", 0, result.size());
    }

    @Test
    public void getOversizedList() {
        initData();
        mLocalStorage.put(entities);

        List<NotificationData> result;
        int pageIndex = 5;
        int limit = 5;

        result = mLocalStorage.get(pageIndex, limit);
        Assert.assertEquals("get oversized list", 0, result.size());
    }

    @Test
    public void getTransactionWhenLimitIsANegativeNumber() {
        initData();
        mLocalStorage.put(entities);

        List<NotificationData> result;
        int pageIndex = 5;
        int limit = -1;

        result = mLocalStorage.get(pageIndex, limit);
        Assert.assertEquals("get with limit is a negative number", 0, result.size());
    }

    @Test
    public void getTransactionWhenPageIndexIsANegativeNumber() {
        initData();
        mLocalStorage.put(entities);

        List<NotificationData> result;
        int pageIndex = -1;
        int limit = 4;

        result = mLocalStorage.get(pageIndex, limit);
        Assert.assertEquals("get transaction with pageIndex is a negative number", 0, result.size());
    }

    @Test
    public void totalNotificationUnReadWithAllUnreadList() {
        initData();
        mLocalStorage.put(entities);

        Assert.assertEquals("totalNotificationUnRead with all unread", TRANSACTION_SIZE, mLocalStorage.totalNotificationUnRead());
    }

    @Test
    public void totalNotificationUnReadWithAllReadList() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.markAsReadAll();

        Assert.assertEquals("totalNotificationUnRead with all read", 0, mLocalStorage.totalNotificationUnRead());
    }

    @Test
    public void totalNotificationUnRead() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.markAsRead(1);
        mLocalStorage.markAsRead(7);
        mLocalStorage.markAsRead(3);

        Assert.assertEquals("totalNotificationUnRead", TRANSACTION_SIZE - 3, mLocalStorage.totalNotificationUnRead());
    }

    @Test
    public void markViewAllNotifyWithAllUnread() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.markViewAllNotify();

        List<NotificationData> result = mLocalStorage.get(0, TRANSACTION_SIZE);
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("markViewAllNotify with all unread", 2, result.get(i).notificationstate);
        }
    }

    @Test
    public void markViewAllNotifyWithAllRead() {
        initData();
        mLocalStorage.put(entities);
        mLocalStorage.markAsReadAll();

        mLocalStorage.markViewAllNotify();

        List<NotificationData> result = mLocalStorage.get(0, TRANSACTION_SIZE);
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals("markViewAllNotify with all read", 3, result.get(i).notificationstate);
        }
    }

    @Test
    public void markViewAllNotify() {
        initData();
        mLocalStorage.put(entities);
        mLocalStorage.markAsRead(1);
        mLocalStorage.markAsRead(7);

        mLocalStorage.markViewAllNotify();

        List<NotificationData> result = mLocalStorage.get(0, TRANSACTION_SIZE);
        for(int i = 0; i < result.size(); i++) {
            if(result.get(i).notificationId == 1 || result.get(i).notificationId == 7) {
                Assert.assertEquals("markViewAllNotify", 3, result.get(i).notificationstate);
            }
            else {
                Assert.assertEquals("markViewAllNotify", 2, result.get(i).notificationstate);
            }
        }
    }

    @Test
    public void getObjectWithEmptyDB() {
        Assert.assertEquals("getObject with empty DB",
                null, mLocalStorage.get(0));
    }

    @Test
    public void getObject() {
        initData();
        mLocalStorage.put(entities);

        compare2Elements(entities.get(1), mLocalStorage.get(2));
    }

    @Test
    public void getObjectWithOversizedTransId() {
        initData();
        mLocalStorage.put(entities);

        Assert.assertEquals("getObject with oversized transId",
                null, mLocalStorage.get(21));
    }

    @Test
    public void delete() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(3);

        Assert.assertEquals("delete",
                null, mLocalStorage.get(3));
    }

    @Test
    public void deleteWithUndefinedId() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(21);

        Assert.assertEquals("delete with undefined id",
                null, mLocalStorage.get(21));
    }

    @Test
    public void deleteWithEmptyDB() {
        mLocalStorage.delete(1);
        Assert.assertEquals("delete with empty DB",
                0, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void deleteSameId() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(3);
        mLocalStorage.delete(3);

        Assert.assertEquals("delete same id",
                null, mLocalStorage.get(3));
    }

    @Test
    public void deleteAll() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.deleteAll();

        Assert.assertEquals("deleteAll",
                0, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void deleteAllWithEmptyDB() {
        mLocalStorage.deleteAll();

        Assert.assertEquals("deleteAll with empty DB",
                0, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void getOldestTimeNotification() {
        initData();
        mLocalStorage.put(entities);

        Assert.assertEquals("getOldestTimeNotification",
                entities.get(0).timestamp, mLocalStorage.getOldestTimeNotification());
    }

    @Test
    public void getOldestTimeNotificationWithEmptyDB() {
        Assert.assertEquals("getOldestTimeNotification with empty DB",
                0, mLocalStorage.getOldestTimeNotification());
    }

    @Test
    public void isNotificationExistedWithMtaid() {
        initData();
        mLocalStorage.put(entities);

        Assert.assertEquals("isNotificationExisted",
                false, mLocalStorage.isNotificationExisted(1, 0));
    }

    @Test
    public void isNotificationExistedWithMtuid() {
        initData();
        mLocalStorage.put(entities);

        Assert.assertEquals("isNotificationExisted",
                true, mLocalStorage.isNotificationExisted(0, 1));
    }

    @Test
    public void delete3Params() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(1, 3, 3);

        Assert.assertEquals("delete3Params",
                null, mLocalStorage.get(3));
    }

    @Test
    public void delete3ParamsWithUndefinedNotiType() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(2, 3, 3);

        Assert.assertEquals("delete3Params", TRANSACTION_SIZE, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void delete3ParamsWithUndefinedAppId() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(1, 0, 3);

        Assert.assertEquals("delete3Params with undefined app id", TRANSACTION_SIZE, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void delete3ParamsWithUndefinedTransId() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(1, 3, 0);

        Assert.assertEquals("delete3Params with undefined trans id", TRANSACTION_SIZE, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void delete3ParamsWithEmptyDB() {
        mLocalStorage.delete(0, 0, 0);
        Assert.assertEquals("delete3Params with empty DB",
                0, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void delete3ParamsSameId() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(1, 3, 3);
        mLocalStorage.delete(1, 3, 3);

        Assert.assertEquals("delete3Params same id", 19, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void delete2Params() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(1, 0);

        Assert.assertEquals("delete2Params", 19, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void delete2ParamsWithUndefinedMtaid() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(1, 1);

        Assert.assertEquals("delete2Params with undefined mtaid", TRANSACTION_SIZE, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void delete2ParamsWithUndefinedMtuid() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(0, 0);

        Assert.assertEquals("delete2Params with undefined mtuid", TRANSACTION_SIZE, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void delete2ParamsWithUndefined2Params() {
        initData();
        mLocalStorage.put(entities);

        mLocalStorage.delete(2, 3);

        Assert.assertEquals("delete2Params with undefined 2 params", TRANSACTION_SIZE, mLocalStorage.get(0, TRANSACTION_SIZE).size());
    }

    @Test
    public void setRecoveryWithTrue() {
        mLocalStorage.setRecovery(true);

        Assert.assertEquals("setRecovery with true param", true, mLocalStorage.isRecovery());
    }

    @Test
    public void setRecoveryWithFalse() {
        mLocalStorage.setRecovery(false);

        Assert.assertEquals("setRecovery with false param", false, mLocalStorage.isRecovery());
    }

    @Test
    public void isRecoveryWhenNotSetting() {
        Assert.assertEquals("isRecovery when not setting", false, mLocalStorage.isRecovery());
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