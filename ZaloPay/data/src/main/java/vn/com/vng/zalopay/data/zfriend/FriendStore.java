package vn.com.vng.zalopay.data.zfriend;

import android.database.Cursor;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.List;

import de.greenrobot.dao.query.LazyList;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.UserExistEntity;
import vn.com.vng.zalopay.data.api.entity.UserRedPackageEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloFriendEntity;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.ListUserExistResponse;
import vn.com.vng.zalopay.data.cache.SqlBaseScope;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacket;

/**
 * Created by huuhoa on 7/4/16.
 * Declaration for friend local storage, friend request service, friend repository
 */
public interface FriendStore {
    interface LocalStorage extends SqlBaseScope {

        boolean isHaveZaloFriendDb();

        void put(List<ZaloFriendEntity> val);

        void put(ZaloFriendEntity val);

        List<ZaloFriendEntity> get();

        Cursor zaloFriendList();

        Cursor searchZaloFriendList(String s);

        void mergeZaloPayId(@Nullable List<UserExistEntity> list);

        List<ZaloFriendEntity> getZaloFriendWithoutZpId();

        List<ZaloFriendEntity> listZaloFriend(List<Long> list);
    }

    interface ZaloRequestService {
        Observable<List<ZaloFriendEntity>> fetchFriendList();
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

        Observable<Cursor> zaloFriendList();

        Observable<Cursor> searchZaloFriend(String s);

        @Nullable
        ZaloFriend transform(Cursor cursor);

        Observable<List<ZaloFriend>> getZaloFriendList();

        Observable<List<UserExistEntity>> checkListZaloIdForClient();

        Observable<List<UserRedPackageEntity>> listZaloPayUser(List<Long> listZaloId);

        Observable<Boolean> syncContact();
    }
}
