package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.UserExistEntity;
import vn.com.vng.zalopay.data.api.entity.UserRPEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloFriendEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.PhoneUtil;
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

    @Override
    public Observable<Boolean> fetchZaloFriends() {
        Timber.d("fetchZaloFriends");
        return mZaloRequestService.fetchFriendList()
                .doOnNext(entities -> mLocalStorage.put(entities, false))
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
        Timber.d("Retrieve Zalo Friends AsNeeded");
        return shouldUpdateFriendList()
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> fetchZaloFriends());
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

        Observable<List<ZaloFriend>> observableFriendLocal = getFriendLocal()
                .filter(zaloFriends -> !Lists.isEmptyOrNull(zaloFriends));
        Observable<List<ZaloFriend>> observableZaloApi = fetchZaloFriends()
                .last()
                .flatMap(aBoolean -> getFriendLocal());

        return Observable.concat(observableFriendLocal, observableZaloApi)
                .first();
    }

    private Observable<List<ZaloFriend>> getFriendLocal() {
        Timber.d("get friend zalo local");
        return ObservableHelper.makeObservable(() -> mLocalStorage.get())
                .map(this::transform);
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
    public Observable<List<UserRPEntity>> getListUserZaloPay(List<Long> listZaloId) {
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
    private List<Long> listUserWithoutZaloPayId(List<UserRPEntity> listUser, List<Long> listZaloId) {
        if (Lists.isEmptyOrNull(listUser)) {
            return listZaloId;
        }

        if (Lists.isEmptyOrNull(listZaloId)) {
            return Collections.emptyList();
        }

        List<Long> listUserWithZaloPayId = new ArrayList<>();
        for (UserRPEntity entity : listUser) {
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

    private Observable<List<UserRPEntity>> getListUserZaloPayLocal(List<Long> listZaloId) {
        return makeObservable(() -> mLocalStorage.listZaloFriend(listZaloId))
                .map(this::transformUserRedPackage);
    }

    private Observable<List<UserRPEntity>> fetchListUserZaloPay(List<Long> listUserWithoutId, List<Long> listZaloId) {
        Timber.d("fetchListUserZaloPay [%s]", listUserWithoutId.size());
        return fetchZaloPayId(Strings.joinWithDelimiter(",", listUserWithoutId))
                .onErrorResumeNext(throwable -> Observable.just(new ArrayList<>()))
                .flatMap(entities -> getListUserZaloPayLocal(listZaloId));
    }

    private UserRPEntity transformUserRedPackage(ZaloFriendEntity entity) {
        UserRPEntity ret = null;
        if (entity != null) {
            ret = new UserRPEntity();
            ret.avatar = entity.avatar;
            ret.zaloName = entity.displayName;
            ret.zaloID = String.valueOf(entity.userId);
            ret.zaloPayID = entity.zaloPayId;
        }

        return ret;
    }

    private List<UserRPEntity> transformUserRedPackage(List<ZaloFriendEntity> entities) {
        return Lists.transform(entities, this::transformUserRedPackage);
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
            Timber.d("begin sync current thread %s", Thread.currentThread().getName());
            ArrayList<Contact> listContact = mContactFetcher.fetchAll();
            List<ZaloFriendEntity> zEntities = mLocalStorage.listZaloFriendWithPhoneNumber();

            Timber.d(" list contact size [%s], list zalo friend have phone [%s],", listContact.size(), zEntities.size());

            if (Lists.isEmptyOrNull(listContact) || Lists.isEmptyOrNull(zEntities)) {
                return Boolean.TRUE;
            }
            int numberPhoneChange = 0;
            for (ZaloFriendEntity zEntity : zEntities) {

                int index = indexOfContact(listContact, zEntity);
                if (index > 0) {
                    Contact contact = listContact.get(index);
                    if (contact != null && !TextUtils.isEmpty(contact.name)) {
                        zEntity.displayName = contact.name;
                        numberPhoneChange++;
                    }
                }
            }
            Timber.d("beginSync: sync number %s", numberPhoneChange);
            if (numberPhoneChange > 0) {
                mLocalStorage.put(zEntities, true);
            }

            listContact.clear();
            zEntities.clear();

            return Boolean.TRUE;
        }).doOnCompleted(() -> mLocalStorage.setLastTimeSyncContact(System.currentTimeMillis() / 1000));
    }

    private int indexOfContact(ArrayList<Contact> listContact, ZaloFriendEntity zEntity) {
        int size = listContact.size();
        String numberPhone = PhoneUtil.formatPhoneNumber(zEntity.numberPhone);
        Timber.d("phone of user zalo %s", numberPhone);
        for (int i = 0; i < size; i++) {
            Contact contact = listContact.get(i);
            if (contact.inside(numberPhone)) {
                return i;
            }
        }
        return -1;
    }


}
