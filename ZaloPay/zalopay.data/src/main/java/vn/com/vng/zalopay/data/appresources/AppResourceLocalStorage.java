package vn.com.vng.zalopay.data.appresources;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.AppResourceGD;
import vn.com.vng.zalopay.data.cache.model.AppResourceGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by huuhoa on 6/17/16.
 * Implementation for AppResource.LocalStorage
 */
public class AppResourceLocalStorage extends SqlBaseScopeImpl implements AppResourceStore.LocalStorage {
    private PlatformDaoMapper platformDaoMapper;
    private final int mAppId;

    public AppResourceLocalStorage(DaoSession daoSession, PlatformDaoMapper mapper, int appId) {
        super(daoSession);
        this.platformDaoMapper = mapper;
        this.mAppId = appId;
    }

    @Override
    public List<AppResourceEntity> getAllAppResource() {
        try {
            List<AppResourceGD> list = getAppInfoDao().queryBuilder().
                    orderAsc(AppResourceGDDao.Properties.SortOrder)
                    .list();
            return Lists.transform(list, platformDaoMapper::transform);
        } catch (android.database.sqlite.SQLiteException e) {
            Timber.d(e, "Get all app resource error");
            return new ArrayList<>();
        }
    }

    @Override
    public AppResourceEntity get(long appId) {
        List<AppResourceGD> appResourceGDList = getAppInfoDao().queryBuilder()
                .where(AppResourceGDDao.Properties.Appid.eq(appId))
                .limit(1)
                .list();
        List<AppResourceEntity> entityList = Lists.transform(appResourceGDList, platformDaoMapper::transform);
        if (Lists.isEmptyOrNull(entityList)) {
            return null;
        }
        return entityList.get(0);
    }

    @Override
    public void put(AppResourceEntity appResourceEntity) {
        AppResourceGD appResourceGD = platformDaoMapper.transform(appResourceEntity);
        if (appResourceGD == null) {
            return;
        }
        getAppInfoDao().insertOrReplaceInTx(appResourceGD);
    }

    @Override
    public void put(List<AppResourceEntity> resourceEntities) {
        List<AppResourceGD> list = Lists.transform(resourceEntities, platformDaoMapper::transform);
        if (Lists.isEmptyOrNull(list)) {
            return;
        }

        for (AppResourceGD appResource : list) {
            appResource.downloadState = 0L;
            appResource.retryNumber = 0L;
            appResource.downloadTime = 0L;
        }

        getAppInfoDao().insertOrReplaceInTx(list);
    }

    @Override
    public void updateAppList(List<Long> listAppId) {
        getAppInfoDao().queryBuilder()
                .where(AppResourceGDDao.Properties.Appid.notIn(listAppId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities(); // TOdo: chú ý có khi trong session/cache van ton tai. (get App !=null)
    }

    @Override
    public void increaseDownloadState(long appId) {
        Timber.d("Increase state download : appId [%s]", appId);
        List<AppResourceGD> appResourceGD = getAppInfoDao().queryBuilder()
                .where(AppResourceGDDao.Properties.Appid.eq(appId))
                .list();
        if (Lists.isEmptyOrNull(appResourceGD)) {
            return;
        }

        for (AppResourceGD app : appResourceGD) {

            long state = app.downloadState == null ? 0 : app.downloadState + 1;
            app.downloadState = state;
            if (state >= 2) {
                app.retryNumber = (0L);
                app.downloadTime = (0L);
            }
        }

        getAppInfoDao().insertOrReplaceInTx(appResourceGD);
    }

    @Override
    public void resetDownloadState(long appId) {
        AppResourceEntity appResourceEntity = get(appId);
        if (appResourceEntity == null) {
            return;
        }
        appResourceEntity.downloadState = 0;
        put(appResourceEntity);
    }

    @Override
    public void resetResourceState(long appId) {
        AppResourceEntity appResourceEntity = get(appId);
        if (appResourceEntity == null) {
            Timber.d("ResetStateResource but find not found appId [%s]", appId);
            return;
        }
        Timber.d("ResetStateResource appId [%s]", appId);
        appResourceEntity.checksum = "";
        appResourceEntity.downloadState = 0;
        put(appResourceEntity);
    }

    @Override
    public void increaseRetryDownload(long appId) {
        List<AppResourceGD> appResourceGD = getAppInfoDao()
                .queryBuilder()
                .where(AppResourceGDDao.Properties.Appid.eq(appId))
                .list();

        if (Lists.isEmptyOrNull(appResourceGD)) return;

        long currentTime = System.currentTimeMillis() / 1000;
        for (AppResourceGD app : appResourceGD) {
            app.retryNumber = (app.retryNumber + 1);
            app.downloadTime = (currentTime);
        }

        getAppInfoDao().insertOrReplaceInTx(appResourceGD);
    }

    @Override
    public void sortApplication(List<Long> list) {
        List<AppResourceGD> appResourceGD = getAppInfoDao().queryBuilder()
                .list();
        if (Lists.isEmptyOrNull(appResourceGD)) {
            return;
        }

        for (AppResourceGD resourceGD : appResourceGD) {
            resourceGD.sortOrder = (long) (list.indexOf(resourceGD.appid));
        }

        getAppInfoDao().insertOrReplaceInTx(appResourceGD);
    }
}
