package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ParamRequestProvider;
import vn.com.vng.zalopay.data.api.entity.AppInfoEntity;
import vn.com.vng.zalopay.data.cache.SqlAppListScope;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 5/4/16.
 */


public class AppListFactory {

    private Context context;

    private AppConfigService appConfigService;

    private HashMap<String, String> params;

    private User user;

    private SqlAppListScope sqlAppListScope;

    public AppListFactory(Context context, AppConfigService service, ParamRequestProvider paramRequestProvider,
                          User user, SqlAppListScope sqlAppListScope) {

        if (context == null || service == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.context = context;
        this.appConfigService = service;
        this.params = paramRequestProvider.paramsDefault;
        this.user = user;
        this.sqlAppListScope = sqlAppListScope;

    }

    public Observable<List<AppInfoEntity>> listAppInfoEntity() {
        return null;
    }
}
