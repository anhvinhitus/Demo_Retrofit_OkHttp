package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.data.zfriend.FriendLocalStorage;
import vn.com.vng.zalopay.data.zfriend.FriendRepository;
import vn.com.vng.zalopay.data.zfriend.FriendRequestService;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.data.zfriend.contactloader.ContactFetcher;
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
    FriendStore.LocalStorage provideFriendLocalStorage(@Named("daosession") DaoSession session) {
        return new FriendLocalStorage(session);
    }

    @UserScope
    @Provides
    FriendStore.RequestService provideFriendRequestService(@Named("retrofitApi") Retrofit retrofit) {
        return retrofit.create(FriendStore.RequestService.class);
    }

    @UserScope
    @Provides
    FriendStore.ZaloRequestService provideFriendStoreZaloRequestService(ZaloSdkApi sdkApi) {
        return new FriendRequestService(sdkApi);
    }

    @UserScope
    @Provides
    FriendStore.Repository provideFriendRepository(User user, FriendStore.ZaloRequestService zaloRequestService,
                                                   FriendStore.LocalStorage localStorage,
                                                   FriendStore.RequestService requestService,
                                                   ContactFetcher contactFetcher
    ) {
        return new FriendRepository(user, zaloRequestService, requestService, localStorage, contactFetcher);
    }


}
