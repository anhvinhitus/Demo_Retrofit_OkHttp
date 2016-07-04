package vn.com.vng.zalopay.transfer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.vng.uicomponent.widget.util.StringUtils;

/**
 * Created by huuhoa on 7/4/16.
 * Implementation for FriendStore.Repository
 */
public class FriendRepository implements FriendStoreRepository {
    private final int TIME_RELOAD = 5 * 60; //5'

    private FriendRequestService mRequestService;
    private FriendStore.LocalStorage mLocalStorage;
    private SqlZaloPayScope mSqlZaloPayScope;
    private Context mContext;

    public interface IZaloFriendListener {
        void onGetZaloFriendSuccess(List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends);

        void onGetZaloFriendError();

        void onZaloFriendUpdated();

        void onGetZaloFriendFinish();
    }


    public FriendRepository(FriendRequestService requestService, FriendStore.LocalStorage localStorage, SqlZaloPayScope sqlZaloPayScope, Context context) {
        mRequestService = requestService;
        mLocalStorage = localStorage;
        mSqlZaloPayScope = sqlZaloPayScope;
        mContext = context;
    }

    private ZaloFriendGD convertZaloFriend(vn.com.vng.zalopay.transfer.models.ZaloFriend zaloFriend) {
        if (zaloFriend == null) {
            return null;
        }
        String fullTextSearch = StringUtils.diacriticsInVietnameseLowerCase(zaloFriend.getDisplayName());
        return new ZaloFriendGD(zaloFriend.getUserId(), zaloFriend.getUserName(), zaloFriend.getDisplayName(), zaloFriend.getAvatar(), zaloFriend.getUserGender(), "", zaloFriend.isUsingApp(), fullTextSearch);
    }

    List<ZaloFriendGD> convertZaloFriends(List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends) {
        List<ZaloFriendGD> result = new ArrayList<>();
        if (zaloFriends == null || zaloFriends.size() <= 0) {
            return result;
        }
        for (vn.com.vng.zalopay.transfer.models.ZaloFriend zaloFriend : zaloFriends) {
            if (zaloFriend == null) {
                continue;
            }
            ZaloFriendGD zaloFriendTmp = convertZaloFriend(zaloFriend);
            result.add(zaloFriendTmp);
        }
        return result;
    }

    public void insertZaloFriends(List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends) {
        List<ZaloFriendGD> zaloFriendList = convertZaloFriends(zaloFriends);
        mLocalStorage.writeZaloFriends(zaloFriendList);
    }

    @Override
    public void retrieveZaloFriendsAsNeeded(final IZaloFriendListener listener) {
        AndroidApplication.instance().getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (mSqlZaloPayScope != null && mLocalStorage.isHaveZaloFriendDb()) {
                    long lasttime = mSqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND, 0);
                    //check xem moi lay thi thoi
                    long currentTime = System.currentTimeMillis() / 1000;
                    if (currentTime - lasttime >= TIME_RELOAD) {
                        fetchListFromServer(listener);
                    } else {
                        if (listener != null) {
                            listener.onZaloFriendUpdated();
                        }
                    }
                } else {
                    fetchListFromServer(listener);
                }
            }
        });
    }

    @Override
    public void fetchListFromServer(final IZaloFriendListener listener) {
        mRequestService.getFriendListServer(mContext).subscribe(new Subscriber<List<vn.com.vng.zalopay.transfer.models.ZaloFriend>>() {
            @Override
            public void onCompleted() {
                mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND, String.valueOf(System.currentTimeMillis() / 1000));
                if (listener != null) {
                    listener.onGetZaloFriendFinish();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (listener != null) {
                    listener.onGetZaloFriendError();
                }
            }

            @Override
            public void onNext(List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends) {
                insertZaloFriends(zaloFriends);
                if (listener != null) {
                    listener.onGetZaloFriendSuccess(zaloFriends);
                }
            }
        });
    }
}
