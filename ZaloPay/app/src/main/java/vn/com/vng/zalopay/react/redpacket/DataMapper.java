package vn.com.vng.zalopay.react.redpacket;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

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

    static WritableMap transform(ReceivePackage packet) {
        if (packet == null) {
            return null;
        }

        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("packageid", String.valueOf(packet.packageID));
        writableMap.putString("bundleid", String.valueOf(packet.bundleID));
        writableMap.putString("sendername", packet.senderFullName);
        writableMap.putString("senderavatar", packet.senderAvatar);
        writableMap.putString("message", packet.message);
        writableMap.putDouble("amount", packet.amount);
        writableMap.putDouble("isluckiest", packet.isLuckiest);
        writableMap.putDouble("createtime", packet.createTime);
        writableMap.putDouble("opentime", packet.openedTime);
        return writableMap;
    }

    static WritableMap transform(SentBundle sentBundle) {
        WritableMap writableMap = Arguments.createMap();
        if (sentBundle == null) {
            return writableMap;
        }

        writableMap.putString("bundleid", String.valueOf(sentBundle.bundleID));
        writableMap.putString("sendzalopayid", sentBundle.sendZaloPayID);
        writableMap.putDouble("createtime", sentBundle.createTime);
        writableMap.putDouble("lastopentime", sentBundle.lastOpenTime);
        writableMap.putDouble("totalluck", sentBundle.totalLuck);
        writableMap.putDouble("type", sentBundle.type);
        writableMap.putDouble("numberpackage", sentBundle.numOfPackages);
        writableMap.putString("message", sentBundle.sendMessage);
        writableMap.putDouble("numberopenpackage", sentBundle.numOfOpenedPakages);
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

    static WritableMap transform(ZaloFriend friend) {
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
            if (item instanceof ZaloFriend) {
                map = transform((ZaloFriend) item);
            } else if (item instanceof PackageInBundle) {
                map = transform((PackageInBundle) item);
            } else if (item instanceof SentBundle) {
                map = transform((SentBundle) item);
            } else if (item instanceof ReceivePackage) {
                map = transform((ReceivePackage) item);
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

    static WritableMap transform(PackageInBundle packet) {
        if (packet == null) {
            return null;
        }

        WritableMap map = Arguments.createMap();
        map.putDouble("amount", packet.amount);
        map.putBoolean("isluckiest", packet.isLuckiest);
        map.putString("revavatarurl", packet.revAvatarURL);
        map.putString("revfullname", packet.revFullName);
        map.putDouble("opentime", packet.openTime);
        map.putString("packetid", String.valueOf(packet.packageID));
        return map;
    }

    public static WritableMap transform(GetSentBundle summary) {
        if (summary == null) {
            return null;
        }

        WritableMap map = Arguments.createMap();
        if (summary.totalofsentamount > 0) {
            map.putDouble("totalofsentamount", summary.totalofsentamount);
        }
        if (summary.totalofsentbundle > 0) {
            map.putDouble("totalofsentbundle", summary.totalofsentbundle);
        }
        WritableArray sentBundleArray = transform(summary.sentbundlelist);
        map.putArray("sentbundlelist", sentBundleArray);
        return map;
    }

    public static WritableMap transform(GetReceivePacket summary) {
        if (summary == null) {
            return null;
        }

        WritableMap map = Arguments.createMap();
        if (summary.totalofrevamount > 0) {
            map.putDouble("totalofrevamount", summary.totalofrevamount);
        }
        if (summary.totalofrevpackage > 0) {
            map.putDouble("totalofrevpackage", summary.totalofrevpackage);
        }
        if (summary.numofluckiestdraw > 0) {
            map.putDouble("totalofluckiestdraw", summary.numofluckiestdraw);
        }

        WritableArray receivePacketArray = transform(summary.revpackageList);
        map.putArray("revpackagelist", receivePacketArray);
        return map;
    }

    public static WritableMap transform(RedPacketAppInfo redPacketAppInfo) {
        if (redPacketAppInfo == null || redPacketAppInfo.appConfigEntity == null) {
            return null;
        }

        WritableMap map = Arguments.createMap();
        map.putDouble("minamounteach", redPacketAppInfo.appConfigEntity.minAmountEach);
        map.putDouble("maxtotalamountperbundle", redPacketAppInfo.appConfigEntity.maxTotalAmountPerBundle);
        map.putDouble("maxpackagequantity", redPacketAppInfo.appConfigEntity.maxPackageQuantity);
        map.putDouble("maxcounthist", redPacketAppInfo.appConfigEntity.maxCountHist);
        map.putDouble("maxmessagelength", redPacketAppInfo.appConfigEntity.maxMessageLength);
        map.putDouble("bundleexpiredtime", redPacketAppInfo.appConfigEntity.bundleExpiredTime);
        map.putDouble("mindivideamount", redPacketAppInfo.appConfigEntity.minDivideAmount);
        map.putDouble("maxamountperpackage", redPacketAppInfo.appConfigEntity.maxAmountPerPackage);
        return map;
    }
}
