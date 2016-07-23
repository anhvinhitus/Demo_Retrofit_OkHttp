package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;

/**
 * Created by longlv on 13/07/2016.
 * Implementation for RedPacketStore.Repository
 */
public class RedPacketRepository implements RedPacketStore.Repository {

    private final RedPacketStore.RequestService mRequestService;
    private final RedPacketStore.LocalStorage mLocalStorage;
    private final RedPacketDataMapper mDataMapper;
    private final User user;

    public RedPacketRepository(RedPacketStore.RequestService requestService,
                               RedPacketStore.LocalStorage localStorage,
                               RedPacketDataMapper dataMapper,
                               User user) {
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
        this.mDataMapper = dataMapper;
        this.user = user;
    }

    @Override
    public Observable<BundleOrder> createBundleOrder(int quantity,
                                                     long totalLuck,
                                                     long amountEach,
                                                     int type,
                                                     String sendMessage) {
        return mRequestService.createBundleOrder(quantity, totalLuck, amountEach, type, user.uid, user.accesstoken, sendMessage)
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
    public Observable<Boolean> sendBundle(long bundleID, List<Long> friendList) {
        String friendListStr = Strings.joinWithDelimiter("|", friendList);
        return mRequestService.sendBundle(bundleID, friendListStr, user.uid, user.accesstoken)
                .map(BaseResponse::isSuccessfulResponse);
    }

    @Override
    public Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID) {
        return mRequestService.submitOpenPackage(packageID, bundleID, user.uid, user.accesstoken)
                .map(redPackageResponse -> new SubmitOpenPackage(bundleID, packageID, redPackageResponse.zptransid));
    }

    @Override
    public Observable<PackageStatus> getpackagestatus(long packageID, long zpTransID, String deviceId) {
        return mRequestService.getPackageStatus(packageID, zpTransID, user.uid, user.accesstoken, deviceId)
                .map(packageStatusResponse ->
                        new PackageStatus(packageStatusResponse.isprocessing,
                                packageStatusResponse.zptransid,
                                packageStatusResponse.reqdate,
                                packageStatusResponse.amount,
                                packageStatusResponse.balance,
                                packageStatusResponse.data));
    }

    @Override
    public Observable<GetSentBundle> getSentBundleList(long timestamp, int count, int order) {
        return mRequestService.getSentBundleList(timestamp, count, order, user.uid, user.accesstoken)
                .map(mDataMapper::transformToSentBundle)
                .doOnNext(this::insertSentBundles);
    }

    private void insertSentBundles(GetSentBundle getSentBundle) {
        if (getSentBundle == null
                || getSentBundle.sentbundlelist == null
                || getSentBundle.sentbundlelist.size() <= 0) {
            return;
        }
        List<SentBundle> sentBundles = getSentBundle.sentbundlelist;
        List<SentBundleGD> sentBundleGDList = mDataMapper.transformToSenBundleGD(sentBundles);
        mLocalStorage.putSentBundle(sentBundleGDList);
    }

    @Override
    public Observable<GetReceivePacket> getReceivedPackageList(long timestamp, int count, int order) {
        return mRequestService.getReceivedPackageList(timestamp, count, order, user.uid, user.accesstoken)
                .map(mDataMapper::transformToReceivePackage)
                .doOnNext(this::insertReceivePackages);
    }

    private void insertReceivePackages(GetReceivePacket getReceivePacket) {
        if (getReceivePacket == null
                || getReceivePacket.revpackageList == null
                || getReceivePacket.revpackageList.size() <= 0) {
            return;
        }
        List<ReceivePackage> receivePackages = getReceivePacket.revpackageList;
        List<ReceivePackageGD> receivePackageGDs = mDataMapper.transformToReceivePackageDB(receivePackages);
        mLocalStorage.putReceivePackages(receivePackageGDs);
    }

    @Override
    public Observable<List<PackageInBundle>> getPackageInBundleList(long bundleID, long timestamp, int count, int order) {
        return mRequestService.getPackageInBundleList(bundleID, timestamp, count, order, user.uid, user.accesstoken)
                .map(mDataMapper::transformToPackageInBundle)
                .doOnNext(this::insertPackageInBundle);
    }

    @Override
    public Observable<Boolean> isPacketOpen(String packetIdStr) {
        return ObservableHelper.makeObservable(() -> {
            long packetId = Long.parseLong(packetIdStr);
            return mLocalStorage.isPacketOpen(packetId);
        });
    }

    @Override
    public Observable<Void> setPacketIsOpen(long packageId, long amount) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.setPacketIsOpen(packageId, amount));
    }

    @Override
    public Observable<Void> addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message) {
        Timber.d("Add received red packet: [packetId: %s, bundleId: %s, sender: %s, avatar: %s, message: %s",
                packetId, bundleId, senderName, senderAvatar, message);
        return ObservableHelper.makeObservable(() -> mLocalStorage.addReceivedRedPacket(packetId, bundleId, senderName, senderAvatar, message));
    }

    @Override
    public Observable<ReceivePackage> getReceivedPacket(long packetId) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.getReceivedPacket(packetId));
    }

    private void insertPackageInBundle(List<PackageInBundle> packageInBundles) {
        List<PackageInBundleGD> packageInBundleGDs = mDataMapper.transformToPackageInBundleGD(packageInBundles);
        mLocalStorage.putPackageInBundle(packageInBundleGDs);
    }
}
