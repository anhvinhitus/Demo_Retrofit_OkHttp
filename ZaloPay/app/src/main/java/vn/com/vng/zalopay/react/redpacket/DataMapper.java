package vn.com.vng.zalopay.react.redpacket;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.util.ConvertHelper;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;

/**
 * Created by huuhoa on 7/24/16.
 * Transform data entities
 */
class DataMapper {
    static WritableMap transform(PackageStatus packageStatus) {
        if (packageStatus == null) {
            return null;
        }

        WritableMap writableMap = Arguments.createMap();
        writableMap.putBoolean("isprocessing", packageStatus.isProcessing);
        writableMap.putString("zpTransid", packageStatus.zpTransID);
        writableMap.putDouble("reqdate", packageStatus.reqdate);
        writableMap.putDouble("amount", packageStatus.amount);
        writableMap.putDouble("balance", packageStatus.balance);
        writableMap.putString("data", packageStatus.data);
        return writableMap;
    }

    static WritableMap transform(ReceivePackageGD packet) {
        if (packet == null) {
            return null;
        }

        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("packageid", String.valueOf(packet.id));
        writableMap.putString("bundleid", String.valueOf(ConvertHelper.unboxValue(packet.bundleID, 0)));
        writableMap.putString("sendername", packet.senderFullName);
        writableMap.putString("senderavatar", packet.senderAvatar);
        writableMap.putString("message", packet.message);
        writableMap.putDouble("amount", ConvertHelper.unboxValue(packet.amount, 0));
        writableMap.putDouble("opentime", packet.openedTime != null ? packet.openedTime.doubleValue() : 0);
        return writableMap;
    }

    static List<Long> transform(ReadableArray friends) {
        List<Long> friendList = new ArrayList<>();
        if (friends == null || friends.size() <= 0) {
            return friendList;
        }
        for (int i = 0; i < friends.size(); i++) {
            long friendId = 0;
            try {
                friendId = Long.valueOf(friends.getString(i));
            } catch (NumberFormatException e) {
                Timber.e(e, "transform friends from react native");
            }
            if (friendId <= 0) {
                continue;
            }
            friendList.add(friendId);
        }
        return friendList;
    }

    private static WritableMap transform(ZPProfile friend) {
        if (friend == null) {
            return null;
        }

        WritableMap friendItem = Arguments.createMap();
        friendItem.putString("displayName", friend.displayName);
        friendItem.putString("ascciDisplayName", friend.normalizeDisplayName);
        friendItem.putString("userId", String.valueOf(friend.userId));
        friendItem.putBoolean("usingApp", friend.usingApp && !TextUtils.isEmpty(friend.zaloPayId));
        friendItem.putString("avatar", friend.avatar);
        return friendItem;
    }

    static <T> WritableArray transform(List<T> list) {
        WritableArray array = Arguments.createArray();
        if (list == null) {
            Timber.d("transform: Null parameter");
            return array;
        }

        for (T item : list) {
            WritableMap map;
            if (item instanceof ZPProfile) {
                map = transform((ZPProfile) item);
            } else if (item instanceof PackageStatus) {
                map = transform((PackageStatus) item);
            } else {
                map = Arguments.createMap();
                Timber.e("Should not call this function! Parameter: %s", item);
            }
            array.pushMap(map);
        }
        return array;
    }

}
