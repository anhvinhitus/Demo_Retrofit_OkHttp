package vn.com.vng.zalopay.internal.di.modules.user;

import android.content.Context;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.zfriend.FriendLocalStorage;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.transfer.FriendRepository;
import vn.com.vng.zalopay.transfer.FriendRequestService;
import vn.com.vng.zalopay.transfer.FriendStoreRepository;

/**
 * Created by huuhoa on 7/4/16.
 * Provide glues for FriendStore services
 */
@Module
public class FriendModule {
    @UserScope
    @Provides
    FriendStore.LocalStorage provideFriendLocalStorage(@Named("daosession") DaoSession session) {
        return new FriendLocalStorage(session);
    }

    @UserScope
    @Provides
    FriendStoreRepository provideFriendRepository(FriendStore.LocalStorage localStorage,
                                                  SqlZaloPayScope sqlZaloPayScope,
                                                  Context context) {
        return new FriendRepository(new FriendRequestService(), localStorage, sqlZaloPayScope, context);
    }
}
