package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.parser.MessageParser;

/**
 * Created by AnhHieu on 6/16/16.
 */
@Module
public class WsModule {
    
    @Provides
    @Singleton
    WsConnection providesWsConnecttion(Context context, UserConfig userConfig) {
        WsConnection wsConnection = new WsConnection(context, new MessageParser(), userConfig);
        wsConnection.setHostPort(BuildConfig.WS_HOST, BuildConfig.WS_PORT);
        return wsConnection;
    }

}
