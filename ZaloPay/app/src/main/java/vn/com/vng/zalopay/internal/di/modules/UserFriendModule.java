package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.data.zpc.ZPCRepository;
import vn.com.vng.zalopay.data.zpc.FriendRequestService;
import vn.com.vng.zalopay.data.zpc.ZPCStore;
import vn.com.vng.zalopay.data.zpc.ZPCLocalStorage;
import vn.com.vng.zalopay.data.zpc.contactloader.ContactFetcher;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by huuhoa on 7/4/16.
 * Provide glues for FriendStore services
 */
@Module
public class UserFriendModule {

    @UserScope
    @Provides
    ZPCStore.LocalStorage provideFriendLocalStorage(@Named("daosession") DaoSession session) {
        return new ZPCLocalStorage(session);
    }

    @UserScope
    @Provides
    ZPCStore.RequestService provideFriendRequestService(@Named("retrofitConnector") Retrofit retrofit) {
        return retrofit.create(ZPCStore.RequestService.class);
    }

    @UserScope
    @Provides
    ZPCStore.ZaloRequestService provideFriendStoreZaloRequestService(ZaloSdkApi sdkApi) {
        return new FriendRequestService(sdkApi);
    }

    @UserScope
    @Provides
    ZPCStore.Repository provideFriendRepository(User user, ZPCStore.ZaloRequestService zaloRequestService,
                                                ZPCStore.LocalStorage localStorage,
                                                ZPCStore.RequestService requestService,
                                                ContactFetcher contactFetcher
    ) {
        return new ZPCRepository(user, zaloRequestService, requestService, localStorage, contactFetcher);
    }


}
