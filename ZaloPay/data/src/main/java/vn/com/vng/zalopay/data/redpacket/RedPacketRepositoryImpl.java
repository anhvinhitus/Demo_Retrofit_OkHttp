package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentPackageGD;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.SubmitOpenPackage;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpackage.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpackage.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpackage.SentBundle;
import vn.com.vng.zalopay.domain.model.redpackage.SentPackage;

/**
 * Created by longlv on 13/07/2016.
 * Implementation for RedPacketStore.Repository
 */
public class RedPacketRepositoryImpl implements RedPacketStore.Repository {

    public RedPacketStore.RequestService mRequestService;
    private RedPacketStore.LocalStorage mLocalStorage;
    private RedPacketDataMapper mDataMapper;
    public UserConfig userConfig;
    public User user;

    public RedPacketRepositoryImpl(RedPacketStore.RequestService requestService, RedPacketStore.LocalStorage localStorage, RedPacketDataMapper dataMapper, UserConfig userConfig, User user) {
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
        this.mDataMapper = dataMapper;
        this.user = user;
        this.userConfig = userConfig;
    }

    @Override
    public Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage) {
        return mRequestService.createBundleOrder(quantity, totalLuck, amountEach, type, user.uid, user.accesstoken, sendMessage)
                .map(bundleOrderResponse -> new BundleOrder(bundleOrderResponse.getAppid(), bundleOrderResponse.getZptranstoken(), bundleOrderResponse.apptransid, bundleOrderResponse.appuser, bundleOrderResponse.apptime, bundleOrderResponse.embeddata, bundleOrderResponse.item, bundleOrderResponse.amount, bundleOrderResponse.description, bundleOrderResponse.payoption, bundleOrderResponse.mac, bundleOrderResponse.bundleID));
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
                .map(packageStatusResponse -> new PackageStatus(packageStatusResponse.isprocessing, packageStatusResponse.zptransid, packageStatusResponse.reqdate, packageStatusResponse.amount, packageStatusResponse.balance, packageStatusResponse.data));
    }

    @Override
    public Observable<List<SentBundle>> getSentBundleList(long timestamp, int count, int order) {
        return mRequestService.getSentBundleList(timestamp, count, order, user.uid, user.accesstoken)
                .map(sentBundleResponse -> mDataMapper.transformToSentBundle(sentBundleResponse))
                .doOnNext(this::insertSentBundles);
    }

    private void insertSentBundles(List<SentBundle> sentBundles) {
        List<SentBundleGD> sentBundleGDList = mDataMapper.transformToSenBundleGD(sentBundles);
        mLocalStorage.putSentBundle(sentBundleGDList);
    }

    @Override
    public Observable<List<SentPackage>> getPackageInBundleList(long bundleID, long timestamp, int count, int order) {
        return mRequestService.getPackageInBundleList(bundleID, timestamp, count, order, user.uid, user.accesstoken)
                .map(packageInBundlesResponse -> mDataMapper.transformToSentPackage(packageInBundlesResponse))
                .doOnNext(sentPackages -> insertSentPackage(sentPackages, bundleID));
    }

    private void insertSentPackage(List<SentPackage> sentPackages, long bundleId) {
        List<SentPackageGD> sentPackageGDs = mDataMapper.transformToSentPackageGD(sentPackages, bundleId);
        mLocalStorage.putSentPackage(sentPackageGDs);
    }

    @Override
    public Observable<List<ReceivePackage>> getRevPackageList(long bundleID, long timestamp, int count, int order) {
        return mRequestService.getRevPackageList(bundleID, timestamp, count, order, user.uid, user.accesstoken)
                .map(revPackageInBundleResponse -> mDataMapper.transformToReceivePackage(revPackageInBundleResponse))
                .doOnNext(this::insertReceivePackages);
    }

    private void insertReceivePackages(List<ReceivePackage> receivePackages) {
        List<ReceivePackageGD> receivePackageGDs = mDataMapper.transformToReceivePackageDB(receivePackages);
        mLocalStorage.putReceivePackages(receivePackageGDs);
    }

}
