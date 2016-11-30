package vn.com.vng.zalopay.data.appresources;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

    private AppConfigEntityDataMapper mAppConfigEntityDataMapper;
    private HashMap<String, String> mRequestParameters;
    private DownloadAppResourceTaskQueue taskQueue;

    private OkHttpClient mOkHttpClient;

    private final boolean mDownloadAppResource;
    private final String mRootBundle;
    private final AppResourceStore.RequestService mRequestService;
    private final AppResourceStore.LocalStorage mLocalStorage;
    private String appVersion;
    //Retry 3 times, each time to download 2 file (js & image)
    private final int RETRY_DOWNLOAD_NUMBER = 6;
    private final int REDPACKET_APP_ID;

    public AppResourceRepository(AppConfigEntityDataMapper mapper,
                                 AppResourceStore.RequestService requestService,
                                 AppResourceStore.LocalStorage localStorage,
                                 HashMap<String, String> requestParameters,
                                 DownloadAppResourceTaskQueue taskQueue,
                                 OkHttpClient okHttpClient,
                                 boolean download,
                                 String rootBundle,
                                 String appVersion,
                                 int redpacketId) {
        this.mAppConfigEntityDataMapper = mapper;
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
        this.mRootBundle = rootBundle;
        this.mRequestParameters = requestParameters;
        this.taskQueue = taskQueue;
        this.mOkHttpClient = okHttpClient;
        this.mDownloadAppResource = download;
        this.appVersion = appVersion;
        this.REDPACKET_APP_ID = redpacketId;
    }

    @Override
    public Observable<Boolean> initialize() {
        return makeObservable(() -> {
            ensureAppResourceAvailable();
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<List<AppResource>> listInsideAppResource() {
        return Observable.concat(
                makeObservable(mLocalStorage::getAllAppResource),
                fetchInsideAppResource()
                        .map(response -> mLocalStorage.getAllAppResource())
                        .onErrorResumeNext(throwable -> Observable.empty())
        ).map(mAppConfigEntityDataMapper::transformAppResourceEntity);
    }

    private Observable<AppResourceResponse> fetchInsideAppResource() {

        List<String> appidlist = new ArrayList<>();
        List<String> checksumlist = new ArrayList<>();

        listAppIdAndChecksum(appidlist, checksumlist);

        String appIds = appidlist.toString().replaceAll("\\s", "");
        String checkSum = checksumlist.toString();

        Timber.d("appIds react-native %s checkSum %s", appIds, checkSum);

        return mRequestService.insideappresource(appIds, checkSum, mRequestParameters, appVersion)
                .doOnNext(this::processAppResourceResponse)
                ;
    }

    private void ensureAppResourceAvailable() {
        List<AppResourceEntity> list = mLocalStorage.getAllAppResource();
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
        Timber.d("shouldDownloadApp stateDownload [%s]", app.stateDownload);
        if (app.appid == REDPACKET_APP_ID) {
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
        List<AppResourceEntity> appResources = mLocalStorage.getAllAppResource();

        if (Lists.isEmptyOrNull(appResources)) {
            return;
        }

        for (AppResourceEntity appResource : appResources) {
            if (appResource != null) {
                listAppId.add(String.valueOf(appResource.appid));
                checksumlist.add(appResource.checksum);
            }
        }
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
                resourcelist.add(appResourceEntity);
            }

            Timber.d("baseUrl [%s] resourceListSize [%s]", resourceResponse.baseurl, resourcelist.size());
            startDownloadService(resourcelist, resourceResponse.baseurl);
            mLocalStorage.put(resourcelist);
        } else if (!Lists.isEmptyOrNull(resourceResponse.orderedInsideApps)) {
            updateInsideAppIndex(resourceResponse.orderedInsideApps);
        }

        List<Integer> listAppId = resourceResponse.appidlist;
        mLocalStorage.updateAppList(listAppId);
    }

    private void updateInsideAppIndex(List<Integer> orderedInsideApps) {
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

    @Override
    public Observable<Boolean> existResource(int appId) {
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
}
