package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import com.google.gson.Gson;
import com.zalopay.apploader.network.NetworkService;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.api.DynamicUrlService;
import vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory;
import vn.com.vng.zalopay.network.ToStringConverterFactory;
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
import vn.com.vng.zalopay.react.iap.NetworkServiceImpl;
import vn.com.vng.zalopay.utils.ConfigUtil;

import static vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory.AdapterType.React;
import static vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory.AdapterType.RedPacket;

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
    PaymentConnectorService providesConnectorService(Context context, Connection connection) {
        return new PaymentConnectorService(context, connection);
    }

    @Provides
    @UserScope
    PaymentConnectorCallFactory providesConnectorCallFactory(PaymentConnectorService connectorService) {
        return new PaymentConnectorCallFactory(connectorService);
    }

    @Provides
    @UserScope
    @Named("retrofitConnector")
    Retrofit providesRetrofitConnector(HttpUrl baseUrl, OkHttpClient okHttpClient,
                                       CallAdapter.Factory callAdapter,
                                       Converter.Factory convertFactory,
                                       PaymentConnectorCallFactory callFactory) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .addCallAdapterFactory(callAdapter)
                .addConverterFactory(convertFactory)
                .baseUrl(baseUrl)
                .validateEagerly(BuildConfig.DEBUG);

        if (ConfigUtil.isHttpsRoute()) {
            builder.client(okHttpClient);
        } else {
            builder.callFactory(callFactory);
        }
        return builder.build();
    }

    @Provides
    @UserScope
    @Named("retrofitRedPacketApi")
    Retrofit provideRetrofitRedPacketApi(OkHttpClient okHttpClient, Context context,
                                         Converter.Factory convertFactory, PaymentConnectorCallFactory callFactory) {

        Retrofit.Builder builder = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create(context, RedPacket))
                .addConverterFactory(convertFactory)
                .baseUrl(BuildConfig.REDPACKET_HOST)
                .validateEagerly(BuildConfig.DEBUG);

        if (ConfigUtil.isHttpsRoute()) {
            builder.client(okHttpClient);
        } else {
            builder.callFactory(callFactory);
        }

        return builder.build();
    }

    @Provides
    @UserScope
    @Named("NetworkServiceWithRetry")
    NetworkService providesNetworkService(@Named("retrofitReact") Retrofit retrofit) {
        return new NetworkServiceImpl(retrofit.create(DynamicUrlService.class));
    }

    @Provides
    @UserScope
    @Named("retrofitReact")
    Retrofit providePaymentAppWithRetry(Context context, HttpUrl baseUrl, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(new ToStringConverterFactory())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create(context, React))
                .baseUrl(baseUrl)
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build();
    }
}
