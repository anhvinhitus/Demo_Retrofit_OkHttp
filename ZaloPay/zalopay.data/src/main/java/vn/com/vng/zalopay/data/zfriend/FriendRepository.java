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
import vn.com.vng.zalopay.data.R;
import vn.com.vng.zalopay.data.ServerErrorMessage;
import vn.com.vng.zalopay.data.api.entity.FavoriteEntity;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.exception.StringResGenericException;
import vn.com.vng.zalopay.data.exception.UserNotFoundException;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.ZPCAlias.ColumnAlias;
import vn.com.vng.zalopay.data.zfriend.contactloader.Contact;
import vn.com.vng.zalopay.data.zfriend.contactloader.ContactFetcher;
import vn.com.vng.zalopay.data.zfriend.contactloader.ContactPhone;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZPProfile;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.Repository
 */
public class FriendRepository implements FriendStore.Repository {

    private static final int MAX_LENGTH_CHECK_LIST_ZALO_ID = 50;
    private static final int TIME_RELOAD = 5 * 60;
    private static final int TIMEOUT_REQUEST_FRIEND = 10;
    private static final int INTERVAL_SYNC_CONTACT = 259200;

    private final FriendStore.RequestService mRequestService;
    private final FriendStore.ZaloRequestService mZaloRequestService;
    private final FriendStore.LocalStorage mLocalStorage;
    private final User mUser;
    private final ContactFetcher mContactFetcher;

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
    public ZPProfile transform(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        try {
            ZPProfile profile = new ZPProfile();
            profile.userId = cursor.getLong(cursor.getColumnIndex(ColumnAlias.ZALO_ID));
            profile.displayName = cursor.getString(cursor.getColumnIndex(ColumnAlias.DISPLAY_NAME));
            profile.avatar = cursor.getString(cursor.getColumnIndex(ColumnAlias.AVATAR));
            profile.usingApp = cursor.getInt(cursor.getColumnIndex(ColumnAlias.USING_APP)) == 1;
            profile.zaloPayId = cursor.getString(cursor.getColumnIndex(ColumnAlias.ZALOPAY_ID));
            profile.normalizeDisplayName = cursor.getString(cursor.getColumnIndex(ColumnAlias.NORMALIZE_DISPLAY_NAME));
            profile.status = cursor.getInt(cursor.getColumnIndex(ColumnAlias.STATUS));
            profile.phonenumber = cursor.getString(cursor.getColumnIndex(ColumnAlias.PHONE_NUMBER));
            return profile;
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

    private Observable<Cursor> getZaloFriendsCursorLocal(boolean isWithPhone) {
        return makeObservable(() -> mLocalStorage.getZaloUserCursor(FriendConfig.sEnableSyncContact, isWithPhone));
    }

    @Override
    public Observable<Cursor> getZaloFriendsCursor(boolean isWithPhone) {
        Observable<Cursor> observableFriendLocal = getZaloFriendsCursorLocal(isWithPhone)
                .filter(cursor -> cursor != null && !cursor.isClosed() && cursor.getCount() > 0);

        Observable<Cursor> observableZaloApi = fetchZaloFriendFullInfo()
                .flatMap(aBoolean -> getZaloFriendsCursorLocal(isWithPhone));

        return Observable.concat(observableFriendLocal, observableZaloApi)
                .first();
    }

    @Override
    public Observable<Cursor> findFriends(String s, boolean isWithPhone) {
        return makeObservable(() -> mLocalStorage.findFriends(s, FriendConfig.sEnableSyncContact, isWithPhone));
    }

    @Override
    public Observable<List<ZPProfile>> findFriends(String s) {
        return findFriends(s, false)
                .map(cursor -> {
                    List<ZPProfile> ret = transformZaloFriend(cursor);
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                    return ret;
                });
    }

    @Override
    public Observable<List<ZPProfile>> getZaloFriendList() {
        Observable<List<ZPProfile>> observableFriendLocal = getFriendLocal()
                .filter(zaloFriends -> !Lists.isEmptyOrNull(zaloFriends));

        Observable<List<ZPProfile>> observableZaloApi = fetchZaloFriendFullInfo()
                .flatMap(aBoolean -> getFriendLocal());

        return Observable.concat(observableFriendLocal, observableZaloApi)
                .first();
    }

    private Observable<List<ZPProfile>> getFriendLocal() {
        return getZaloFriendsCursorLocal(false)
                .map(cursor -> {
                    List<ZPProfile> ret = transformZaloFriend(cursor);
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                    return ret;
                });
    }

    @Nullable
    private List<ZPProfile> transformZaloFriend(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return Collections.emptyList();
        }
        List<ZPProfile> ret = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                ZPProfile zaloProfile = transform(cursor);
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
        return makeObservable(mLocalStorage::getZaloUserWithoutZaloPayId)
                .map(entities -> Lists.chopped(entities, MAX_LENGTH_CHECK_LIST_ZALO_ID))
                .flatMap(Observable::from)
                .map(zaloids -> Strings.joinWithDelimiter(",", zaloids))
                .filter(s -> !TextUtils.isEmpty(s))
                .flatMap(this::fetchZaloPayUserByZaloId)
                .toList()
                .map(entities -> Boolean.TRUE);
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
                .map(aBoolean -> mLocalStorage.getLastTimeSyncContact())
                .filter(lastTime -> Math.abs(System.currentTimeMillis() / 1000 - lastTime) >= INTERVAL_SYNC_CONTACT)
                .flatMap(aLong -> syncImmediateContact());
    }

    @Override
    public Observable<Boolean> syncImmediateContact() {
        return makeObservable(mContactFetcher::fetchAll)
                .doOnNext(mLocalStorage::putContacts)
                .map(contacts -> Boolean.TRUE)
                .doOnCompleted(() -> mLocalStorage.setLastTimeSyncContact(System.currentTimeMillis() / 1000));
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

    @Override
    public Observable<Long> getUserContactBookCount() {
        return makeObservable(mLocalStorage::getUserContactBookCount);
    }

    @Override
    public Observable<Long> getZaloFriendListCount() {
        return makeObservable(mLocalStorage::getZaloFriendListCount);
    }

    @Override
    public Observable<Long> getLastTimeSyncContact() {
        return makeObservable(mLocalStorage::getLastTimeSyncContact);
    }

    @Override
    public Observable<List<String>> getAvatarContacts(int limit) {
        return makeObservable(() -> mLocalStorage.getAvatarContacts(limit))
                ;
    }

    @Override
    public Observable<List<String>> getAvatarZaloFriends(int limit) {
        return makeObservable(() -> mLocalStorage.getAvatarZaloFriends(limit))
                ;
    }

    @Override
    public Observable<Boolean> addFavorite(String phone, long zaloId) {
        return makeObservable(() -> mLocalStorage.addFavorite(phone, zaloId));
    }

    @Override
    public Observable<Boolean> removeFavorite(String phone, long zaloId) {
        return makeObservable(() -> mLocalStorage.removeFavorite(phone, zaloId));
    }

    @Override
    public Observable<List<FavoriteData>> getFavorites(int limit) {
        return makeObservable(() -> mLocalStorage.getFavorites(limit))
                .map(entities -> Lists.transform(entities, this::transform))
                ;
    }

    private FavoriteData transform(FavoriteEntity entity) {
        if (entity == null) {
            return null;
        }
        FavoriteData fav = new FavoriteData();
        fav.displayName = entity.displayName;
        fav.avatar = entity.avatar;
        fav.phoneNumber = entity.phoneNumber;
        fav.zaloId = entity.zaloId;

        return fav;
    }

    private Person transform(Contact entity) {
        if (entity == null || Lists.isEmptyOrNull(entity.numbers)) {
            return null;
        }
        Person person = new Person();
        person.avatar = entity.photoUri;
        person.displayName = entity.name;
        ContactPhone contactPhone = entity.numbers.get(0);
        try {
            person.phonenumber = Long.valueOf(contactPhone.number);
        } catch (Exception e) {
            Timber.d(e);
        }
        return person;
    }

    private Person transform(ZaloUserEntity entity) {
        if (entity == null) {
            return null;
        }

        Person person = new Person();
        person.avatar = entity.avatar;
        person.displayName = entity.displayName;
        person.zaloId = entity.userId;
        return person;
    }

    private Person transform(ZaloPayUserEntity entity) {
        if (entity == null) {
            return null;
        }

        Person person = new Person(entity.userid);
        try {
            person.zaloId = Long.valueOf(entity.zaloid);
        } catch (NumberFormatException e) {
            Timber.d(e, "Transform error : zalopayId [%s] zaloId [%s]", entity.userid, entity.zaloid);
        }

        person.avatar = entity.avatar;
        person.displayName = entity.displayName;
        person.zalopayname = entity.zalopayname;
        try {
            person.phonenumber = Long.valueOf(entity.phonenumber);
        } catch (NumberFormatException e) {
            Timber.d(e, "parse phoneNumber number [%s]", entity.phonenumber);
        }
        person.status = entity.status;
        return person;
    }

}
