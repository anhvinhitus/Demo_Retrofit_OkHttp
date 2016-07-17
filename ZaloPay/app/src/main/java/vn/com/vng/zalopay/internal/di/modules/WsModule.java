package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.parser.MessageParser;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 6/16/16.
 */
@Module
public class WsModule {

    @UserScope
    @Provides
    WsConnection providesWsConnection(Context context, UserConfig userConfig, Gson gson) {
        WsConnection wsConnection = new WsConnection(context, new MessageParser(userConfig, gson), userConfig);
        wsConnection.setHostPort(BuildConfig.WS_HOST, BuildConfig.WS_PORT);
        return wsConnection;
    }

}
