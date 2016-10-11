package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
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
                .doOnNext(mLocalStorage::put)
                .doOnCompleted(this::updateTimeStamp)
                .map(entities -> Boolean.TRUE);
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
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> fetchZaloFriends())
                ;
    }

    @Override
    public ZaloFriend transform(Cursor cursor) {

        //todo: remove hardcode
        ZaloFriend zaloFriend = new ZaloFriend();
        zaloFriend.setUserId(cursor.getLong(0));
        zaloFriend.setUserName(cursor.getString(1));
        zaloFriend.setDisplayName(cursor.getString(2));
        zaloFriend.setAvatar(cursor.getString(3));
        //zaloFriend.setUserGender(cursor.getInt(4));
        //5 birthday
        zaloFriend.setUsingApp(cursor.getInt(6) == 1);
        zaloFriend.setNormalizeDisplayName(cursor.getString(7));
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
        return ObservableHelper.makeObservable(() -> mLocalStorage.get())
                .map(this::transform);
    }

    private ZaloFriend transform(ZaloFriendEntity entity) {
        ZaloFriend friend = null;
        if (entity != null) {
            friend = new ZaloFriend();
            friend.setUserId(entity.userId);
            friend.setAvatar(entity.avatar);
            friend.setDisplayName(entity.displayName);
            friend.setUserName(entity.userName);
            friend.setUsingApp(entity.usingApp);
            friend.setNormalizeDisplayName(entity.normalizeDisplayName);
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
