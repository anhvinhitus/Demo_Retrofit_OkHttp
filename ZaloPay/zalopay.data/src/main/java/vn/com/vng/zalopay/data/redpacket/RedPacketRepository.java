package vn.com.vng.zalopay.data.redpacket;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.RedPacketStatusEntity;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by longlv on 13/07/2016.
 * Implementation for RedPacketStore.Repository
 */
public class RedPacketRepository implements RedPacketStore.Repository {

    private final RedPacketStore.RequestService mRequestService;
    private final RedPacketStore.RequestTPEService mRequestTPEService;
    private final RedPacketStore.LocalStorage mLocalStorage;
    private final User user;
    private final int mAppId;
    private final Gson mGson;


    public RedPacketRepository(RedPacketStore.RequestService requestService,
                               RedPacketStore.RequestTPEService requestTPEService,
                               RedPacketStore.LocalStorage localStorage,
                               User user, int appId, Gson gson) {
        this.mRequestService = requestService;
        this.mRequestTPEService = requestTPEService;
        this.mLocalStorage = localStorage;
        this.user = user;
        this.mAppId = appId;
        this.mGson = gson;
        Timber.d("accessToken[%s]", this.user.accesstoken);
    }

    @Override
    public Observable<BundleOrder> createBundleOrder(int quantity,
                                                     long totalLuck,
                                                     long amountEach,
                                                     int type,
                                                     String sendMessage) {
        return mRequestService.createBundleOrder(quantity, totalLuck, amountEach, type, user.zaloPayId, user.accesstoken, sendMessage)
                .map(bundleOrderResponse ->
                        new BundleOrder(bundleOrderResponse.getAppid(),
                                bundleOrderResponse.getZptranstoken(),
                                bundleOrderResponse.apptransid,
                                bundleOrderResponse.appuser,
                                bundleOrderResponse.apptime,
                                bundleOrderResponse.embeddata,
                                bundleOrderResponse.item,
                                bundleOrderResponse.amount,
                                bundleOrderResponse.description,
                                bundleOrderResponse.payoption,
                                bundleOrderResponse.mac,
                                bundleOrderResponse.bundleID));
    }

    @Override
    public Observable<Boolean> sendBundle(long bundleID, List<RedPacketUserEntity> entities) {
        Timber.d("sendBundle: bundleId %s friend %s", bundleID, entities);
        return makeObservable(this::getSenderInfo)
                .flatMap(s -> mRequestService.submittosendbundlebyzalopayinfo(bundleID, mGson.toJson(filterUserWithZaloPayId(entities)), mGson.toJson(s), user.accesstoken))
                .map(BaseResponse::isSuccessfulResponse);
    }

    /**
     * Chỉ send bundle cho nhưng user có zalopayid
     */
    private List<RedPacketUserEntity> filterUserWithZaloPayId(List<RedPacketUserEntity> entities) {
        if (Lists.isEmptyOrNull(entities)) {
            return Collections.emptyList();
        }

        List<RedPacketUserEntity> ret = new ArrayList<>();
        List<RedPacketUserEntity> userWithoutZPId = new ArrayList<>();
        for (RedPacketUserEntity entity : entities) {
            if (TextUtils.isEmpty(entity.zaloPayID)) {
                userWithoutZPId.add(entity);
                continue;
            }

            ret.add(entity);
        }

        if (userWithoutZPId.size() > 0) {
            Timber.d("User without zalopayId size [%s]", userWithoutZPId.size());
        }

        return ret;
    }

    private RedPacketUserEntity getSenderInfo() {
        RedPacketUserEntity entity = new RedPacketUserEntity();
        entity.zaloPayID = user.hasZaloPayId() ? user.zaloPayId : "";
        entity.zaloID = String.valueOf(user.zaloId);
        entity.zaloName = TextUtils.isEmpty(user.displayName) ? "" : user.displayName;
        entity.avatar = TextUtils.isDigitsOnly(user.avatar) ? "" : user.avatar;
        return entity;
    }

    @Override
    public Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID) {
        return mRequestService.submitOpenPackage(packageID, bundleID, user.zaloPayId, user.accesstoken)
                .map(redPackageResponse -> new SubmitOpenPackage(bundleID, packageID, redPackageResponse.zptransid));
    }

    @Override
    public Observable<PackageStatus> getPackageStatus(long packageID, long zpTransID, String deviceId) {
        return mRequestTPEService.getPackageStatus(mAppId, packageID, zpTransID, user.zaloPayId, user.accesstoken, deviceId)
                .map(response -> {
                    PackageStatus item = new PackageStatus();
                    item.isProcessing = response.isprocessing;
                    item.zpTransID = response.zptransid;
                    item.reqdate = response.reqdate;
                    item.amount = response.amount;
                    item.balance = response.balance;
                    item.data = response.data;
                    return item;
                });
    }

    @Override
    public Observable<ReceivePackageGD> getPacketStatus(String packetIdStr) {
        return ObservableHelper.makeObservable(() -> {
            long packetId = Long.parseLong(packetIdStr);
            Timber.d("getPacketStatus packetId[%s]", packetId);
            return mLocalStorage.getPacketStatus(packetId);
        });
    }

    @Override
    public Observable<Void> setPacketStatus(long packageId, long amount, int status, String messageStatus) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.setPacketStatus(packageId, amount, status, messageStatus));
    }

    @Override
    public Observable<Void> addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message) {
        Timber.d("Add received red packet: [packetId: %s, bundleId: %s, sender: %s, avatar: %s, message: %s",
                packetId, bundleId, senderName, senderAvatar, message);
        return ObservableHelper.makeObservable(() -> mLocalStorage.addReceivedRedPacket(packetId, bundleId, senderName, senderAvatar, message));
    }

    @Override
    public Observable<Boolean> getListPackageStatus(List<Long> listpackageid) {
        if (Lists.isEmptyOrNull(listpackageid)) {
            return Observable.just(true);
        }

        String listPacketId = listpackageid.toString().replaceAll("\\s", "");
        Timber.d("getListPackageStatus: %s", listPacketId);
        return mRequestService.getListPackageStatus(listPacketId, user.zaloPayId, user.accesstoken)
                .doOnNext(response -> updateListPackageStatus(response.listpackagestatus, response.amount))
                .map(BaseResponse::isSuccessfulResponse)
                ;
    }

    private void updateListPackageStatus(List<RedPacketStatusEntity> listpackagestatus, long amount) {
        for (RedPacketStatusEntity entity : listpackagestatus) {
            entity.status = mapRedPacketStatus(entity.status).getValue();
            entity.amount = amount;
        }

        mLocalStorage.updateListPackageStatus(listpackagestatus);
    }

    //    -1: Không tìm thấy package
    //    1. INIT (chưa mở)
    //    2. OPENED (đã mở)
    //    3. REFUNDED (đã hoàn tiền)
    private RedPacketStatus mapRedPacketStatus(long status) {
        if (status == 1) {
            return RedPacketStatus.CanOpen;
        } else if (status == 2) {
            return RedPacketStatus.Opened;
        } else if (status == 3) {
            return RedPacketStatus.Refunded;
        } else {
            return RedPacketStatus.Unknown;
        }
    }
}
