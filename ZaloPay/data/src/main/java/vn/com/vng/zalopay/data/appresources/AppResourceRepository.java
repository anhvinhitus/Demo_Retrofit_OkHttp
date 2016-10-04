package vn.com.vng.zalopay.data.appresources;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.util.ListStringUtil;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by huuhoa on 6/17/16.
 * Implementation for AppResource.Repository
 */
public class AppResourceRepository implements AppResourceStore.Repository {

    private AppConfigEntityDataMapper mAppConfigEntityDataMapper;
    private HashMap<String, String> mRequestParameters;
    private DownloadAppResourceTaskQueue taskQueue;

    private OkHttpClient mOkHttpClient;

    private final boolean mDownloadAppResource;
    private final String mRootBundle;
    private final AppResourceStore.RequestService mRequestService;
    private final AppResourceStore.LocalStorage mLocalStorage;
    private String appVersion;

    public AppResourceRepository(AppConfigEntityDataMapper mapper,
                                 AppResourceStore.RequestService requestService,
                                 AppResourceStore.LocalStorage localStorage,
                                 HashMap<String, String> requestParameters,
                                 DownloadAppResourceTaskQueue taskQueue,
                                 OkHttpClient okHttpClient,
                                 boolean download,
                                 String rootBundle,
                                 String appVersion

    ) {
        this.mAppConfigEntityDataMapper = mapper;
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
        this.mRootBundle = rootBundle;
        this.mRequestParameters = requestParameters;
        this.taskQueue = taskQueue;
        this.mOkHttpClient = okHttpClient;
        this.mDownloadAppResource = download;
        this.appVersion = appVersion;
    }

    @Override
    public Observable<Boolean> initialize() {
        return ObservableHelper.makeObservable(() -> {
            ensureAppResourceAvailable();
            return Boolean.TRUE;
        });
    }

    @Override
    public List<AppResource> listAppResourceFromDB() {
        return mAppConfigEntityDataMapper.transformAppResourceEntity(mLocalStorage.getInsideAppResource());
    }

    @Override
    public Observable<List<AppResource>> listAppResource() {
        return Observable.concat(
                ObservableHelper.makeObservable(mLocalStorage::getInsideAppResource),
                fetchAppResource().flatMap(appResourceResponse -> ObservableHelper.makeObservable(mLocalStorage::getInsideAppResource)))
                //.delaySubscription(200, TimeUnit.MILLISECONDS)
                .map(o -> mAppConfigEntityDataMapper.transformAppResourceEntity(o));
    }

    private Observable<AppResourceResponse> fetchAppResource() {
        List<AppResource> appResources = listAppResourceFromDB();
        List<String> checksumlist = new ArrayList<>();
        if (!Lists.isEmptyOrNull(appResources)) {
            listAppIdAndChecksum(appResources, checksumlist);
        }

        String appIds = ListStringUtil.toStringListAppId(appResources);
        String checkSum = checksumlist.toString();
        Timber.d("appIds react-native %s checkSum %s", appIds, checkSum);

        return mRequestService.insideappresource(appIds, checkSum, mRequestParameters, appVersion)
                .doOnNext(this::processAppResourceResponse)
                ;
    }

    private void ensureAppResourceAvailable() {
        List<AppResourceEntity> list = mLocalStorage.getInsideAppResource();
        List<AppResourceEntity> listAppDownload = new ArrayList<>();
        for (AppResourceEntity app : list) {
            if (shouldDownloadApp(app)) {
                listAppDownload.add(app);
            }
        }

        if (!listAppDownload.isEmpty()) {
            startDownloadService(listAppDownload, null);
        }
    }

    private boolean shouldDownloadApp(AppResourceEntity app) {
        if (app.stateDownload < 2) {
            if (app.numRetry < 3) {
                return true;
            } else {
                long currentTime = System.currentTimeMillis() / 1000;
                if (currentTime - app.timeDownload >= 4 * 60 * 60) {
                    return true;
                }
            }
        }
        return false;
    }

    private void listAppIdAndChecksum(List<AppResource> appResources, List<String> checksumlist) {
        if (Lists.isEmptyOrNull(appResources)) {
            return;
        }

        for (AppResource appResource: appResources) {
            if (appResource == null) {
                continue;
            }
            AppResourceEntity appResourceEntity = mLocalStorage.get(appResource.appid);
            if (appResourceEntity == null) {
                checksumlist.add("");
            } else {
                checksumlist.add(appResourceEntity.checksum);
            }
        }
    }

    private void processAppResourceResponse(AppResourceResponse resourceResponse) {
        List<Integer> listAppId = resourceResponse.appidlist;

        if (!Lists.isEmptyOrNull(resourceResponse.resourcelist)) {
            List<AppResourceEntity> resourcelist = new ArrayList<>();
            for (int i = 0; i < resourceResponse.resourcelist.size(); i++) {
                AppResourceEntity appResourceEntity = resourceResponse.resourcelist.get(i);
                if (!TextUtils.isEmpty(appResourceEntity.iconurl)) {
                    appResourceEntity.iconurl = resourceResponse.baseurl + appResourceEntity.iconurl;
                }
                int index = resourceResponse.orderedInsideApps.indexOf(appResourceEntity.appid);
                Timber.d("processAppResourceResponse appid [%s] index [%s]", appResourceEntity.appid, index);
                appResourceEntity.sortOrder = index;
                resourcelist.add(appResourceEntity);
            }

            Timber.d("baseurl %s listAppId %s resourcelistSize %s", resourceResponse.baseurl, listAppId, resourcelist.size());
            startDownloadService(resourcelist, resourceResponse.baseurl);
            mLocalStorage.put(resourcelist);
        } else if (!Lists.isEmptyOrNull(resourceResponse.orderedInsideApps)) {
            updateInsideAppIndex(resourceResponse.orderedInsideApps);
        }

        mLocalStorage.updateAppList(listAppId);
    }

    private void updateInsideAppIndex(List<Integer> orderedInsideApps) {
        if (Lists.isEmptyOrNull(orderedInsideApps)) {
            return;
        }
        List<AppResourceEntity> appResourceEntities = mLocalStorage.getInsideAppResource();
        if (Lists.isEmptyOrNull(appResourceEntities)) {
            return;
        }
        for (int i = 0; i < appResourceEntities.size(); i++) {
            appResourceEntities.get(i).sortOrder = orderedInsideApps.indexOf(appResourceEntities.get(i).appid);
        }
        mLocalStorage.put(appResourceEntities);
    }


    private void startDownloadService(List<AppResourceEntity> resource, String baseUrl) {
        if (!mDownloadAppResource) {
            return;
        }

        List<DownloadAppResourceTask> needDownloadList = new ArrayList<>();
        for (AppResourceEntity appResourceEntity : resource) {

            if (!TextUtils.isEmpty(baseUrl)) {
                appResourceEntity.jsurl = baseUrl + appResourceEntity.jsurl;
                appResourceEntity.imageurl = baseUrl + appResourceEntity.imageurl;
            }

            if (appResourceEntity.needdownloadrs == 1) {
                createTask(appResourceEntity, needDownloadList);
            }
        }

        if (needDownloadList.isEmpty()) {
            return;
        }

        Timber.d("Start download %s", needDownloadList.size());
        taskQueue.enqueue(needDownloadList);
    }

    private void createTask(AppResourceEntity appResourceEntity, List<DownloadAppResourceTask> listTask) {

        DownloadAppResourceTask taskJs = new DownloadAppResourceTask(
                new DownloadInfo(appResourceEntity.jsurl, appResourceEntity.appname,
                        appResourceEntity.appid, appResourceEntity.checksum),
                mOkHttpClient, mLocalStorage, mRootBundle);

        listTask.add(taskJs);

        DownloadAppResourceTask taskImgUrl = new DownloadAppResourceTask(
                new DownloadInfo(appResourceEntity.imageurl, appResourceEntity.appname,
                        appResourceEntity.appid, appResourceEntity.checksum),
                mOkHttpClient, mLocalStorage, mRootBundle);

        listTask.add(taskImgUrl);
    }
}
