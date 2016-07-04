package vn.com.vng.zalopay.transfer;

import android.content.Context;

import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.cache.model.ZaloFriend;
import vn.com.vng.zalopay.data.zfriend.FriendStore;

/**
 * Created by longlv on 13/06/2016.
 */
public class ZaloFriendsFactory {
    private SqlZaloPayScope sqlZaloPayScope;

    public ZaloFriendsFactory(SqlZaloPayScope sqlZaloPayScope) {
        this.sqlZaloPayScope = sqlZaloPayScope;
    }

    public void insertTransferRecent(vn.com.vng.zalopay.transfer.models.TransferRecent transferRecent) {
        TransferRecent transferEntity = convertTransactionRecent(transferRecent);
        sqlZaloPayScope.writeTransferRecent(transferEntity);
    }

    private TransferRecent convertTransactionRecent(vn.com.vng.zalopay.transfer.models.TransferRecent transferRecent) {
        if (transferRecent == null) {
            return null;
        }
        return new TransferRecent(transferRecent.getUserId(), transferRecent.getZaloPayId(), transferRecent.getUserName(), transferRecent.getDisplayName(), transferRecent.getAvatar(), transferRecent.getUserGender(), transferRecent.getBirthday(), transferRecent.isUsingApp(), transferRecent.getPhoneNumber(), transferRecent.getTransferType(), transferRecent.getAmount(), transferRecent.getMessage());
    }
}
