package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.LazyList;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.Repository
 */
public class FriendRepository implements FriendStore.Repository {
    private final int TIME_RELOAD = 5 * 60; //5'

    private FriendStore.RequestService mRequestService;
    private FriendStore.LocalStorage mLocalStorage;
    private SqlZaloPayScope mSqlZaloPayScope;

    public FriendRepository(FriendStore.RequestService requestService, FriendStore.LocalStorage localStorage, SqlZaloPayScope sqlZaloPayScope) {
        mRequestService = requestService;
        mLocalStorage = localStorage;
        mSqlZaloPayScope = sqlZaloPayScope;
    }

    @Override
    public Observable<List<ZaloFriend>> fetchListFromServer() {
        return mRequestService.fetchFriendList()
                .doOnNext(this::insertZaloFriends)
                .doOnCompleted(this::updateTimeStamp);
    }

    @Override
    public Observable<LazyList<ZaloFriendGD>> listZaloFriendFromDb(String textSearch) {
        Timber.d("LazyListZaloFriendFromDb textSearch: %s", textSearch);
        return ObservableHelper.makeObservable(() -> mLocalStorage.listZaloFriend(textSearch))
                .doOnNext(lazyListZaloFriend -> Timber.d("listZaloFriendFromDb get %s", lazyListZaloFriend.size()));
    }

    @Override
    public Observable<List<ZaloFriendGD>> listZaloFriendFromDb() {
        Timber.d("listZaloFriendFromDb");
        return ObservableHelper.makeObservable(() -> mLocalStorage.listZaloFriend())
                .doOnNext(listZaloFriend -> Timber.d("listZaloFriendFromDb get %s", listZaloFriend.size()));
    }

    @Override
    public Observable<List<ZaloFriend>> retrieveZaloFriendsAsNeeded() {
        return Observable.create(new Observable.OnSubscribe<List<ZaloFriend>>() {
            @Override
            public void call(final Subscriber<? super List<ZaloFriend>> subscriber) {
                shouldUpdate().subscribe(l -> fetchListFromServer().subscribe(subscriber));
            }
        });
    }

    @Override
    public ZaloFriend convertZaloFriendGD(ZaloFriendGD zaloFriendGD) {
        if (zaloFriendGD == null) {
            return null;
        }
        return new ZaloFriend(zaloFriendGD.getId(), zaloFriendGD.getUserName(), zaloFriendGD.getDisplayName(), zaloFriendGD.getAvatar(), zaloFriendGD.getUserGender(), zaloFriendGD.getUsingApp());
    }

    private ZaloFriendGD convertZaloFriend(ZaloFriend zaloFriend) {
        if (zaloFriend == null) {
            return null;
        }
        String fullTextSearch = Strings.stripAccents(zaloFriend.getDisplayName());
        return new ZaloFriendGD(zaloFriend.getUserId(), zaloFriend.getUserName(),
                zaloFriend.getDisplayName(), zaloFriend.getAvatar(),
                zaloFriend.getUserGender(), "",
                zaloFriend.isUsingApp(),
                fullTextSearch);
    }

    private List<ZaloFriendGD> convertZaloFriends(List<ZaloFriend> zaloFriends) {
        List<ZaloFriendGD> result = new ArrayList<>();
        if (zaloFriends == null || zaloFriends.size() <= 0) {
            return result;
        }
        for (ZaloFriend zaloFriend : zaloFriends) {
            if (zaloFriend == null) {
                continue;
            }
            ZaloFriendGD zaloFriendTmp = convertZaloFriend(zaloFriend);
            result.add(zaloFriendTmp);
        }
        return result;
    }

    private void insertZaloFriends(List<ZaloFriend> zaloFriends) {
        List<ZaloFriendGD> zaloFriendList = convertZaloFriends(zaloFriends);
        mLocalStorage.put(zaloFriendList);
    }

    private Observable<List<ZaloFriend>> shouldUpdate() {
        List<ZaloFriend> empty = new ArrayList<>();
        return Observable.just(empty).filter(integer -> {
            if (mSqlZaloPayScope != null && mLocalStorage.isHaveZaloFriendDb()) {
                long lastUpdated = mSqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND, 0);
                //check xem moi lay thi thoi
                long currentTime = System.currentTimeMillis() / 1000;
                boolean flag = ((currentTime - lastUpdated) >= TIME_RELOAD);

                Timber.i("Should update: %s [current: %d, last: %d, offset: %d", flag, currentTime, lastUpdated, currentTime - lastUpdated);
                return flag;
            }

            Timber.i("Should update: TRUE");
            return true;
        });
    }

    private void updateTimeStamp() {
        Timber.d("Request to update DB timestamp for ZaloFriendList");
        mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND, String.valueOf(System.currentTimeMillis() / 1000));
    }

    @Override
    public Observable<Cursor> zaloFriendList() {
        return ObservableHelper.makeObservable(() -> mLocalStorage.zaloFriendList());
    }

    @Override
    public Observable<Cursor> searchZaloFiend(String s) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.searchZaloFriendList(s));
    }
}
