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
import vn.com.vng.zalopay.data.ServerErrorMessage;
import vn.com.vng.zalopay.data.R;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.exception.StringResGenericException;
import vn.com.vng.zalopay.data.exception.UserNotFoundException;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.contactloader.Contact;
import vn.com.vng.zalopay.data.zfriend.contactloader.ContactFetcher;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloProfile;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.Repository
 */
public class FriendRepository implements FriendStore.Repository {

    private final int TIME_RELOAD = 5 * 60; //5'

    private static final int TIMEOUT_REQUEST_FRIEND = 10;

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
        Timber.d("Fetch zalo friend");
        return mZaloRequestService.fetchFriendList()
                .doOnNext(mLocalStorage::putZaloUser)
                .last()
                .timeout(TIMEOUT_REQUEST_FRIEND, TimeUnit.SECONDS)
                .map(entities -> Boolean.TRUE)
                .doOnCompleted(this::updateTimeStamp)
                ;
    }


    @Override
    public Observable<Boolean> retrieveZaloFriendsAsNeeded() {
        return shouldUpdateFriendList()
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> fetchZaloFriendFullInfo());
    }

    @Nullable
    @Override
    public ZaloProfile transform(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        try {
            ZaloProfile zaloProfile = new ZaloProfile();
            zaloProfile.userId = cursor.getLong(ColumnIndex.ID);
            zaloProfile.userName = cursor.getString(ColumnIndex.USER_NAME);
            zaloProfile.displayName = cursor.getString(cursor.getColumnIndex(ColumnIndex.ALIAS_DISPLAY_NAME));
            zaloProfile.avatar = cursor.getString(ColumnIndex.AVATAR);
            zaloProfile.usingApp = cursor.getInt(ColumnIndex.USING_APP) == 1;
            zaloProfile.zaloPayId = cursor.getString(cursor.getColumnIndex(ColumnIndex.ZALOPAY_ID));
            zaloProfile.normalizeDisplayName = cursor.getString(cursor.getColumnIndex(ColumnIndex.ALIAS_FULL_TEXT_SEARCH));
            zaloProfile.status = cursor.getInt(cursor.getColumnIndex(ColumnIndex.STATUS));
            return zaloProfile;
        } catch (Exception e) {
            Timber.d(e, "Transform friend exception");
            return null;
        }
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
        mLocalStorage.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND,
                String.valueOf(System.currentTimeMillis() / 1000));
    }

    @Override
    public Observable<Cursor> getZaloFriendsCursorLocal() {
        return makeObservable(() -> mLocalStorage.getZaloUserCursor(FriendConfig.sEnableSyncContact));
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
        return makeObservable(() -> mLocalStorage.searchZaloFriendList(s, FriendConfig.sEnableSyncContact));
    }

    @Override
    public Observable<List<ZaloFriend>> findFriends(String s) {
        return searchZaloFriend(s)
                .map(cursor -> {
                    List<ZaloFriend> ret = transformZaloFriend(cursor);
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                    return ret;
                });
    }

    @Override
    public Observable<List<ZaloFriend>> getZaloFriendList() {
        Observable<List<ZaloProfile>> observableFriendLocal = getFriendLocal()
                .filter(zaloFriends -> !Lists.isEmptyOrNull(zaloFriends));

        Observable<List<ZaloProfile>> observableZaloApi = fetchZaloFriendFullInfo()
                .flatMap(aBoolean -> getFriendLocal());

        return Observable.concat(observableFriendLocal, observableZaloApi)
                .first();
    }

    private Observable<List<ZaloProfile>> getFriendLocal() {
        return getZaloFriendsCursorLocal()
                .map(cursor -> {
                    List<ZaloProfile> ret = transformZaloFriend(cursor);
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                    return ret;
                });
    }

    @Nullable
    @Override
    public List<ZaloFriend> transformZaloFriend(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return Collections.emptyList();
        }
        List<ZaloProfile> ret = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                ZaloProfile zaloProfile = transform(cursor);
                if (zaloProfile == null) {
                    continue;
                }

                ret.add(zaloProfile);
                cursor.moveToNext();
            }
        }

        return ret;
    }

    /**
     * Kiểm tra trong db có friend nào chưa có zalopay thì request
     * (get zalopayid from zaloid)
     **/
    @Override
    public Observable<Boolean> checkListZaloIdForClient() {
        return makeObservable(() -> mLocalStorage.getZaloUserWithoutZaloPayId())
                .map(this::transformZpId)
                .filter(s -> !TextUtils.isEmpty(s))
                .flatMap(this::fetchZaloPayUserByZaloId)
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

    private Observable<List<ZaloPayUserEntity>> fetchZaloPayUserByZaloId(String zaloidlist) {
        Timber.d("Fetching zalopay info : zaloidlist [%s]", zaloidlist);
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
                Timber.e(e, "Error pasre zaloId [%s]", entity.zaloID);
            }
        }

        List<Long> listUserWithoutZaloPayId = new ArrayList<>();

        listUserWithoutZaloPayId.addAll(listZaloId);
        listUserWithoutZaloPayId.removeAll(listUserWithZaloPayId);
        Timber.d("List user without zalopayid: size [%s]", listUserWithoutZaloPayId.size());
        return listUserWithoutZaloPayId;
    }

    private Observable<List<RedPacketUserEntity>> getListUserZaloPayLocal(List<Long> listZaloId) {
        return makeObservable(() -> mLocalStorage.getRedPacketUsersEntity(listZaloId));
    }

    private Observable<List<RedPacketUserEntity>> fetchListUserZaloPay(List<Long> listUserWithoutId, List<Long> listZaloId) {
        Timber.d("Fetch list user zalopay: size [%s]", listUserWithoutId.size());
        return fetchZaloPayUserByZaloId(Strings.joinWithDelimiter(",", listUserWithoutId))
                .onErrorResumeNext(throwable -> Observable.just(new ArrayList<>()))
                .flatMap(entities -> getListUserZaloPayLocal(listZaloId));
    }

    @Override
    public Observable<Boolean> syncContact() {
        return Observable.just(FriendConfig.sEnableSyncContact)
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> makeObservable(() -> mLocalStorage.lastTimeSyncContact()))
                .filter(lastTime -> Math.abs(System.currentTimeMillis() / 1000 - lastTime) >= 259200) //3 Ngày.
                .flatMap(aLong -> beginSync());
    }

    private Observable<Boolean> beginSync() {
        Timber.d("Begin sync contact");
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

    @Override
    public Observable<Person> getUserInfo(long zaloid) {

        Observable<ZaloPayUserEntity> mCacheObservable = makeObservable(() -> mLocalStorage.getZaloPayUserByZaloId(zaloid))
                .filter(entity -> entity != null && !TextUtils.isEmpty(entity.userid) && !TextUtils.isEmpty(entity.zalopayname));

        Observable<ZaloPayUserEntity> mFetchObservable = fetchZaloPayUserByZaloId(String.valueOf(zaloid))
                .flatMap(entities -> {
                    if (Lists.isEmptyOrNull(entities)) {
                        return Observable.error(new UserNotFoundException());
                    }

                    ZaloPayUserEntity entity = entities.get(0);
                    if (entity.status == ServerErrorMessage.USER_NOT_EXIST) {
                        return Observable.error(new UserNotFoundException());
                    } else if (entity.status == ServerErrorMessage.ZPW_ACCOUNT_SUSPENDED ||
                            entity.status == ServerErrorMessage.RECEIVER_IS_LOCKED ||
                            entity.status == ServerErrorMessage.USER_IS_LOCKED) {
                        return Observable.error(new StringResGenericException(R.string.exception_zpw_account_suspended));
                    } else if (TextUtils.isEmpty(entity.userid)) {
                        return Observable.error(new UserNotFoundException());
                    }

                    return Observable.just(entity);
                });

        return Observable.concat(mCacheObservable, mFetchObservable)
                .first()
                .map(this::transform)
                ;
    }


    private Person transform(ZaloPayUserEntity entity) {
        if (entity == null) {
            return null;
        }

        Person person = new Person(entity.userid);
        try {
            person.zaloId = Long.valueOf(entity.zaloid);
        } catch (NumberFormatException e) {
            Timber.e(e, "Transform error : zalopayId [%s] zaloid [%s]", entity.userid, entity.zaloid);
        }

        person.avatar = entity.avatar;
        person.displayName = entity.displayName;
        person.zalopayname = entity.zalopayname;
        person.phonenumber = entity.phonenumber;
        person.status = entity.status;

        return person;
    }
}
