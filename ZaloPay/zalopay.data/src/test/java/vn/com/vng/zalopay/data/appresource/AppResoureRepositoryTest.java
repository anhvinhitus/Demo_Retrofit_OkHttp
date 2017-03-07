package vn.com.vng.zalopay.data.appresource;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.observers.TestSubscriber;
import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.appresources.AbsDownloadService;
import vn.com.vng.zalopay.data.appresources.AppResourceLocalStorage;
import vn.com.vng.zalopay.data.appresources.AppResourceRepository;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.appresources.DownloadAppResourceTaskQueue;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.domain.model.AppResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by longlv on 11/26/16.
 * Unit test cho AppResourceLocalStorage.
 * kiểm tra thay đổi thứ tự app, thêm bớt app.
 */

public class AppResoureRepositoryTest extends ApplicationTestCase {

    private AppResourceStore.LocalStorage mAppResourceLocalStorage;
    private AppResourceStore.Repository mRepository;
    private AppResourceStore.RequestService mRequestService;

    @Before
    public void setUpEnvironment() {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, "zalopaytest.db", null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mAppResourceLocalStorage = new AppResourceLocalStorage(daoSession, new PlatformDaoMapper(), 1);
        mRequestService = new AppResourceStore.RequestService() {

            @Override
            public Observable<AppResourceResponse> getinsideappresource(@Query(value = "appidlist", encoded = false) String appidlist, @Query(value = "checksumlist", encoded = true) String checksumlist, @QueryMap HashMap<String, String> params, @Query("appversion") String appVersion) {
                AppResourceResponse resourceResponse = new AppResourceResponse();
                resourceResponse.resourcelist = listUpgradeData();
                resourceResponse.orderedInsideApps = mSortAppSource;
                resourceResponse.appidlist = mAppList;
                resourceResponse.baseurl = "";
                resourceResponse.expiredtime = System.currentTimeMillis();
                return Observable.just(resourceResponse);
            }
        };

        List<AppResource> appResourceList = new ArrayList<>();
        List<AppResource> excludeAppResourceList = new ArrayList<>();

        mRepository = new AppResourceRepository(new AppConfigEntityDataMapper(),
                mRequestService, mAppResourceLocalStorage, new HashMap<>(),
                new DownloadAppResourceTaskQueue(RuntimeEnvironment.application, AbsDownloadService.class),
                new OkHttpClient(),
                true,
                "rootBundle",
                "2.4.0",
                Arrays.asList(6L), appResourceList, excludeAppResourceList);
    }

    private AppResourceEntity createAppResource(int index) {
        AppResourceEntity appResourceEntity = new AppResourceEntity();
        appResourceEntity.appid = index;
        appResourceEntity.appname = "app name " + index;
        appResourceEntity.sortOrder = index;
        appResourceEntity.imageurl = "imageurl" + index;
        appResourceEntity.jsurl = "jsurl" + index;
        appResourceEntity.iconName = "iconName" + index;
        appResourceEntity.iconColor = "iconColor" + index;
        appResourceEntity.weburl = "webUrl" + index;
        appResourceEntity.stateDownload = 0;
        appResourceEntity.numRetry = 0;
        appResourceEntity.timeDownload = 0;
        return appResourceEntity;
    }

    private AppResourceEntity createUpgradeAppResource(int index) {
        AppResourceEntity appResourceEntity = new AppResourceEntity();
        appResourceEntity.appid = index;
        appResourceEntity.appname = "Upgrade app name " + index;
        appResourceEntity.sortOrder = index;
        appResourceEntity.imageurl = "Upgrade imageurl" + index;
        appResourceEntity.jsurl = "Upgrade jsurl" + index;
        appResourceEntity.iconName = "Upgrade iconName" + index;
        appResourceEntity.iconColor = "Upgrade iconColor" + index;
        appResourceEntity.weburl = "Upgrade webUrl" + index;
        appResourceEntity.stateDownload = 0;
        appResourceEntity.numRetry = 0;
        appResourceEntity.timeDownload = 0;
        return appResourceEntity;
    }

    //AppId [0 ->3]
    private List<AppResourceEntity> listInitData() {
        List<AppResourceEntity> appResourceEntities = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            appResourceEntities.add(createAppResource(i));
        }
        return appResourceEntities;
    }

    //AppId [0-1], [3-4]
    private List<Long> mAppList = Arrays.asList(0L, 1L, 3L, 4L);
    private List<Long> mSortAppSource = Arrays.asList(0L, 1L, 3L, 4L);

    private List<AppResourceEntity> listUpgradeData() {
        List<AppResourceEntity> appResourceEntities = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (i == 2) {
                continue;
            }
            appResourceEntities.add(createUpgradeAppResource(i));
        }
        return appResourceEntities;
    }

    @Test
    public void testUpgradeData() {
        //B1: init new database
        List<AppResourceEntity> listAppSources = listInitData();
        mAppResourceLocalStorage.put(listAppSources);

        TestSubscriber testSubscriber = new TestSubscriber<List<AppResource>>();

        //B2: upgrade list app
        mRepository.listInsideAppResource().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        compareData(mAppResourceLocalStorage.getAllAppResource(), listUpgradeData());
        compareSortOrder(mAppResourceLocalStorage.getAllAppResource(), mSortAppSource);
    }

    private void compareData(List<AppResourceEntity> allAppResource, List<AppResourceEntity> appResourceEntities) {
        assertEquals(allAppResource.size(), appResourceEntities.size());
        if (allAppResource.size() != appResourceEntities.size()) {
            return;
        }
        for (int i = 0; i < allAppResource.size(); i++) {
            AppResourceEntity appResource = allAppResource.get(i);
            AppResourceEntity entity = appResourceEntities.get(i);
            assertTrue(compareAppResource(appResource, entity));
        }
    }

    private void compareSortOrder(List<AppResourceEntity> allAppResource, List<Long> sortAppSource) {
        for (int i = 0; i < allAppResource.size(); i++) {
            AppResourceEntity entity = allAppResource.get(i);
            assertTrue(entity.sortOrder == sortAppSource.indexOf(entity.appid));
        }
    }

    private boolean compareAppResource(AppResourceEntity appResource1, AppResourceEntity appResource2) {
        if (appResource1.appid != appResource2.appid) {
            return false;
        } else if (!appResource1.appname.equals(appResource2.appname)) {
            return false;
        } else if (!appResource1.imageurl.equals(appResource2.imageurl)) {
            return false;
        } else if (!appResource1.jsurl.equals(appResource2.jsurl)) {
            return false;
        } else if (!appResource1.iconName.equals(appResource2.iconName)) {
            return false;
        } else if (!appResource1.iconColor.equals(appResource2.iconColor)) {
            return false;
        } else if (!appResource1.weburl.equals(appResource2.weburl)) {
            return false;
        }
        return true;
    }
}

