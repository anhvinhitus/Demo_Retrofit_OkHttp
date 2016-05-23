package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;
import vn.com.vng.zalopay.data.download.DownloadAppResourceTask;
import vn.com.vng.zalopay.data.download.DownloadAppResourceTaskQueue;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 4/28/16.
 */

public class AppConfigFactory {

    private Context context;

    private AppConfigService appConfigService;

    private User user;

    private SqlitePlatformScope sqlitePlatformScope;

    private String platformcode = "android";
    private String dscreentype = "xhigh";
    private String appversion = "appversion";
    private String mno = "mno";
    private String devicemodel = "devicemodel";

    private HashMap<String, String> paramsReq;
    private DownloadAppResourceTaskQueue taskQueue;

    private OkHttpClient mOkHttpClient;

    public AppConfigFactory(Context context, AppConfigService service,
                            User user, SqlitePlatformScope sqlitePlatformScope,
                            HashMap<String, String> paramsReq,
                            DownloadAppResourceTaskQueue taskQueue, OkHttpClient mOkHttpClient) {

        if (context == null || service == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.appConfigService = service;
        this.user = user;
        this.sqlitePlatformScope = sqlitePlatformScope;
        this.paramsReq = paramsReq;
        this.taskQueue = taskQueue;
        this.mOkHttpClient = mOkHttpClient;
    }


    public Observable<PlatformInfoResponse> getPlatformInfo() {

        String platforminfochecksum = sqlitePlatformScope.getDataManifest(Constants.MANIF_PLATFORM_INFO_CHECKSUM);
        String rsversion = sqlitePlatformScope.getDataManifest(Constants.MANIF_RESOURCE_VERSION);

        return appConfigService.platforminfo(user.uid, user.accesstoken, platformcode, dscreentype, platforminfochecksum, rsversion, appversion, mno, devicemodel)
                .doOnNext(response -> processPlatformResp(response))
                ;

    }

    private void processPlatformResp(PlatformInfoResponse response) {
        //  sqlitePlatformScope.put
        sqlitePlatformScope.insertDataManifest(Constants.MANIF_PLATFORM_INFO_CHECKSUM, response.platforminfochecksum);
        sqlitePlatformScope.insertDataManifest(Constants.MANIF_RESOURCE_VERSION, response.resource.rsversion);

        sqlitePlatformScope.writeCards(response.platforminfo.cardlist);
    }


    public Observable<List<CardEntity>> listCardCache() {
        return sqlitePlatformScope.listCard();
    }

    public Observable<AppResourceResponse> getAppResourceCloud() {
        List<Long> appidlist = new ArrayList<>();
        List<String> checksumlist = new ArrayList<>();
        listAppIdAndChecksum(appidlist, checksumlist);

        Timber.d("appid react-native list ", appidlist);

        return appConfigService.insideappresource(appidlist, checksumlist, paramsReq)
                .doOnNext(resourceResponse -> processAppResourceResponse(resourceResponse))
                ;
    }


    public Observable<List<AppResourceEntity>> listAppResourceCache() {
        return sqlitePlatformScope.listApp();
    }

    private void listAppIdAndChecksum(List<Long> appidlist, List<String> checksumlist) {
        List<AppResourceEntity> listApp = sqlitePlatformScope.listAppResourceEntity();
        if (!Lists.isEmptyOrNull(listApp)) {
            for (AppResourceEntity appResourceEntity : listApp) {
                appidlist.add(appResourceEntity.appid);
                checksumlist.add(appResourceEntity.checksum);
            }
        }
    }

    private void processAppResourceResponse(AppResourceResponse resourceReponse) {
        List<Long> listAppId = resourceReponse.appidlist;

        List<AppResourceEntity> resourcelist = resourceReponse.resourcelist;

        long expiredtime = resourceReponse.expiredtime;
        String baseurl = resourceReponse.baseurl;


        List<DownloadAppResourceTask> needDownloadList = new ArrayList<>();
        for (AppResourceEntity appResourceEntity : resourcelist) {
            appResourceEntity.jsurl = baseurl + appResourceEntity.jsurl;
            appResourceEntity.imageurl = baseurl + appResourceEntity.imageurl;

            if (appResourceEntity.needdownloadrs == 1) {
                DownloadAppResourceTask task = new DownloadAppResourceTask(context, appResourceEntity, mOkHttpClient);
                needDownloadList.add(task);
            }
        }

        if (!needDownloadList.isEmpty()) {
            // taskQueue.enqueue(needDownloadList);
        }


        Timber.d("baseurl %s listAppId %s resourcelistSize %s", baseurl, listAppId, resourcelist.size());

        sqlitePlatformScope.write(resourcelist);
        sqlitePlatformScope.updateAppId(listAppId);
    }
}
