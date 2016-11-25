package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.UserExistEntity;
import vn.com.vng.zalopay.data.api.entity.UserRedPackageEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloFriendEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.Repository
 */
public class FriendRepository implements FriendStore.Repository {
    private final int TIME_RELOAD = 5 * 60; //5'

    private final int TIMEOUT_REQUEST_FRIEND = 10;

    private FriendStore.RequestService mRequestService;
    private FriendStore.ZaloRequestService mZaloRequestService;
    private FriendStore.LocalStorage mLocalStorage;
    private User mUser;

    public FriendRepository(User user, FriendStore.ZaloRequestService zaloRequestService,
                            FriendStore.RequestService requestService,
                            FriendStore.LocalStorage localStorage) {
        mRequestService = requestService;
        mLocalStorage = localStorage;
        mZaloRequestService = zaloRequestService;
        mUser = user;
    }

    @Override
    public Observable<Boolean> fetchZaloFriends() {
        Timber.d("fetchZaloFriends");
        return mZaloRequestService.fetchFriendList()
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
        Timber.d("Retrieve Zalo Friends AsNeeded");
        return shouldUpdateFriendList()
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean;
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

    @Nullable
    @Override
    public ZaloFriend transform(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
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

    public Observable<List<UserExistEntity>> checkListZaloIdForClient() {
        return ObservableHelper.makeObservable(() -> mLocalStorage.getZaloFriendWithoutZpId())
                .map(this::transformZpId)
                .filter(s -> !TextUtils.isEmpty(s))
                .flatMap(s -> Observable.create(subscriber -> checklistzaloidforclient(s, subscriber, 0)));
    }

    private String mPreviousZaloId = null;

    private void checklistzaloidforclient(String zaloidlist, Subscriber<? super List<UserExistEntity>> subscriber, int deep) {
        Timber.d("check list zaloid for client %s deep %s", zaloidlist, deep);
        Subscription subscription = fetchZaloPayId(zaloidlist)
                .doOnNext(entities -> {

                    if (subscriber.isUnsubscribed()) {
                        return;
                    }

                    List<ZaloFriendEntity> mList = mLocalStorage.getZaloFriendWithoutZpId();
                    Timber.d("list zalo need merge %s ", Lists.isEmptyOrNull(mList) ? 0 : mList.size());
                    String mNextZaloIdList = transformZpId(mList);
                    if (TextUtils.isEmpty(mNextZaloIdList)) {
                        Timber.d("Check list zaloid or client on Complete");
                        subscriber.onCompleted();
                    } else {

                        if (mNextZaloIdList.equals(mPreviousZaloId)) { // trường hợp k lấy đc user info từ server
                            subscriber.onCompleted();
                            return;
                        }

                        mPreviousZaloId = mNextZaloIdList;
                        checklistzaloidforclient(mNextZaloIdList, subscriber, deep + 1);
                    }
                })
                .doOnTerminate(() -> mPreviousZaloId = null)
                .subscribe(subscriber::onNext);
    }

    private String transformZpId(List<ZaloFriendEntity> list) {
        if (Lists.isEmptyOrNull(list)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (ZaloFriendEntity entity : list) {
            if (builder.length() == 0) {
                builder.append(entity.userId);
            } else {
                builder.append(",");
                builder.append(entity.userId);
            }
        }
        return builder.toString();
    }

    private Observable<List<UserExistEntity>> fetchZaloPayId(String zaloidlist) {
        return mRequestService.checklistzaloidforclient(mUser.zaloPayId, mUser.accesstoken, zaloidlist)
                .map(response -> response.userList)
                .doOnNext(entities -> mLocalStorage.mergeZaloPayId(entities))
                ;
    }

    @Override
    public Observable<List<UserRedPackageEntity>> listZaloPayUser(List<Long> listZaloId) {
        return ObservableHelper.makeObservable(() -> listZaloFriend(listZaloId));
    }

    private List<UserRedPackageEntity> listZaloFriend(List<Long> listZaloId) {
        if (Lists.isEmptyOrNull(listZaloId)) {
            return Collections.emptyList();
        }
        List<ZaloFriendEntity> list = mLocalStorage.listZaloFriend(listZaloId);
        if (Lists.isEmptyOrNull(list)) {
            return Collections.emptyList();
        }
        List<UserRedPackageEntity> ret = new ArrayList<>();

        for (ZaloFriendEntity entity : list) {
            UserRedPackageEntity item = transformUserRedPackage(entity);
            if (item != null) {
                ret.add(item);
            }
        }

        return ret;
    }

    private UserRedPackageEntity transformUserRedPackage(ZaloFriendEntity entity) {
        UserRedPackageEntity ret = null;
        if (entity != null) {
            ret = new UserRedPackageEntity();
            ret.avatar = entity.avatar;
            ret.zaloName = entity.displayName;
            ret.zaloID = String.valueOf(entity.userId);
            ret.zaloPayID = entity.zaloPayId;
        }

        return ret;
    }

    public Observable<Boolean> syncContact() {
        return Observable.empty();
    }
}
