package vn.com.vng.zalopay.data.zpc;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.FavoriteEntity;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.api.response.ListUserExistResponse;
import vn.com.vng.zalopay.data.cache.SqlBaseScope;
import vn.com.vng.zalopay.data.zpc.contactloader.Contact;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.ZPCGetByPhone;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by huuhoa on 7/4/16.
 * Declaration for friend local storage, friend request service, friend repository
 */
public interface ZPCStore {
    interface LocalStorage extends SqlBaseScope {

        void putContacts(@Nullable List<Contact> contacts);

        void putZaloUser(List<ZaloUserEntity> val);

        void putZaloPayUser(@Nullable List<ZaloPayUserEntity> entities);

        @Nullable
        ZaloPayUserEntity getZaloPayUserByZaloId(long zaloId);

        @NonNull
        List<RedPacketUserEntity> getRedPacketUsersEntity(List<Long> zaloIds);

        Cursor getZaloUserCursor(boolean isSyncContact, boolean isWithPhone);

        Cursor findFriends(String s, boolean isSyncContact, boolean isWithPhone);

        @NonNull
        List<Long> getZaloUserWithoutZaloPayId();

        long getLastTimeSyncContact();

        void setLastTimeSyncContact(long time);

        boolean addFavorite(String phoneNumber, long zaloId);

        boolean removeFavorite(String phoneNumber, long zaloId);

        long getUserContactBookCount();

        long getZaloFriendListCount();

        List<String> getAvatarContacts(int limit);

        List<String> getAvatarZaloFriends(int limit);

        List<FavoriteEntity> getFavorites(int limit);
    }

    interface ZaloRequestService {
        Observable<List<ZaloUserEntity>> fetchFriendList();
    }

    interface RequestService {

        @API_NAME(https = ZPEvents.API_UM_CHECKLISTZALOIDFORCLIENT, connector = ZPEvents.CONNECTOR_UM_CHECKLISTZALOIDFORCLIENT)
        @GET(Constants.UM_API.CHECKLISTZALOIDFORCLIENT)
        Observable<ListUserExistResponse> checklistzaloidforclient(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("zaloidlist") String zaloidlist);
        @GET(Constants.UM_API.GETUSERINFOBYPHONE)
        Observable<ZPCGetByPhone> getuserinfobyphone(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("phonenumber") String phone);
    }

    /**
     * Declaration for FriendStore.Repository
     */
    interface Repository {

        Observable<Boolean> retrieveZaloFriendsAsNeeded();

        Observable<Boolean> shouldUpdateFriendList();

        Observable<Boolean> fetchZaloFriends();

        Observable<Cursor> getZaloFriendsCursor(boolean isWithPhone);

        Observable<Cursor> findFriends(String s, boolean isWithPhone);

        Observable<List<ZPProfile>> findFriends(String s);

        @Nullable
        ZPProfile transform(Cursor cursor);

        Observable<List<ZPProfile>> getZaloFriendList();

        Observable<Boolean> checkListZaloIdForClient();

        Observable<List<RedPacketUserEntity>> getListUserZaloPay(List<Long> listZaloId);

        Observable<Boolean> syncContact();

        Observable<Boolean> syncImmediateContact();

        Observable<Boolean> fetchZaloFriendFullInfo();

        Observable<Person> getUserInfo(long zaloId);

        Observable<Long> getUserContactBookCount();

        Observable<Long> getZaloFriendListCount();

        Observable<Long> getLastTimeSyncContact();

        Observable<List<String>> getAvatarContacts(int limit);

        Observable<List<String>> getAvatarZaloFriends(int limit);

        Observable<Boolean> addFavorite(@Nullable String phone, long zaloId);

        Observable<Boolean> removeFavorite(@Nullable String phone, long zaloId);

        Observable<List<FavoriteData>> getFavorites(int limit);

        Observable<ZPCGetByPhone> getUserInfoByPhone(String userID, String token, String phone);

    }
}
