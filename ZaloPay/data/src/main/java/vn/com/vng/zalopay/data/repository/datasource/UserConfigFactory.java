package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import vn.com.vng.zalopay.data.cache.SqlZaloPayScopeImpl;
import vn.com.vng.zalopay.data.cache.UserConfig;

/**
 * Created by longlv on 17/05/2016.
 */
public class UserConfigFactory {

    private Context context;
    private UserConfig userConfig;
    private SqlZaloPayScopeImpl sqlZaloPayScope;

    public UserConfigFactory(Context context, UserConfig userConfig, SqlZaloPayScopeImpl sqlZaloPayScope) {
        this.context = context;
        this.userConfig = userConfig;
        this.sqlZaloPayScope = sqlZaloPayScope;
    }

    public void clearAllUserDB() {
        userConfig.clearConfig();
        sqlZaloPayScope.clearAllDatabase();
    }
}
