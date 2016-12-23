package vn.com.vng.zalopay.data.redpacket;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.UserRPEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.model.BundleGD;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by longlv on 13/07/2016.
 * Implementation for RedPacketStore.Repository
 */
public class RedPacketRepository implements RedPacketStore.Repository {
    private final int LIMIT_ITEMS_PER_REQ = 30;
    //1 : lấy các giao dịch có createTime > timestamp theo thứ tụ time giảm dần
    //-1 : lấy các giao dịch có createTime <= timestamp theo thứ tụ time giảm dần
    private final int ORDER = -1;

    private final RedPacketStore.RequestService mRequestService;
    private final RedPacketStore.RequestTPEService mRequestTPEService;
    private final RedPacketStore.LocalStorage mLocalStorage;
    private final RedPacketDataMapper mDataMapper;
    private final User user;
    private final int mAppId;
    private final Gson mGson;

    public RedPacketRepository(RedPacketStore.RequestService requestService,
                               RedPacketStore.RequestTPEService requestTPEService,
                               RedPacketStore.LocalStorage localStorage,
                               RedPacketDataMapper dataMapper,
                               User user, int appId, Gson gson) {
        this.mRequestService = requestService;
        this.mRequestTPEService = requestTPEService;
        this.mLocalStorage = localStorage;
        this.mDataMapper = dataMapper;
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
    public Observable<Boolean> sendBundle(long bundleID, List<UserRPEntity> entities) {
        Timber.d("sendBundle: bundleId %s friend %s", bundleID, entities);
        return makeObservable(this::getSenderInfo)
                .flatMap(s -> mRequestService.submittosendbundlebyzalopayinfo(bundleID, mGson.toJson(filterUserWithZaloPayId(entities)), mGson.toJson(s), user.accesstoken))
                .map(BaseResponse::isSuccessfulResponse);
    }

    /**
     * Chỉ send bundle cho nhưng user có zalopayid
     */
    private List<UserRPEntity> filterUserWithZaloPayId(List<UserRPEntity> entities) {
        if (Lists.isEmptyOrNull(entities)) {
            return entities;
        }

        List<UserRPEntity> ret = new ArrayList<>();
        List<UserRPEntity> userWithoutZPId = new ArrayList<>();
        for (UserRPEntity entity : entities) {
            if (TextUtils.isEmpty(entity.zaloPayID)) {
                userWithoutZPId.add(entity);
                continue;
            }

            ret.add(entity);
        }

        if (userWithoutZPId.size() > 0) {
            Timber.d("User without zalopayId size [%s]", ret.size());
        }

        if (ret.size() == 0) {
            return entities;
        }

        return ret;
    }

    private UserRPEntity getSenderInfo() {
        UserRPEntity entity = new UserRPEntity();
        entity.zaloPayID = TextUtils.isEmpty(user.zaloPayId) ? "" : user.zaloPayId;
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
    public Observable<PackageStatus> getpackagestatus(long packageID, long zpTransID, String deviceId) {
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

    private void insertSentBundles(GetSentBundle getSentBundle) {
        if (getSentBundle == null) {
            return;
        }
        List<SentBundleGD> sentBundleGDList = Lists.transform(getSentBundle.sentbundlelist, mDataMapper::transform);
        if (sentBundleGDList != null) {
            mLocalStorage.putSentBundle(sentBundleGDList);
        }
    }

    @Override
    public Observable<GetReceivePacket> getReceivedPackagesServer(long timeCreate, int count, int order) {
        return mRequestService.getReceivedPackageList(timeCreate, count, order, user.zaloPayId, user.accesstoken)
                .map(mDataMapper::transformToGetRevPacket)
                .doOnNext(this::insertRevPacketSummary)
                .doOnNext(this::insertReceivePackages)
                .doOnNext(this::insertBundles);
    }

    @Override
    public Observable<GetReceivePacket> getReceivePacketList(long openTime, final int count) {
        Timber.d("getReceivePacketList openTime [%s] count [%s]", String.valueOf(openTime), String.valueOf(count));
        if (shouldGetReceivePacketFromServer(openTime, count)) {
            return getReceivePacketListCloud(openTime, count)
                    .onErrorResumeNext(throwable -> {
                        Timber.d("getReceivePacketList onErrorResumeNext throwable: [%s]", throwable);
                        return getReceivePacketListCache(openTime, count);
                    });
        } else {
            return getReceivePacketListCache(openTime, count);
        }
    }

    private Observable<GetReceivePacket> getReceivePacketListCache(long openTime, final int count) {
        return Observable.zip(mLocalStorage.getReceiveBundle(openTime, count),
                mLocalStorage.getReceivePacketSummary(),
                (receivePackages, getReceivePacket) -> {
                    GetReceivePacket item = new GetReceivePacket();
                    item.totalofrevamount = getReceivePacket.totalofrevamount;
                    item.totalofrevpackage = getReceivePacket.totalofrevpackage;
                    item.numofluckiestdraw = getReceivePacket.numofluckiestdraw;
                    item.revpackageList = receivePackages;
                    return item;
                });
    }

    private Observable<GetReceivePacket> getReceivePacketListCloud(long openTime, final int count) {
        return getReceivedPackagesServer(openTime, count, ORDER)
                .flatMap(getReceivePacket -> getReceivePacketListCache(openTime, count));
    }


    private boolean shouldGetReceivePacketFromServer(long createTime, int count) {
        return !mLocalStorage.isHaveReceivePacketInDb(createTime, count);
    }

    private void insertReceivePackages(GetReceivePacket getReceivePacket) {
        if (getReceivePacket == null) {
            return;
        }

        List<ReceivePackageGD> receivePackageGDs =
                Lists.transform(getReceivePacket.revpackageList,
                        mDataMapper::transform);
        if (receivePackageGDs != null) {
            mLocalStorage.putReceivePackages(receivePackageGDs);
        }
    }

    private void insertBundles(GetReceivePacket getReceivePacket) {
        List<BundleGD> bundleGDs = mDataMapper.transformToBundleGD(getReceivePacket);
        if (Lists.isEmptyOrNull(bundleGDs)) {
            return;
        }
        mLocalStorage.putBundle(bundleGDs);
    }

    private void insertBundles(GetSentBundle getSentBundle) {
        List<BundleGD> bundleGDs = mDataMapper.transformToBundleGD(getSentBundle);
        if (Lists.isEmptyOrNull(bundleGDs)) {
            return;
        }
        mLocalStorage.putBundle(bundleGDs);
    }

    @Override
    public Observable<Boolean> getAllPacketInBundleServer(long bundleId) {
        Timber.d("getAllPacketInBundleServer bundleId [%s]", bundleId);
        long timestamp = 0;
        int count = LIMIT_ITEMS_PER_REQ;
        int order = ORDER;
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                getPackageInBundlesServer(bundleId, timestamp, count, order, subscriber);
            }
        });
    }

    private void getPackageInBundlesServer(final long bundleId, final long timestamp,
                                           final int count, final int sortOrder,
                                           Subscriber<? super Boolean> subscriber) {
        Timber.d("getPackageInBundlesServer %s ", timestamp);
        mRequestService.getPackageInBundleList(bundleId, timestamp, count, sortOrder, user.zaloPayId, user.accesstoken)
                .map(mDataMapper::transformToPackageInBundle)
                .doOnNext(this::insertPackageInBundle)
                .doOnNext(packageInBundles -> {
                    subscriber.onNext(true);

                    if (packageInBundles != null && packageInBundles.size() >= count) {
                        long newTimeStamp = packageInBundles.get(packageInBundles.size() - 1).openTime;
                        getPackageInBundlesServer(bundleId, newTimeStamp, count, sortOrder, subscriber);
                    } else {
                        mLocalStorage.updateLastTimeGetPackage(bundleId);
                        subscriber.onCompleted();
                    }
                })
                .doOnError(subscriber::onError)
                .subscribe(new DefaultSubscriber<>());
    }

    @Override
    public Observable<List<PackageInBundle>> getPacketsInBundle(long bundleId) {
        if (shouldUpdatePacketsForBundle(bundleId)) {
            return Observable.create(new Observable.OnSubscribe<List<PackageInBundle>>() {
                @Override
                public void call(Subscriber<? super List<PackageInBundle>> subscriber) {
                    Timber.d("Begin to fetch packets from server for bundle: %s", bundleId);
                    getAllPacketInBundleServer(bundleId).subscribeOn(Schedulers.io()).doOnCompleted(() -> {
                        Timber.d("Finished fetching packets for bundle %s", bundleId);
                        mLocalStorage.getPackageInBundle(bundleId)
                                .subscribe(subscriber);
                    }).subscribe(new DefaultSubscriber<>());
                }
            });

        } else {
            return mLocalStorage.getPackageInBundle(bundleId);
        }
    }

    /**
     * Should Update Packets For Bundle?
     * Note: Package can't open after 24 hours from the time user creates red package.
     * If (LastTimeGetPackageFromServer - createTime) > 24h then shouldn't
     * else should get package from server
     *
     * @param bundleId bundleId
     * @return should or shouldn't get data from server.
     */
    private boolean shouldUpdatePacketsForBundle(long bundleId) {
        BundleGD bundleGD = mLocalStorage.getBundle(bundleId);
        if (bundleGD == null) {
            return true;
        }
        Long lastTimeGetData = bundleGD.lastTimeGetPackage;
        Long createTime = bundleGD.createTime;

        if (lastTimeGetData == null || createTime == null || createTime <= 0) {
            Timber.d("LastTime get data or createTime is NULL");
            return true;
        }

        Timber.d("Last open time: %s, %s, %s", lastTimeGetData, createTime, lastTimeGetData - createTime);
        boolean shouldGetDataFromServer = !((lastTimeGetData - createTime) > 1000 * 60 * 60 * 24);
        Timber.d("shouldGetDataFromServer [%s]", shouldGetDataFromServer);
        return shouldGetDataFromServer;
    }

    @Override
    public Observable<ReceivePackageGD> getPacketStatus(String packetIdStr) {
        return ObservableHelper.makeObservable(() -> {
            long packetId = Long.parseLong(packetIdStr);
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
    public Observable<RedPacketAppInfo> getAppInfoServer(String checksum) {
        return mRequestService.getAppInfo(checksum, user.zaloPayId, user.accesstoken)
                .map(mDataMapper::transform)
                .doOnNext(mLocalStorage::putRedPacketAppInfo);
    }

    @Override
    public Observable<RedPacketAppInfo> getRedPacketAppInfo() {
        if (shouldUpdateRedPacketAppInfo()) {
            return Observable.create(new Observable.OnSubscribe<RedPacketAppInfo>() {
                @Override
                public void call(Subscriber<? super RedPacketAppInfo> subscriber) {
                    Timber.d("Begin to fetch RedPacketAppInfo from server");
                    RedPacketAppInfo redPacketAppInfo = mLocalStorage.getRedPacketAppInfo();
                    String checksum = redPacketAppInfo == null ? "" : redPacketAppInfo.checksum;
                    getAppInfoServer(checksum).subscribeOn(Schedulers.io()).doOnCompleted(() -> {
                        Timber.d("Finished fetching AppInfo");
                        ObservableHelper.makeObservable(mLocalStorage::getRedPacketAppInfo).subscribe(subscriber);
                    }).subscribe(new DefaultSubscriber<>());
                }
            });

        } else {
            return ObservableHelper.makeObservable(mLocalStorage::getRedPacketAppInfo);
        }
    }

    @Override
    public Observable<Void> setBundleStatus(long bundleId, int status) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.setBundleStatus(bundleId, status));
    }

    private boolean shouldUpdateRedPacketAppInfo() {
        RedPacketAppInfo redPacketAppInfo = mLocalStorage.getRedPacketAppInfo();
        if (redPacketAppInfo == null) {
            return true;
        } else if (TextUtils.isEmpty(redPacketAppInfo.checksum)) {
            return true;
        } else {
            long expiredTime = redPacketAppInfo.expiredTime;
            long currentTime = System.currentTimeMillis();
            if (currentTime > expiredTime) {
                return true;
            }
        }
        return false;
    }

    private void insertRevPacketSummary(GetReceivePacket getReceivePacket) {
        if (getReceivePacket == null) {
            return;
        }
        ReceivePacketSummaryDB data = new ReceivePacketSummaryDB();
        data.id = null;
        data.totalOfRevamount = getReceivePacket.totalofrevamount;
        data.totalOfLuckiestDraw = getReceivePacket.numofluckiestdraw;
        data.totalOfRevPackage = getReceivePacket.totalofrevpackage;
        data.timeCreate = System.currentTimeMillis();
        mLocalStorage.putReceivePacketSummary(data);
    }

    @Override
    public Observable<ReceivePackage> getReceivedPacket(long packetId) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.getReceivedPacket(packetId));
    }

    @Override
    public Observable<GetSentBundle> getSentBundleListServer(long timestamp, int count, int order) {
        return mRequestService.getSentBundleList(timestamp, count, order, user.zaloPayId, user.accesstoken)
                .map(mDataMapper::transformToSentBundleSummary)
                .doOnNext(this::insertSentBundleSummary)
//                .map(mDataMapper::transformToSentBundles)
                .doOnNext(this::insertSentBundles)
                .doOnNext(this::insertBundles);
    }

    private void insertSentBundleSummary(GetSentBundle getSentBundle) {
        if (getSentBundle == null) {
            return;
        }

        SentBundleSummaryDB data = new SentBundleSummaryDB();
        data.id = null;
        data.totalOfSentAmount = getSentBundle.totalofsentamount;
        data.totalOfSentBundle = getSentBundle.totalofsentbundle;
        data.timeCreate = System.currentTimeMillis();
        mLocalStorage.putSentBundleSummary(data);
    }

    @Override
    public Observable<GetSentBundle> getSentBundleList(final long timeCreate, final int count) {
        Timber.d("getSentBundleList timeCreate [%s] count [%s]", String.valueOf(timeCreate), String.valueOf(count));
        if (shouldGetSentBundleFromServer(timeCreate, count)) {
            return getSentBundleListCloud(timeCreate, count)
                    .onErrorResumeNext(throwable -> {
                        Timber.d("getSentBundleList onErrorResumeNext throwable: [%s]", throwable);
                        return getSentBundleListCache(timeCreate, count);
                    });
        } else {
            return getSentBundleListCache(timeCreate, count);
        }
    }

    private Observable<GetSentBundle> getSentBundleListCache(long timeCreate, int count) {
        return Observable.zip(mLocalStorage.getSentBundle(timeCreate, count), mLocalStorage.getSentBundleSummary(),
                (sentBundles, getSentBundle) -> {
                    GetSentBundle bundle = new GetSentBundle();
                    bundle.totalofsentamount = getSentBundle.totalofsentamount;
                    bundle.totalofsentbundle = getSentBundle.totalofsentbundle;
                    bundle.sentbundlelist = sentBundles;
                    return bundle;
                });
    }

    private Observable<GetSentBundle> getSentBundleListCloud(long timeCreate, int count) {
        return getSentBundleListServer(timeCreate, count, ORDER).flatMap(getSentBundle -> getSentBundleListCache(timeCreate, count));
    }

    private boolean shouldGetSentBundleFromServer(long createTime, int count) {
        return !mLocalStorage.isHaveSentBundleInDb(createTime, count);
    }

    private void insertPackageInBundle(List<PackageInBundle> packageInBundles) {
        mLocalStorage.putPackageInBundle(Lists.transform(packageInBundles, mDataMapper::transform));
    }
}
