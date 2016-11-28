package vn.com.vng.zalopay.data.appresource;


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

import vn.com.vng.zalopay.data.AndroidApplicationTest;
import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.appresources.AppResourceLocalStorage;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.domain.model.AppResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by huuhoa on 6/18/16.
 * Unit tests for AppResourceLocalStorage
 */
public class AppResourceLocalStorageTest extends ApplicationTestCase{
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

        List<AppResourceEntity> result = mLocalStorage.getAllAppResource();

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

        List<AppResourceEntity> result = mLocalStorage.getAllAppResource();
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

        List<AppResourceEntity> result = mLocalStorage.getAllAppResource();
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

        List<AppResourceEntity> result = mLocalStorage.getAllAppResource();
        assertTrue(resourceEntityList.equals(result));
    }

    private AppResourceEntity createAppResource(int index) {
        AppResourceEntity appResourceEntity = new AppResourceEntity();
        appResourceEntity.appid = index;
        appResourceEntity.appname = "app name " + index;
        appResourceEntity.sortOrder = index;
        appResourceEntity.imageurl = "imageurl"+ index;
        appResourceEntity.jsurl = "jsurl" + index;
        appResourceEntity.stateDownload = 0;
        appResourceEntity.numRetry = 0;
        appResourceEntity.timeDownload = 0;
        return appResourceEntity;
    }

    @Test
    public void testSortApplication() {
        //B1: Delete all database & init new database
        mLocalStorage.getDaoSession().getAppResourceGDDao().detachAll();
        List<AppResourceEntity> appResourceEntities = new ArrayList<>();
        for (int i =0; i< 4; i++) {
            appResourceEntities.add(createAppResource(i));
        }
        mLocalStorage.put(appResourceEntities);

        //B2: Compare before sort
        List<Integer> sortAppSource = Arrays.asList(0,1,2,3,4);
        List<AppResourceEntity> appResourceList = mLocalStorage.getAllAppResource();
        for (int i = 0; i< appResourceList.size(); i++) {
            AppResourceEntity entity = appResourceList.get(i);
            assertEquals(entity.sortOrder, sortAppSource.indexOf(entity.appid));
        }

        //B3: Sort
        List<Integer> sortApps = Arrays.asList(1,4,3,2,0);
        mLocalStorage.sortApplication(sortApps);

        //B4: Compare after sort
        List<AppResourceEntity> appResourceList2 = mLocalStorage.getAllAppResource();
        for (int i = 0; i< appResourceList2.size(); i++) {
            AppResourceEntity entity = appResourceList2.get(i);
            assertEquals(entity.sortOrder, sortApps.indexOf(entity.appid));
        }
    }

}
