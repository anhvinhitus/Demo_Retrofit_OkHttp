package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.Call;
import okhttp3.HttpUrl;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory;
import vn.com.vng.zalopay.data.paymentconnector.PaymentConnectorCallFactory;
import vn.com.vng.zalopay.data.paymentconnector.PaymentConnectorService;
import vn.com.vng.zalopay.data.ws.connection.Connection;
import vn.com.vng.zalopay.data.ws.connection.NotificationService;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.parser.MessageParser;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.notification.NotificationHelper;
import vn.com.vng.zalopay.notification.ZPNotificationService;

/**
 * Created by hieuvm on 3/10/17.
 * Provide glues between network services
 */

@Module
public class UserSocketModule {

    @Provides
    @UserScope
    Connection providesWsConnection(Context context, Gson gson, User user) {
        return new WsConnection(BuildConfig.WS_HOST, BuildConfig.WS_PORT, context,
                new MessageParser(gson), user);
    }

    @Provides
    @UserScope
    NotificationService providesNotificationServices(Context context, User user,
                                                     NotificationHelper helper, ThreadExecutor threadExecutor,
                                                     Gson gson, EventBus eventBus, Connection connection) {
        return new ZPNotificationService(context, user, helper, threadExecutor, gson, eventBus, connection);
    }

    @Provides
    @UserScope
    PaymentConnectorCallFactory providesConnectorCallFactory(Context context, Connection connection) {
        return new PaymentConnectorCallFactory(new PaymentConnectorService(context, connection));
    }

    @Provides
    @UserScope
    @Named("retrofitConnector")
    Retrofit providesRetrofitConnector(HttpUrl baseUrl,
                                       CallAdapter.Factory callAdapter,
                                       Converter.Factory convertFactory,
                                       PaymentConnectorCallFactory callFactory) {

        return new Retrofit.Builder()
                .addConverterFactory(convertFactory)
                .addCallAdapterFactory(callAdapter)
                .callFactory(callFactory)
                .baseUrl(baseUrl)
                .validateEagerly(BuildConfig.DEBUG)
                .build();
    }

    @Provides
    @UserScope
    @Named("retrofitConnectorSdk")
    Retrofit providesRetrofitConnectorForSdk(HttpUrl baseUrl,
                                             Converter.Factory convertFactory,
                                             PaymentConnectorCallFactory callFactory) {
        return new Retrofit.Builder()
                .addConverterFactory(convertFactory)
                .callFactory(callFactory)
                .baseUrl(baseUrl)
                .validateEagerly(BuildConfig.DEBUG)
                .build();
    }
}
