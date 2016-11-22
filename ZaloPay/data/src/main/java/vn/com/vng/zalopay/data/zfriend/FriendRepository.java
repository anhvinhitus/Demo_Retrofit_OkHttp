package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.ZaloFriendEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.Repository
 */
public class FriendRepository implements FriendStore.Repository {
    private final int TIME_RELOAD = 5 * 60; //5'

    private final int TIMEOUT_REQUEST_FRIEND = 10;

    private FriendStore.RequestService mRequestService;
    private FriendStore.LocalStorage mLocalStorage;

    public FriendRepository(FriendStore.RequestService requestService, FriendStore.LocalStorage localStorage) {
        mRequestService = requestService;
        mLocalStorage = localStorage;
    }

    @Override
    public Observable<Boolean> fetchZaloFriends() {
        Timber.d("fetchZaloFriends");
        return mRequestService.fetchFriendList()
                .doOnNext(new Action1<List<ZaloFriendEntity>>() {
                    @Override
                    public void call(List<ZaloFriendEntity> entities) {
                        mLocalStorage.put(entities);
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        updateTimeStamp();
                    }
                })
                .map(new Func1<List<ZaloFriendEntity>, Boolean>() {
                    @Override
                    public Boolean call(List<ZaloFriendEntity> entities) {
                        return Boolean.TRUE;
                    }
                });
    }

    @Override
    public Observable<Cursor> fetchZaloFriendList() {
        return fetchZaloFriends()
                .map(new Func1<Boolean, Cursor>() { //convert to cursor
                    @Override
                    public Cursor call(Boolean aBoolean) {
                        return null;
                    }
                })
                .timeout(TIMEOUT_REQUEST_FRIEND, TimeUnit.SECONDS)
                .concatWith(this.zaloFriendList());
    }

    @Override
    public Observable<Boolean> retrieveZaloFriendsAsNeeded() {
        Timber.d("retrieveZaloFriendsAsNeeded");
        return shouldUpdateFriendList()
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return Boolean.TRUE;
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean aBoolean) {
                        return fetchZaloFriends();
                    }
                })
                ;
    }

    @Override
    public ZaloFriend transform(Cursor cursor) {
        ZaloFriend zaloFriend = new ZaloFriend();
        zaloFriend.userId = cursor.getLong(ColumnIndex.Id);
        zaloFriend.userName = cursor.getString(ColumnIndex.UserName);
        zaloFriend.displayName = cursor.getString(ColumnIndex.DisplayName);
        zaloFriend.avatar = cursor.getString(ColumnIndex.Avatar);
        zaloFriend.usingApp = cursor.getInt(ColumnIndex.UsingApp) == 1;
        zaloFriend.normalizeDisplayName = cursor.getString(ColumnIndex.Fulltextsearch);
        return zaloFriend;
    }

    public Observable<Boolean> shouldUpdateFriendList() {
        return ObservableHelper.makeObservable(() -> {
            if (mLocalStorage.isHaveZaloFriendDb()) {
                long lastUpdated = mLocalStorage.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND, 0);
                long currentTime = System.currentTimeMillis() / 1000;
                boolean flag = ((currentTime - lastUpdated) >= TIME_RELOAD);
                Timber.i("Should update: %s [current: %d, last: %d, offset: %d]", flag, currentTime, lastUpdated, currentTime - lastUpdated);
                return flag;

            }
            return Boolean.TRUE;
        });
    }


    private void updateTimeStamp() {
        Timber.d("Request to update DB timestamp for ZaloFriendList");
        mLocalStorage.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND,
                String.valueOf(System.currentTimeMillis() / 1000));
    }

    @Override
    public Observable<Cursor> zaloFriendList() {
        return ObservableHelper.makeObservable(() -> mLocalStorage.zaloFriendList());
    }

    @Override
    public Observable<Cursor> searchZaloFriend(String s) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.searchZaloFriendList(s));
    }

    @Override
    public Observable<List<ZaloFriend>> getZaloFriendList() {
        return ObservableHelper.makeObservable(new Callable<List<ZaloFriendEntity>>() {
            @Override
            public List<ZaloFriendEntity> call() throws Exception {
                return mLocalStorage.get();
            }
        })
                .map(new Func1<List<ZaloFriendEntity>, List<ZaloFriend>>() {
                    @Override
                    public List<ZaloFriend> call(List<ZaloFriendEntity> entities) {
                        return transform(entities);
                    }
                });
    }

    private ZaloFriend transform(ZaloFriendEntity entity) {
        ZaloFriend friend = null;
        if (entity != null) {
            friend = new ZaloFriend();
            friend.userId = entity.userId;
            friend.avatar = entity.avatar;
            friend.displayName = entity.displayName;
            friend.userName = entity.userName;
            friend.usingApp = entity.usingApp;
            friend.normalizeDisplayName = entity.normalizeDisplayName;
        }
        return friend;
    }

    private List<ZaloFriend> transform(List<ZaloFriendEntity> zaloFriends) {
        if (Lists.isEmptyOrNull(zaloFriends)) {
            return Collections.emptyList();
        }

        List<ZaloFriend> result = new ArrayList<>();
        for (ZaloFriendEntity zaloFriend : zaloFriends) {
            ZaloFriend zaloFriendTmp = transform(zaloFriend);
            if (zaloFriendTmp != null) {
                result.add(zaloFriendTmp);
            }
        }
        return result;
    }

}
