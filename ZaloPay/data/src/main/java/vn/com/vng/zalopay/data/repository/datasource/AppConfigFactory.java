package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;
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

    public AppConfigFactory(Context context, AppConfigService service,
                            User user, SqlitePlatformScope sqlitePlatformScope, HashMap<String, String> paramsReq) {

        if (context == null || service == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.appConfigService = service;
        this.user = user;
        this.sqlitePlatformScope = sqlitePlatformScope;
        this.paramsReq = paramsReq;
    }


    public Observable<PlatformInfoResponse> getPlatformInfo() {

        String platforminfochecksum = sqlitePlatformScope.getDataManifest(Constants.MANIF_PLATFORM_INFO_CHECKSUM);
        String resourceversion = sqlitePlatformScope.getDataManifest(Constants.MANIF_RESOURCE_VERSION);

        return appConfigService.platforminfo(user.uid, user.accesstoken, platformcode, dscreentype, platforminfochecksum, resourceversion, appversion, mno, devicemodel)
                .doOnNext(response -> processPlatformResp(response))
                ;

    }

    private void processPlatformResp(PlatformInfoResponse response) {
        //  sqlitePlatformScope.put
        sqlitePlatformScope.insertDataManifest(Constants.MANIF_PLATFORM_INFO_CHECKSUM, response.platforminfochecksum);
        // sqlitePlatformScope.insertDataManifest(Constants.MANIF_RESOURCE_VERSION, response.resource);
        sqlitePlatformScope.writeCards(response.cardlist);
    }


    public Observable<List<CardEntity>> listCard() {
        return sqlitePlatformScope.listCard();
    }

    public Observable<AppResourceResponse> getAppResource() {
        List<Long> appidlist = null;
        List<String> checksumlist = null;
        return appConfigService.insideappresource(appidlist, checksumlist, paramsReq)
                .doOnNext(resourceReponse -> processAppResourceReponse(resourceReponse))
                ;
    }


    private void processAppResourceReponse(AppResourceResponse resourceReponse) {
        
    }
}
