package vn.com.vng.zalopay.data;


import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.appresources.AppResourceLocalStorage;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by huuhoa on 6/18/16.
 * Unit tests for AppResourceLocalStorage
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class AppResourceLocalStorageTest {
    private AppResourceLocalStorage mLocalStorage;
    private DaoSession mDaoSession;

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        mDaoSession = new DaoMaster(db).newSession();
        mLocalStorage = new AppResourceLocalStorage(mDaoSession, new PlatformDaoMapper());
    }

    @Test
    public void testSetupUnitTest() {
        assertEquals(0, 0);
    }

    @Test
    public void testInsertAppResource() {
        List<AppResourceEntity> resourceEntityList = new ArrayList<>();

        resourceEntityList.add(createAppResourceEntity(11));
        mLocalStorage.put(resourceEntityList);

        List<AppResourceEntity> result = mLocalStorage.get();

        assertTrue(resourceEntityList.equals(result));
    }

    AppResourceEntity createAppResourceEntity(int appid) {
        AppResourceEntity entity = new AppResourceEntity();
        entity.appid = appid;
        entity.appname = "Nạp tiền ĐT";
        entity.checksum = "11";
        entity.imageurl = "imageurl";
        entity.jsurl = "jsurl";
        entity.needdownloadrs = 1;
        entity.numRetry = 0;
        entity.status = 1;
        entity.timeDownload = 0;
        entity.stateDownload = 0;
        return entity;
    }
}
