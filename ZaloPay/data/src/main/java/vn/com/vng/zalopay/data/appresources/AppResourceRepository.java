package vn.com.vng.zalopay.data.appresources;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.cache.helper.ObservableHelper;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by huuhoa on 6/17/16.
 * Implementation for AppResource.Repository
 */
public class AppResourceRepository implements AppResource.Repository {
    private Context mContext;
    private AppConfigEntityDataMapper mAppConfigEntityDataMapper;
    private HashMap<String, String> mRequestParameters;
    private DownloadAppResourceTaskQueue taskQueue;

    private OkHttpClient mOkHttpClient;

    private final boolean mDownloadAppResource;
    private final String mRootBundle;
    private final AppResource.RequestService mRequestService;
    private final AppResource.LocalStorage mLocalStorage;

    public AppResourceRepository(Context context,
                                 AppConfigEntityDataMapper mapper,
                                 AppResource.RequestService requestService,
                                 AppResource.LocalStorage localStorage,
                                 HashMap<String, String> requestParameters,
                                 DownloadAppResourceTaskQueue taskQueue,
                                 OkHttpClient okHttpClient,
                                 boolean download,
                                 String rootBundle) {
        this.mContext = context;
        this.mAppConfigEntityDataMapper = mapper;
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
        this.mRootBundle = rootBundle;
        this.mRequestParameters = requestParameters;
        this.taskQueue = taskQueue;
        this.mOkHttpClient = okHttpClient;
        this.mDownloadAppResource = download;
    }

    @Override
    public Observable<Boolean> initialize() {
        return ObservableHelper.makeObservable(() -> {
            ensureAppResourceAvailable();
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<List<vn.com.vng.zalopay.domain.model.AppResource>> listAppResource() {
        return Observable.concat(
                    ObservableHelper.makeObservable(mLocalStorage::get),
                    fetchAppResource().flatMap(appResourceResponse -> ObservableHelper.makeObservable(mLocalStorage::get)))
                .delaySubscription(200, TimeUnit.MILLISECONDS)
                .map(o -> mAppConfigEntityDataMapper.transformAppResourceEntity(o));
    }

    private Observable<AppResourceResponse> fetchAppResource() {

        List<Integer> appidlist = new ArrayList<>();
        List<String> checksumlist = new ArrayList<>();

        listAppIdAndChecksum(appidlist, checksumlist);

        String appIds = appidlist.toString().replaceAll("\\s", "");

        Timber.d("appIds react-native list %s", appIds);

        return mRequestService.insideappresource(appIds, checksumlist.toString(), mRequestParameters)
                .doOnNext(this::processAppResourceResponse)
                ;
    }

    private void ensureAppResourceAvailable() {
        List<AppResourceEntity> list = mLocalStorage.get();
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

    private void listAppIdAndChecksum(List<Integer> appidlist, List<String> checksumlist) {
        List<AppResourceEntity> listApp = mLocalStorage.get();
        if (Lists.isEmptyOrNull(listApp)) {
            return;
        }

        for (AppResourceEntity appResourceEntity : listApp) {
            appidlist.add(appResourceEntity.appid);
            checksumlist.add(appResourceEntity.checksum);
        }
    }

    private void processAppResourceResponse(AppResourceResponse resourceResponse) {
        List<Integer> listAppId = resourceResponse.appidlist;

        List<AppResourceEntity> resourcelist = resourceResponse.resourcelist;

        startDownloadService(resourcelist, resourceResponse.baseurl);

        Timber.d("baseurl %s listAppId %s resourcelistSize %s", resourceResponse.baseurl, listAppId, resourcelist.size());

        mLocalStorage.put(resourcelist);
        mLocalStorage.updateAppList(listAppId);
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
