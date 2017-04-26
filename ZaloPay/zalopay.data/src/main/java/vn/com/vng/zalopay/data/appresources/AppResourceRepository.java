package vn.com.vng.zalopay.data.appresources;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
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
                                 String appVersion,
                                 List<Long> excludeDownloadApps,
                                 List<AppResource> listDefaultApp,
                                 List<AppResource> listExcludeApp
    ) {
        this.mDataMapper = mapper;
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
        this.mRootBundle = ResourceHelper.getBundleRootFolder();
        this.mRequestParameters = requestParameters;
        this.mTaskQueue = taskQueue;
        this.mOkHttpClient = okHttpClient;
        this.mDownloadAppResource = download;
        this.mAppVersion = appVersion;
        this.mListAppIdExcludeDownload = excludeDownloadApps;

        this.mListDefaultApp = listDefaultApp;
        this.mListExcludeApp = listExcludeApp;
    }

    private Boolean ensureResourceAvailable() {
        Timber.d("Ensure resource available, download if necessary.");
        if (mTaskQueue.isRunningDownloadService()) {
            Timber.d("Service download resource is running. Return and wait result.");
            return Boolean.FALSE;
        }

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
            startDownloadService(listAppDownload);
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }

    }

    @Override
    public Observable<Boolean> ensureAppResourceAvailable() {
        return makeObservable(this::ensureResourceAvailable);
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

        Timber.d("Fetch insdie app : appIds [%s] checkSum [%s]", appIds, checkSum);

        return mRequestService.getinsideappresource(appIds, checkSum, mRequestParameters, mAppVersion)
                .doOnNext(this::processAppResourceResponse)
                .doOnTerminate(this::ensureResourceAvailable)
                ;
    }

    private void resetStateDownloadApp(AppResourceEntity app) {
        if (app == null) {
            return;
        }
        app.retryNumber = 0;
        app.downloadTime = 0L;
        app.downloadState = 0;

        mLocalStorage.put(app);
    }

    private boolean shouldDownloadApp(AppResourceEntity app) {
        Timber.d("Should download app : appId [%s] downloadState [%s]", app.appid, app.downloadState);
        if (mListAppIdExcludeDownload != null && mListAppIdExcludeDownload.contains(app.appid)) {
            Timber.d("Exclude download : appId [%s]", app.appid);
            return false;
        }
        if (app.downloadState < DownloadState.STATE_SUCCESS) {
            if (app.retryNumber < RETRY_DOWNLOAD_NUMBER) {
                return true;
            } else {
                long currentTime = System.currentTimeMillis() / 1000;
                if (currentTime - app.downloadTime >= 60) {
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
            String baseUrl = resourceResponse.baseurl;
            for (int i = 0; i < resourceResponse.resourcelist.size(); i++) {
                AppResourceEntity appResourceEntity = resourceResponse.resourcelist.get(i);
                int index = resourceResponse.orderedInsideApps.indexOf(appResourceEntity.appid);
                Timber.d("Process app resource response, appId [%s] index [%s]",
                        appResourceEntity.appid, index);
                appResourceEntity.sortOrder = index;
                if (!TextUtils.isEmpty(baseUrl)) {
                    appResourceEntity.imageurl = baseUrl + appResourceEntity.imageurl;
                    appResourceEntity.jsurl = baseUrl + appResourceEntity.jsurl;
                }
                if (mListAppIdExcludeDownload != null
                        && mListAppIdExcludeDownload.contains(appResourceEntity.appid)) {
                    appResourceEntity.needdownloadrs = 0;
                }
                resourcelist.add(appResourceEntity);
            }

            Timber.d("Process app resource response, baseUrl [%s] resourceListSize [%s]",
                    baseUrl, resourcelist.size());
            mLocalStorage.put(resourcelist);
            stopDownloadService(); //stop download older resource before start download new resource.
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

    private void stopDownloadService() {
        mTaskQueue.clearTaskAndStopDownloadService();
    }

    private void startDownloadService(List<AppResourceEntity> resource) {
        if (!mDownloadAppResource) {
            Timber.d("Download service terminate because config DOWNLOAD_APP_RESOURCE is false.");
            return;
        }

        List<DownloadAppResourceTask> needDownloadList = new ArrayList<>();
        for (AppResourceEntity appResourceEntity : resource) {
            if (appResourceEntity.needdownloadrs == 1) {
                createTask(appResourceEntity, needDownloadList);
                mLocalStorage.resetDownloadState(appResourceEntity.appid);
            }
        }

        if (needDownloadList.isEmpty()) {
            Timber.d("No app need download resource.");
            return;
        }

        Timber.d("Start download service, size [%s]", needDownloadList.size());
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
    public Observable<Boolean> existResource(long appId, boolean downloadIfNeed) {
        return makeObservable(() -> {
            AppResourceEntity entity = mLocalStorage.get(appId);
            Timber.d("Exist resource : appId [%s] state [%s]", appId, entity.downloadState);
            boolean downloadSuccess = (entity.downloadState >= DownloadState.STATE_SUCCESS);
            if (!downloadSuccess && downloadIfNeed) {
                startDownloadService(Collections.singletonList(entity));
            }
            return downloadSuccess;
        });
    }

    @Override
    public Observable<Boolean> existResource(long appId) {
        return existResource(appId, true);
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
        Timber.d("Fetch application resource : appId [%s] checkSum [%s] ", appIds, checkSum);
        return fetchAppResource(appIds, checkSum);
    }

    private Observable<AppResourceResponse> fetchAppResource(String appIds, String checkSum) {
        return mRequestService.getinsideappresource(appIds, checkSum, mRequestParameters, mAppVersion)
                .doOnNext(this::processAppResourceResponse)
                .doOnTerminate(this::ensureResourceAvailable)
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
                .onErrorResumeNext(Observable.empty());
        return Observable.concat(local, cloud)
                // .throttleLast(100,TimeUnit.MILLISECONDS)
                .map(this::listAppInHomePage);

    }

    @Override
    public Observable<List<AppResource>> getListAppHomeLocal() {
        return getAppResourceLocal()
                .map(this::listAppInHomePage);
    }

    @Override
    public Observable<List<AppResource>> fetchListAppHome() {
        return fetchAppResource()
                .map(this::listAppInHomePage);
    }

    @Override
    public Observable<Void> resetStateResource(long appId) {
        return makeObservable(() -> {
            mLocalStorage.resetResourceState(appId);
            return null;
        });
    }

    private List<AppResource> listAppInHomePage(List<AppResource> resources) {
        ArrayList<AppResource> listApp = new ArrayList<>(mListDefaultApp);
        Timber.d("Get list app in home, app default size [%s]", listApp.size());
        if (resources.containsAll(listApp)) {
            resources.removeAll(listApp);
        }
        listApp.addAll(resources);
        listApp.removeAll(mListExcludeApp);
        Timber.d("app show in home page: %s", listApp.size());
        return listApp;
    }


    @Override
    public Boolean existAppResource(long appid) {
        AppResourceEntity entity = mLocalStorage.get(appid);
        return entity != null && entity.downloadState >= DownloadState.STATE_SUCCESS;
    }
}