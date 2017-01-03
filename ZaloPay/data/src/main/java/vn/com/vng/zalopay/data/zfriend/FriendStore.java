package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.api.response.ListUserExistResponse;
import vn.com.vng.zalopay.data.cache.SqlBaseScope;
import vn.com.vng.zalopay.data.zfriend.contactloader.Contact;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by huuhoa on 7/4/16.
 * Declaration for friend local storage, friend request service, friend repository
 */
public interface FriendStore {
    interface LocalStorage extends SqlBaseScope {

        void putZaloUser(List<ZaloUserEntity> val);

        @NonNull
        List<ZaloUserEntity> getZaloUsers();

        @NonNull
        List<ZaloUserEntity> getZaloUsers(List<Long> zaloids);

        ZaloUserEntity getZaloUser(long zaloid);

        void putZaloUser(ZaloUserEntity entity);

        void putZaloPayUser(@Nullable List<ZaloPayUserEntity> entities);

        void putZaloPayUser(ZaloPayUserEntity entity);

        @NonNull
        List<ZaloPayUserEntity> getZaloPayUsers();

        @NonNull
        List<ZaloPayUserEntity> getZaloPayUsers(List<String> zalopayids);

        @Nullable
        ZaloPayUserEntity getZaloPayUser(String zalopayId);

        @NonNull
        List<RedPacketUserEntity> getRedPacketUsersEntity(List<Long> zaloids);

        void putContacts(@Nullable List<Contact> contacts);

        Cursor getZaloUserCursor();

        Cursor searchZaloFriendList(String s);

        @NonNull
        List<ZaloUserEntity> getZaloUserWithoutZaloPayId();

        long lastTimeSyncContact();

        void setLastTimeSyncContact(long time);
    }

    interface ZaloRequestService {
        Observable<List<ZaloUserEntity>> fetchFriendList();
    }

    interface RequestService {
        @GET("um/checklistzaloidforclient")
        Observable<ListUserExistResponse> checklistzaloidforclient(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("zaloidlist") String zaloidlist);
    }

    interface APICallback {
        void onResult(JSONObject var1);
    }

    interface SDKApi {
        void getFriendList(int pageIndex, int totalCount, APICallback callback);
    }

    /**
     * Declaration for FriendStore.Repository
     */
    interface Repository {

        Observable<Boolean> retrieveZaloFriendsAsNeeded();

        Observable<Boolean> shouldUpdateFriendList();

        Observable<Boolean> fetchZaloFriends();

        Observable<Cursor> fetchZaloFriendList();

        Observable<Cursor> getZaloFriendsCursor();

        Observable<Cursor> getZaloFriendsCursorLocal();

        Observable<Cursor> searchZaloFriend(String s);

        @Nullable
        ZaloFriend transform(Cursor cursor);

        Observable<List<ZaloFriend>> getZaloFriendList();

        Observable<Boolean> checkListZaloIdForClient();

        Observable<List<RedPacketUserEntity>> getListUserZaloPay(List<Long> listZaloId);

        Observable<Boolean> syncContact();


        Observable<Boolean> fetchZaloFriendFullInfo();

        Observable<Cursor> fetchZaloFriendCursorFullInfo();
    }
}
