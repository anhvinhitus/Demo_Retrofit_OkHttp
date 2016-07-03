package vn.com.vng.zalopay.transfer;

import android.content.Context;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.cache.model.ZaloFriend;
import vn.vng.uicomponent.widget.util.StringUtils;

/**
 * Created by longlv on 13/06/2016.
 */
public class ZaloFriendsFactory {
    private final int OFFSET_FRIEND_LIST = 50;
    private final int TIME_RELOAD = 5 * 60; //5'

    private SqlZaloPayScope sqlZaloPayScope;

    public interface IZaloFriendListener {
        void onGetZaloFriendSuccess(List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends);

        void onGetZaloFriendError();

        void onZaloFriendUpdated();

        void onGetZaloFriendFinish();
    }

    public ZaloFriendsFactory(SqlZaloPayScope sqlZaloPayScope) {
        this.sqlZaloPayScope = sqlZaloPayScope;
    }

    public void insertZaloFriends(List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends) {
        List<ZaloFriend> zaloFriendList = convertZaloFriend(zaloFriends);
        sqlZaloPayScope.writeZaloFriends(zaloFriendList);
    }

    public void insertTransferRecent(vn.com.vng.zalopay.transfer.models.TransferRecent transferRecent) {
        TransferRecent transferEntity = convertTransactionRecent(transferRecent);
        sqlZaloPayScope.writeTransferRecent(transferEntity);
    }

    public void retrieveZaloFriendsAsNeeded(final Context context, final IZaloFriendListener listener) {
        AndroidApplication.instance().getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (sqlZaloPayScope != null && sqlZaloPayScope.isHaveZaloFriendDb()) {
                    long lasttime = sqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND, 0);
                    //check xem moi lay thi thoi
                    long currentTime = System.currentTimeMillis() / 1000;
                    if (currentTime - lasttime >= TIME_RELOAD) {
                        getFriendListServer(context, 0, listener);
                    } else {
                        if (listener != null) {
                            listener.onZaloFriendUpdated();
                        }
                    }
                } else {
                    getFriendListServer(context, 0, listener);
                }
            }
        });
    }

    public void getFriendListServer(final Context context, final IZaloFriendListener listener) {
        AndroidApplication.instance().getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getFriendListServer(context, 0, listener);
            }
        });
    }

    private void getFriendListServer(final Context context, final int pageIndex, final IZaloFriendListener listener) {
        Timber.d("getFriendListServer context:%s pageIndex:%s IZaloFriendListener:%s", context, pageIndex, listener);
        ZaloSDK.Instance.getFriendList(context, pageIndex, OFFSET_FRIEND_LIST, new ZaloOpenAPICallback() {
            @Override
            public void onResult(final JSONObject arg0) {
                JSONArray data;
                try {
                    data = arg0.getJSONArray("result");
                    Timber.d("getFriendListServer, result: %s", data.toString());
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    if (listener != null) {
                        listener.onGetZaloFriendError();
                    }
                    return;
                }

                if (data.length() <= 0) {
                    if (listener != null) {
                        listener.onGetZaloFriendFinish();
                    }
                } else {
                    if (data.length() >= OFFSET_FRIEND_LIST) {
                        getFriendListServer(context, (pageIndex + OFFSET_FRIEND_LIST), listener);
                    } else {
                        if (sqlZaloPayScope != null) {
                            sqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_ZALO_FRIEND, String.valueOf(System.currentTimeMillis() / 1000));
                        }
                        if (listener != null) {
                            listener.onGetZaloFriendFinish();
                        }
                    }
                    List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends = zaloFriends(data);
                    insertZaloFriends(zaloFriends);
                    if (listener != null) {
                        listener.onGetZaloFriendSuccess(zaloFriends);
                    }
                }
            }
        });
    }

    private List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends(final JSONArray jsonArray) {
        List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends = new ArrayList<>();
        if (jsonArray == null || jsonArray.length() <= 0) {
            return zaloFriends;
        }
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                vn.com.vng.zalopay.transfer.models.ZaloFriend zaloFriend = new vn.com.vng.zalopay.transfer.models.ZaloFriend(jsonObject);
                if (zaloFriend.getUserId() > 0 /*&& zaloFriend.isUsingApp()*/) {
                    zaloFriends.add(zaloFriend);
                }
            }
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        return zaloFriends;
    }

    private ZaloFriend convertZaloFriend(vn.com.vng.zalopay.transfer.models.ZaloFriend zaloFriend) {
        if (zaloFriend == null) {
            return null;
        }
        String fullTextSearch = StringUtils.diacriticsInVietnameseLowerCase(zaloFriend.getDisplayName());
        return new ZaloFriend(zaloFriend.getUserId(), zaloFriend.getUserName(), zaloFriend.getDisplayName(), zaloFriend.getAvatar(), zaloFriend.getUserGender(), "", zaloFriend.isUsingApp(), fullTextSearch);
    }

    private List<ZaloFriend> convertZaloFriend(List<vn.com.vng.zalopay.transfer.models.ZaloFriend> zaloFriends) {
        List<ZaloFriend> result = new ArrayList<>();
        if (zaloFriends == null || zaloFriends.size() <= 0) {
            return result;
        }
        for (vn.com.vng.zalopay.transfer.models.ZaloFriend zaloFriend : zaloFriends) {
            if (zaloFriend == null) {
                continue;
            }
            ZaloFriend zaloFriendTmp = convertZaloFriend(zaloFriend);
            result.add(zaloFriendTmp);
        }
        return result;
    }

    private TransferRecent convertTransactionRecent(vn.com.vng.zalopay.transfer.models.TransferRecent transferRecent) {
        if (transferRecent == null) {
            return null;
        }
        return new TransferRecent(transferRecent.getUserId(), transferRecent.getZaloPayId(), transferRecent.getUserName(), transferRecent.getDisplayName(), transferRecent.getAvatar(), transferRecent.getUserGender(), transferRecent.getBirthday(), transferRecent.isUsingApp(), transferRecent.getPhoneNumber(), transferRecent.getTransferType(), transferRecent.getAmount(), transferRecent.getMessage());
    }
}
