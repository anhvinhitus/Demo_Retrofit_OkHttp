package vn.com.vng.zalopay.data.notification;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

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

/**
 * Created by hieuvm on 12/22/16.
 */

public class NotificationLocalStorageTest extends ApplicationTestCase {

    private NotificationStore.LocalStorage mLocalStorage;

    private Gson mGson;

    private final int TRANSACTION_SIZE = 20; //row count jsonObject

    private List<NotificationData> entities = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new NotificationLocalStorage(daoSession);
        mGson = new Gson();
        initData();
    }

    private void initData() {

        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            NotificationData item = new NotificationData();
            item.mtuid = i;
            item.mtaid = 0;
            entities.add(item);
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
}