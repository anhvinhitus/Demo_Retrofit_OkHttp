package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.zfriend.FriendLocalStorage;
import vn.com.vng.zalopay.data.zfriend.FriendRepository;
import vn.com.vng.zalopay.data.zfriend.FriendRequestService;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.transfer.provider.ZaloFriendStoreApi;

/**
 * Created by huuhoa on 7/4/16.
 * Provide glues for FriendStore services
 */
@Module
public class UserFriendModule {
    @UserScope
    @Provides
    FriendStore.SDKApi provideFriendStoreApi(Context context) {
        return new ZaloFriendStoreApi(context);
    }

    @UserScope
    @Provides
    FriendStore.LocalStorage provideFriendLocalStorage(@Named("daosession") DaoSession session) {
        return new FriendLocalStorage(session);
    }

    @UserScope
    @Provides
    FriendStore.RequestService provideFriendStoreRequestService(FriendStore.SDKApi sdkApi) {
        return new FriendRequestService(sdkApi);
    }

    @UserScope
    @Provides
    FriendStore.Repository provideFriendRepository(FriendStore.LocalStorage localStorage,
                                                   FriendStore.RequestService requestService
    ) {
        return new FriendRepository(requestService, localStorage);
    }
}
