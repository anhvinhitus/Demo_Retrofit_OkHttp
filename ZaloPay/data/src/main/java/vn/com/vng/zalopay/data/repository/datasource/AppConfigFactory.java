package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.HashMap;

import rx.Observable;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ParamRequestProvider;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;
import vn.com.vng.zalopay.data.cache.SqlitePlatformScope;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 4/28/16.
 */

public class AppConfigFactory {

    private Context context;

    private AppConfigService appConfigService;

    private HashMap<String, String> params;

    private HashMap<String, String> authZaloParams;

    private User user;

    private SqlitePlatformScope sqlitePlatformScope;

    public AppConfigFactory(Context context, AppConfigService service, ParamRequestProvider paramRequestProvider,
                            User user, SqlitePlatformScope sqlitePlatformScope) {

        if (context == null || service == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.appConfigService = service;
        this.params = paramRequestProvider.paramsDefault;
        this.authZaloParams = paramRequestProvider.paramsZalo;
        this.user = user;
        this.sqlitePlatformScope = sqlitePlatformScope;

    }


    public Observable<PlatformInfoResponse> getPlatformInfo() {
        //Todo: đang test
        String platformcode = null;
        String dscreentype = null;
        String platforminfochecksum = null;
        String resourceversion = null;
        String mno = null;

        return appConfigService.platforminfo(platformcode, dscreentype,
                platformcode, resourceversion, mno,
                user.uid, user.accesstoken, params);

    }

}
