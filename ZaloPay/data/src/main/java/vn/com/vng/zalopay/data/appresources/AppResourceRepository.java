package vn.com.vng.zalopay.data.appresources;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.AppResource;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by huuhoa on 6/17/16.
 * Implementation for AppResource.Repository
 */
public class AppResourceRepository implements AppResourceStore.Repository {

    private AppConfigEntityDataMapper mDataMapper;
    private HashMap<String, String> mRequestParameters;
    private DownloadAppResourceTaskQueue mTaskQueue;

    private OkHttpClient mOkHttpClient;

    private final boolean mDownloadAppResource;
    private final String mRootBundle;
    private final AppResourceStore.RequestService mRequestService;
    private final AppResourceStore.LocalStorage mLocalStorage;
    private String mAppVersion;
    //Retry 3 times, each time to download 2 file (js & image)
    private static final int RETRY_DOWNLOAD_NUMBER = 6;
    //List AppID that exclude download
    private final List<Long> mListAppIdExcludeDownload;

    private final List<AppResource> mListDefaultApp;

    private final List<AppResource> mListExcludeApp;

    private long mLastTimeFetchApplication;

    public AppResourceRepository(AppConfigEntityDataMapper mapper,
                                 AppResourceStore.RequestService requestService,
                                 AppResourceStore.LocalStorage localStorage,
                                 HashMap<String, String> requestParameters,
                                 DownloadAppResourceTaskQueue taskQueue,
                                 OkHttpClient okHttpClient,
                                 boolean download,
                                 String rootBundle,
                                 String appVersion,
                                 List<Long> excludeDownloadApps,
                                 List<AppResource> listDefaultApp,
                                 List<AppResource> listExcludeApp
    ) {
        this.mDataMapper = mapper;
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
        this.mRootBundle = rootBundle;
        this.mRequestParameters = requestParameters;
        this.mTaskQueue = taskQueue;
        this.mOkHttpClient = okHttpClient;
        this.mDownloadAppResource = download;
        this.mAppVersion = appVersion;
        this.mListAppIdExcludeDownload = excludeDownloadApps;

        this.mListDefaultApp = listDefaultApp;
        this.mListExcludeApp = listExcludeApp;
    }

    @Override
    public Observable<Boolean> ensureAppResourceAvailable() {
        return makeObservable(() -> {
            List<AppResourceEntity> appLists = mLocalStorage.getAllAppResource();

            if (Lists.isEmptyOrNull(appLists)) {
                return Boolean.TRUE;
            }

            List<AppResourceEntity> listAppDownload = new ArrayList<>();
            for (AppResourceEntity app : appLists) {
                if (shouldDownloadApp(app)) {
                    listAppDownload.add(app);
                }
            }

            if (!listAppDownload.isEmpty()) {
                startDownloadService(listAppDownload, null);
            }

            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<List<AppResource>> listInsideAppResource() {
        return Observable.concat(getAppResourceEntityLocal(),
                fetchInsideAppResource()
                        .flatMap(response -> getAppResourceEntityLocal())
                        .onErrorResumeNext(throwable -> Observable.empty())
        ).map(mDataMapper::transformAppResourceEntity);
    }

    private Observable<AppResourceResponse> fetchInsideAppResource() {

        List<String> appidlist = new ArrayList<>();
        List<String> checksumlist = new ArrayList<>();

        listAppIdAndChecksum(appidlist, checksumlist);

        String appIds = appidlist.toString().replaceAll("\\s", "");
        String checkSum = checksumlist.toString();

        Timber.d("appIds react-native %s checkSum %s", appIds, checkSum);

        return mRequestService.getinsideappresource(appIds, checkSum, mRequestParameters, mAppVersion)
                .doOnNext(this::processAppResourceResponse)
                ;
    }

    private void resetStateDownloadApp(AppResourceEntity app) {
        if (app == null) {
            return;
        }
        app.numRetry = 0;
        app.timeDownload = 0L;
        app.stateDownload = 0;

        mLocalStorage.put(app);
    }

    private boolean shouldDownloadApp(AppResourceEntity app) {
        Timber.d("shouldDownloadApp appId[%s] stateDownload[%s]", app.appid, app.stateDownload);
        if (mListAppIdExcludeDownload != null && mListAppIdExcludeDownload.contains(app.appid)) {
            Timber.d("Exclude download app[%s]", app.appid);
            return false;
        }
        if (app.stateDownload < DownloadState.STATE_SUCCESS) {
            if (app.numRetry < RETRY_DOWNLOAD_NUMBER) {
                return true;
            } else {
                long currentTime = System.currentTimeMillis() / 1000;
                if (currentTime - app.timeDownload >= 60) {
                    resetStateDownloadApp(app);
                    return true;
                }
            }
        }
        return false;
    }

    private void listAppIdAndChecksum(List<String> listAppId, List<String> checksumlist) {
        listAppIdAndChecksum(listAppId, checksumlist, mLocalStorage.getAllAppResource());
    }

    private void processAppResourceResponse(AppResourceResponse resourceResponse) {

        if (!Lists.isEmptyOrNull(resourceResponse.resourcelist)) {
            List<AppResourceEntity> resourcelist = new ArrayList<>();
            for (int i = 0; i < resourceResponse.resourcelist.size(); i++) {
                AppResourceEntity appResourceEntity = resourceResponse.resourcelist.get(i);
                if (!TextUtils.isEmpty(appResourceEntity.iconurl)) {
                    appResourceEntity.iconurl = resourceResponse.baseurl + appResourceEntity.iconurl;
                }
                int index = resourceResponse.orderedInsideApps.indexOf(appResourceEntity.appid);
                Timber.d("processAppResourceResponse appId [%s] index [%s]", appResourceEntity.appid, index);
                appResourceEntity.sortOrder = index;
                if (mListAppIdExcludeDownload != null
                        && mListAppIdExcludeDownload.contains(appResourceEntity.appid)) {
                    appResourceEntity.needdownloadrs = 0;
                }
                resourcelist.add(appResourceEntity);
            }

            Timber.d("baseUrl [%s] resourceListSize [%s]", resourceResponse.baseurl, resourcelist.size());
            startDownloadService(resourcelist, resourceResponse.baseurl);
            mLocalStorage.put(resourcelist);
        } else if (!Lists.isEmptyOrNull(resourceResponse.orderedInsideApps)) {
            updateInsideAppIndex(resourceResponse.orderedInsideApps);
        }

        List<Long> listAppId = resourceResponse.appidlist;
        mLocalStorage.updateAppList(listAppId);
        mLastTimeFetchApplication = System.currentTimeMillis() / 1000;
    }

    private void updateInsideAppIndex(List<Long> orderedInsideApps) {
        if (Lists.isEmptyOrNull(orderedInsideApps)) {
            return;
        }
        List<AppResourceEntity> appResourceEntities = mLocalStorage.getAllAppResource();
        if (Lists.isEmptyOrNull(appResourceEntities)) {
            return;
        }
        for (AppResourceEntity entity : appResourceEntities) {
            entity.sortOrder = orderedInsideApps.indexOf(entity.appid);
        }

        mLocalStorage.sortApplication(orderedInsideApps);
    }


    private void startDownloadService(List<AppResourceEntity> resource, String baseUrl) {
        Timber.d("startDownloadService mDownloadAppResource [%s]", mDownloadAppResource);
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
                mLocalStorage.resetStateDownload(appResourceEntity.appid);
            }
        }

        if (needDownloadList.isEmpty()) {
            return;
        }

        Timber.d("Start download %s", needDownloadList.size());
        mTaskQueue.enqueue(needDownloadList);
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

    @Override
    public Observable<Boolean> existResource(long appId) {
        return makeObservable(() -> {
            AppResourceEntity entity = mLocalStorage.get(appId);
            Timber.d("existResource appId [%s] state [%s]", appId, entity.stateDownload);
            boolean downloadSuccess = (entity.stateDownload >= DownloadState.STATE_SUCCESS);
            if (!downloadSuccess) {
                startDownloadService(Arrays.asList(entity), null);
            }
            return downloadSuccess;
        });
    }

    interface DownloadState {
        int STATE_FAIL = -1;
        int STATE_DOWNLOADING = 1;
        int STATE_SUCCESS = 2;
    }

    @Override
    public Observable<List<AppResource>> fetchAppResource() {
        return getAppResourceEntityLocal()
                .flatMap(this::fetchAppResource)
                .flatMap(response -> getAppResourceLocal())
                ;
    }

    private Observable<AppResourceResponse> fetchAppResource(List<AppResourceEntity> entities) {
        Timber.d("Fetch app resource");

        List<String> appidlist = new ArrayList<>();
        List<String> checksumlist = new ArrayList<>();
        listAppIdAndChecksum(appidlist, checksumlist, entities);
        return fetchAppResource(appidlist, checksumlist);
    }

    private Observable<AppResourceResponse> fetchAppResource(List<String> appidlist, List<String> checksumlist) {
        String appIds = appidlist.toString().replaceAll("\\s", "");
        String checkSum = checksumlist.toString();
        Timber.d("Fetch application resource appId [%s] checkSum [%s] ", appIds, checkSum);
        return fetchAppResource(appIds, checkSum);
    }

    private Observable<AppResourceResponse> fetchAppResource(String appIds, String checkSum) {
        return mRequestService.getinsideappresource(appIds, checkSum, mRequestParameters, mAppVersion)
                .doOnNext(this::processAppResourceResponse)
                ;
    }

    private void listAppIdAndChecksum(List<String> listAppId, List<String> checksumlist, List<AppResourceEntity> appResources) {
        if (Lists.isEmptyOrNull(appResources)) {
            return;
        }

        for (AppResourceEntity appResource : appResources) {
            if (appResource == null) {
                continue;
            }

            listAppId.add(String.valueOf(appResource.appid));
            checksumlist.add(appResource.checksum);
        }
    }

    private Observable<List<AppResourceEntity>> getAppResourceEntityLocal() {
        return makeObservable(mLocalStorage::getAllAppResource);
    }

    @Override
    public Observable<List<AppResource>> getAppResourceLocal() {
        return getAppResourceEntityLocal()
                .map(mDataMapper::transformAppResourceEntity);
    }

    @Override
    public Observable<List<AppResource>> getListAppHome() {
        Observable<List<AppResource>> local = getAppResourceLocal();
        Observable<List<AppResource>> cloud = fetchAppResource()
                .onErrorResumeNext(throwable -> Observable.just(new ArrayList<>(mListDefaultApp)));
        Observable<List<AppResource>> source = Observable.concat(local, cloud);
        if (isUpToDate()) {
            return source
                    .takeFirst(resources -> !Lists.isEmptyOrNull(resources) && resources.size() > 0)
                    .map(this::transform);
        } else {
            return source
                    .throttleLast(200, TimeUnit.MILLISECONDS)
                    .map(this::transform);
        }

    }

    @Override
    public Observable<List<AppResource>> fetchListAppHome() {
        return fetchAppResource()
                .map(this::transform);
    }

    private List<AppResource> transform(List<AppResource> resources) {
        ArrayList<AppResource> listApp = new ArrayList<>(mListDefaultApp);
        Timber.d("app default size [%s]", listApp.size());
        if (resources.containsAll(listApp)) {
            resources.removeAll(listApp);
        }
        listApp.addAll(resources);
        listApp.removeAll(mListExcludeApp);
        Timber.d("app show in home page: %s", listApp.size());
        return listApp;
    }


    private boolean isUpToDate() {
        boolean isUpToDate = false;
        if (mLastTimeFetchApplication > 0) {
            long time = Math.abs(System.currentTimeMillis() / 1000 - mLastTimeFetchApplication);
            if (time < 3 * 60) { // 3min
                isUpToDate = true;
            }
        }
        Timber.d("isUpToDate: [%s]", isUpToDate);
        return isUpToDate;
    }

}