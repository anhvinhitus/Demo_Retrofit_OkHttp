package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
<<<<<<< HEAD
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
=======
>>>>>>> 39f2c87... [Payment Connector] Enforce code convention and cleanup trace log
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory;
import vn.com.vng.zalopay.data.transfer.TransferLocalStorage;
import vn.com.vng.zalopay.data.transfer.TransferRepository;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.ws.connection.Connection;
import vn.com.vng.zalopay.data.ws.connection.NotificationService;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.parser.MessageParser;
import vn.com.vng.zalopay.data.ws.payment.request.PaymentConnectorCallFactory;
import vn.com.vng.zalopay.data.ws.payment.request.PaymentConnectorService;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.notification.NotificationHelper;
import vn.com.vng.zalopay.notification.ZPNotificationService;
import vn.com.vng.zalopay.service.UserSession;

import com.google.gson.Gson;
import com.zalopay.apploader.ReactNativeHostable;
import com.zalopay.apploader.ReactNativeHostLongLife;

import org.greenrobot.eventbus.EventBus;

@Module
public class UserModule {

    private final User user;

    public UserModule(User user) {
        Timber.d("Create new instance of UserModule");
        this.user = user;
    }

    @Provides
    @UserScope
    User provideUser() {
        return user;
    }

    @Provides
    @UserScope
    Connection providesWsConnection(Context context, Gson gson) {
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
    PaymentConnectorService providesPaymentRequestService(User user, Connection connection) {
        return new PaymentConnectorService(connection);
    }

    @Provides
    @UserScope
    UserSession providesUserSession(Context context, UserConfig userConfig, EventBus eventBus,
                                    NotificationService service,
                                    BalanceStore.Repository repository) {
        return new UserSession(context, user, userConfig, eventBus, service, repository);
    }

    @Provides
    @UserScope
    TransferStore.LocalStorage provideTransferLocalStorage(@Named("daosession") DaoSession session) {
        return new TransferLocalStorage(session);
    }

    @Provides
    @UserScope
    TransferStore.Repository provideTransferRepository(TransferStore.LocalStorage localStorage) {
        return new TransferRepository(localStorage);
    }


    @Provides
    @UserScope
    ReactNativeHostable provideReactNativeInstanceManager() {
        Timber.d("Create new instance of ReactNativeInstanceManagerLongLife");
        return new ReactNativeHostLongLife();
    }

}