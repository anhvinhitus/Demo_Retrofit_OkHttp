package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.contactloader.Contact;
import vn.com.vng.zalopay.data.zfriend.contactloader.ContactFetcher;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

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

    private ContactFetcher mContactFetcher;

    public FriendRepository(User user, FriendStore.ZaloRequestService zaloRequestService,
                            FriendStore.RequestService requestService,
                            FriendStore.LocalStorage localStorage, ContactFetcher contactFetcher) {
        mRequestService = requestService;
        mLocalStorage = localStorage;
        mZaloRequestService = zaloRequestService;
        mUser = user;
        mContactFetcher = contactFetcher;
    }

    /**
     * Get danh sánh friend zalo, đồng thời get zalopayid với những user đã using-app
     */

    @Override
    public Observable<Boolean> fetchZaloFriends() {
        Timber.d("fetchZaloFriends");
        return mZaloRequestService.fetchFriendList()
                .doOnNext(mLocalStorage::putZaloUser)
                .last()
                .timeout(TIMEOUT_REQUEST_FRIEND, TimeUnit.SECONDS)
                .map(entities -> Boolean.TRUE)
                .doOnCompleted(this::updateTimeStamp)
                ;
    }

    @Override
    public Observable<Cursor> fetchZaloFriendList() {
        return fetchZaloFriends()
                .flatMap(aBoolean -> getZaloFriendsCursorLocal())
                ;
    }

    @Override
    public Observable<Boolean> retrieveZaloFriendsAsNeeded() {
        Timber.d("Retrieve Zalo Friends AsNeeded");
        return shouldUpdateFriendList()
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> fetchZaloFriendFullInfo());
    }

    @Nullable
    @Override
    public ZaloFriend transform(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        ZaloFriend zaloFriend = new ZaloFriend();
        zaloFriend.userId = cursor.getLong(ColumnIndex.ID);
        zaloFriend.userName = cursor.getString(ColumnIndex.USER_NAME);
        zaloFriend.displayName = cursor.getString(cursor.getColumnIndex(ColumnIndex.ALIAS_DISPLAY_NAME));
        zaloFriend.avatar = cursor.getString(ColumnIndex.AVATAR);
        zaloFriend.usingApp = cursor.getInt(ColumnIndex.USING_APP) == 1;
        zaloFriend.zaloPayId = cursor.getString(cursor.getColumnIndex(ColumnIndex.ZALOPAY_ID));
        return zaloFriend;
    }

    public Observable<Boolean> shouldUpdateFriendList() {
        return makeObservable(() -> {
            long lastUpdated = mLocalStorage.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND, 0);
            long currentTime = System.currentTimeMillis() / 1000;
            boolean flag = ((currentTime - lastUpdated) >= TIME_RELOAD);
            Timber.i("Should update: %s [current: %d, last: %d, offset: %d]", flag, currentTime, lastUpdated, currentTime - lastUpdated);
            return flag;
        });
    }


    private void updateTimeStamp() {
        Timber.d("Request to update DB timestamp for ZaloFriendList");
        mLocalStorage.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND,
                String.valueOf(System.currentTimeMillis() / 1000));
    }

    @Override
    public Observable<Cursor> getZaloFriendsCursorLocal() {
        return makeObservable(() -> mLocalStorage.getZaloUserCursor());
    }

    @Override
    public Observable<Cursor> getZaloFriendsCursor() {
        Observable<Cursor> observableFriendLocal = getZaloFriendsCursorLocal()
                .filter(cursor -> cursor != null && !cursor.isClosed() && cursor.getCount() > 0);

        Observable<Cursor> observableZaloApi = fetchZaloFriendCursorFullInfo();

        return Observable.concat(observableFriendLocal, observableZaloApi)
                .first();
    }

    @Override
    public Observable<Cursor> searchZaloFriend(String s) {
        return makeObservable(() -> mLocalStorage.searchZaloFriendList(s));
    }

    @Override
    public Observable<List<ZaloFriend>> getZaloFriendList() {

        Observable<List<ZaloFriend>> observableFriendLocal = getFriendLocal()
                .filter(zaloFriends -> !Lists.isEmptyOrNull(zaloFriends));

        Observable<List<ZaloFriend>> observableZaloApi = fetchZaloFriends()
                .flatMap(aBoolean -> getFriendLocal());

        return Observable.concat(observableFriendLocal, observableZaloApi)
                .first();
    }

    private Observable<List<ZaloFriend>> getFriendLocal() {
        Timber.d("get friend zalo local");
        return makeObservable(() -> mLocalStorage.getZaloUserCursor())
                .map(this::transformZaloFriend);
    }

    private List<ZaloFriend> transformZaloFriend(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return Collections.emptyList();
        }
        List<ZaloFriend> ret = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    ZaloFriend zaloFriend = transform(cursor);
                    ret.add(zaloFriend);
                    cursor.moveToNext();
                }
            }
        } finally {
            cursor.close();
        }

        return ret;
    }


    /**
     * Kiểm tra trong db có friend nào chưa có zalopay thì request
     **/
    @Override
    public Observable<Boolean> checkListZaloIdForClient() {
        return makeObservable(() -> mLocalStorage.getZaloUserWithoutZaloPayId())
                .map(this::transformZpId)
                .filter(s -> !TextUtils.isEmpty(s))
                .flatMap(this::fetchZaloPayId)
                .map(entities -> Boolean.TRUE);
    }

    private String transformZpId(List<ZaloUserEntity> list) {
        if (Lists.isEmptyOrNull(list)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (ZaloUserEntity entity : list) {
            if (builder.length() == 0) {
                builder.append(entity.userId);
            } else {
                builder.append(",");
                builder.append(entity.userId);
            }
        }
        return builder.toString();
    }

  /*   private String mPreviousZaloId = null;
 private void checklistzaloidforclient(String zaloidlist, Subscriber<? super List<ZaloPayUserEntity>> subscriber, int deep) {
        Timber.d("check list zaloid for client %s deep %s", zaloidlist, deep);
        Subscription subscription = fetchZaloPayId(zaloidlist)
                .doOnNext(entities -> {

                    if (subscriber.isUnsubscribed()) {
                        return;
                    }

                    List<ZaloUserEntity> mList = mLocalStorage.getZaloFriendWithoutZpId();
                    Timber.d("list zalo need merge %s ", Lists.isEmptyOrNull(mList) ? 0 : mList.size());
                    String mNextZaloIdList = transformZpId(mList);
                    if (TextUtils.isEmpty(mNextZaloIdList) || mNextZaloIdList.equals(mPreviousZaloId)) {
                        Timber.d("Check list zaloid or client on Complete");
                        subscriber.onCompleted();
                    } else {
                        mPreviousZaloId = mNextZaloIdList;
                        checklistzaloidforclient(mNextZaloIdList, subscriber, deep + 1);
                    }
                })
                .doOnTerminate(() -> mPreviousZaloId = null)
                .subscribe(subscriber::onNext, subscriber::onError);
    }
*/


    private Observable<List<ZaloPayUserEntity>> fetchZaloPayId(String zaloidlist) {
        Timber.d("fetch zalopay info: zaloids [%s]", zaloidlist);
        return mRequestService.checklistzaloidforclient(mUser.zaloPayId, mUser.accesstoken, zaloidlist)
                .map(response -> response.userList)
                .doOnNext(mLocalStorage::putZaloPayUser)
                ;
    }

    @Override
    public Observable<List<RedPacketUserEntity>> getListUserZaloPay(List<Long> listZaloId) {
        return getListUserZaloPayLocal(listZaloId)
                .map(entities -> listUserWithoutZaloPayId(entities, listZaloId))
                .flatMap(listUserWithoutId -> {
                    if (Lists.isEmptyOrNull(listUserWithoutId)) {
                        return getListUserZaloPayLocal(listZaloId);
                    } else {
                        return fetchListUserZaloPay(listUserWithoutId, listZaloId);
                    }
                });
    }

    @NonNull
    private List<Long> listUserWithoutZaloPayId(List<RedPacketUserEntity> listUser, List<Long> listZaloId) {
        if (Lists.isEmptyOrNull(listUser) || Lists.isEmptyOrNull(listZaloId)) {
            return listZaloId;
        }

        List<Long> listUserWithZaloPayId = new ArrayList<>();
        for (RedPacketUserEntity entity : listUser) {
            if (TextUtils.isEmpty(entity.zaloPayID)) {
                continue;
            }

            try {
                listUserWithZaloPayId.add(Long.valueOf(entity.zaloID));
            } catch (NumberFormatException e) {
                Timber.e(e, "error pasre zaloId [%s]", entity.zaloID);
            }
        }

        List<Long> listUserWithoutZaloPayId = new ArrayList<>();

        listUserWithoutZaloPayId.addAll(listZaloId);
        listUserWithoutZaloPayId.removeAll(listUserWithZaloPayId);
        Timber.d("list User Without ZaloPayId: [%s]", listUserWithoutZaloPayId.size());
        return listUserWithoutZaloPayId;
    }

    private Observable<List<RedPacketUserEntity>> getListUserZaloPayLocal(List<Long> listZaloId) {
        return makeObservable(() -> mLocalStorage.getRedPacketUsersEntity(listZaloId));
    }

    private Observable<List<RedPacketUserEntity>> fetchListUserZaloPay(List<Long> listUserWithoutId, List<Long> listZaloId) {
        Timber.d("fetchListUserZaloPay [%s]", listUserWithoutId.size());
        return fetchZaloPayId(Strings.joinWithDelimiter(",", listUserWithoutId))
                .onErrorResumeNext(throwable -> Observable.just(new ArrayList<>()))
                .flatMap(entities -> getListUserZaloPayLocal(listZaloId));
    }

    @Override
    public Observable<Boolean> syncContact() {
        return makeObservable(() -> mLocalStorage.lastTimeSyncContact())
                .filter(lastTime -> Math.abs(System.currentTimeMillis() / 1000 - lastTime) >= 259200) //3 Ngày.
                .flatMap(aLong -> beginSync());
    }

    private Observable<Boolean> beginSync() {
        Timber.d("begin Sync contact");
        return makeObservable(() -> {
            ArrayList<Contact> listContact = mContactFetcher.fetchAll();
            mLocalStorage.putContacts(listContact);
            return Boolean.TRUE;
        }).doOnCompleted(() -> mLocalStorage.setLastTimeSyncContact(System.currentTimeMillis() / 1000));
    }

    @Override
    public Observable<Boolean> fetchZaloFriendFullInfo() {

        Observable<Boolean> fetchZaloProfile = fetchZaloFriends();
        Observable<Boolean> fetchZaloPayInfo = checkListZaloIdForClient()
                .onErrorResumeNext(throwable -> Observable.just(Boolean.TRUE));

        return Observable.concat(fetchZaloProfile, fetchZaloPayInfo)
                .last();
    }

    @Override
    public Observable<Cursor> fetchZaloFriendCursorFullInfo() {
        return fetchZaloFriendFullInfo()
                .flatMap(aBoolean -> {
                    Timber.d("fetch zalo friend cursor full info call");
                    return getZaloFriendsCursorLocal();
                });
    }
}
