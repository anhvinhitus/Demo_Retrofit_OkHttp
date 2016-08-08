package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import java.util.HashMap;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.appresources.AppResource;
import vn.com.vng.zalopay.data.appresources.AppResourceLocalStorage;
import vn.com.vng.zalopay.data.appresources.AppResourceRepository;
import vn.com.vng.zalopay.data.cache.mapper.PlatformDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.appresources.DownloadAppResourceTaskQueue;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by huuhoa on 6/17/16.
 * Provide glues for AppResource local storage, request service, and repository
 */
@Module
public class AppResourceModule {

    @Provides
    @UserScope
    AppResource.RequestService provideAppResourceRequestService(@Named("retrofitApi") Retrofit retrofit) {
        return retrofit.create(AppResource.RequestService.class);
    }

    @Provides
    @UserScope
    AppResource.LocalStorage provideAppResourceLocalStorage(@Named("daosession") DaoSession session,
                                                            PlatformDaoMapper mapper) {
        return new AppResourceLocalStorage(session, mapper);
    }

    @Provides
    @UserScope
    AppResource.Repository provideAppResourceRepository(Context context,
                                                        AppConfigEntityDataMapper mapper,
                                                        AppResource.RequestService requestService,
                                                        AppResource.LocalStorage localStorage,
                                                        @Named("params_request_default") HashMap<String, String> paramsReq,
                                                        DownloadAppResourceTaskQueue taskQueue,
                                                        OkHttpClient mOkHttpClient,
                                                        @Named("rootbundle") String rootBundle) {
        return new AppResourceRepository(mapper, requestService, localStorage,
                paramsReq, taskQueue, mOkHttpClient,
                BuildConfig.DOWNLOAD_APP_RESOURCE, rootBundle, BuildConfig.VERSION_NAME);
    }
}
