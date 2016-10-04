package vn.com.vng.zalopay.data;


import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new AppResourceLocalStorage(daoSession, new PlatformDaoMapper(), 1);
    }

    private AppResourceEntity createAppResourceEntity(int appid) {
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

    @Test
    public void testSetupUnitTest() {
        assertEquals(0, 0);
    }

    @Test
    public void testInsertAppResource() {
        List<AppResourceEntity> resourceEntityList = new ArrayList<>();

        resourceEntityList.add(createAppResourceEntity(11));
        resourceEntityList.add(createAppResourceEntity(12));
        resourceEntityList.add(createAppResourceEntity(13));
        mLocalStorage.put(resourceEntityList);

        List<AppResourceEntity> result = mLocalStorage.getInsideAppResource();

        assertTrue(resourceEntityList.equals(result));
    }

    @Test
    public void testIncreaseDownloadAppResource() {
        List<AppResourceEntity> resourceEntityList = new ArrayList<>();

        resourceEntityList.add(createAppResourceEntity(11));
        resourceEntityList.add(createAppResourceEntity(12));
        resourceEntityList.add(createAppResourceEntity(13));
        mLocalStorage.put(resourceEntityList);

        mLocalStorage.increaseStateDownload(11);
        mLocalStorage.increaseStateDownload(12);
        mLocalStorage.increaseStateDownload(13);
        mLocalStorage.increaseStateDownload(12);

        List<AppResourceEntity> result = mLocalStorage.getInsideAppResource();
        for (AppResourceEntity app : result) {
            switch (app.appid) {
                case 11:
                    assertEquals(app.stateDownload, 1);
                    break;
                case 12:
                    assertEquals(app.stateDownload, 2);
                    break;
                case 13:
                    assertEquals(app.stateDownload, 1);
            }
        }
    }

    @Test
    public void testIncreaseRetryDownloadAppResource() {
        List<AppResourceEntity> resourceEntityList = new ArrayList<>();

        resourceEntityList.add(createAppResourceEntity(11));
        resourceEntityList.add(createAppResourceEntity(12));
        resourceEntityList.add(createAppResourceEntity(13));
        mLocalStorage.put(resourceEntityList);

        mLocalStorage.increaseRetryDownload(11);
        mLocalStorage.increaseRetryDownload(12);
        mLocalStorage.increaseRetryDownload(13);
        mLocalStorage.increaseRetryDownload(12);

        List<AppResourceEntity> result = mLocalStorage.getInsideAppResource();
        for (AppResourceEntity app : result) {
            switch (app.appid) {
                case 11:
                    assertEquals(app.numRetry, 1);
                    break;
                case 12:
                    assertEquals(app.numRetry, 2);
                    break;
                case 13:
                    assertEquals(app.numRetry, 1);
            }
        }
    }

    @Test
    public void testUpdateAppResource() {
        List<AppResourceEntity> resourceEntityList = new ArrayList<>();

        resourceEntityList.add(createAppResourceEntity(11));
        resourceEntityList.add(createAppResourceEntity(12));
        resourceEntityList.add(createAppResourceEntity(13));
        mLocalStorage.put(resourceEntityList);

        // add app 14, remove app 11
        resourceEntityList.add(createAppResourceEntity(14));
        resourceEntityList.remove(0);

        // update new list
        mLocalStorage.put(resourceEntityList);
        mLocalStorage.updateAppList(Arrays.asList(12, 13, 14));

        List<AppResourceEntity> result = mLocalStorage.getInsideAppResource();
        assertTrue(resourceEntityList.equals(result));
    }
}
