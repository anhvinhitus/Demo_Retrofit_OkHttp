package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
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
    private final int LIMIT_ITEMS_PER_REQ = 30;
    //1 : lấy các giao dịch có createTime > timestamp theo thứ tụ time giảm dần
    //-1 : lấy các giao dịch có createTime <= timestamp theo thứ tụ time giảm dần
    private final int ORDER = -1;

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

    private void insertSentBundles(GetSentBundle getSentBundle) {
        if (getSentBundle == null) {
            return;
        }
        List<SentBundle> sentBundles = getSentBundle.sentbundlelist;
        if (sentBundles == null || sentBundles.size() <= 0) {
            return;
        }
        List<SentBundleGD> sentBundleGDList = mDataMapper.transformToSenBundleGD(sentBundles);
        mLocalStorage.putSentBundle(sentBundleGDList);
    }

    @Override
    public Observable<GetReceivePacket> getReceivedPackagesServer(long timeCreate, int count, int order) {
        return mRequestService.getReceivedPackageList(timeCreate, count, order, user.uid, user.accesstoken)
                .map(mDataMapper::transformToGetRevPacket)
                .doOnNext(this::insertRevPacketSummary)
                .doOnNext(this::insertReceivePackages);
    }

    @Override
    public Observable<GetReceivePacket> getReceivePacketList(long openTime, final int count) {
        Timber.d("getReceivePacketList openTime [%s] count [%s]", String.valueOf(openTime), String.valueOf(count));
        if (shouldGetReceivePacketFromServer(openTime, count)) {
            Timber.d("getReceivePacketList -> getReceivedPackagesServer");
            return Observable.create(new Observable.OnSubscribe<GetReceivePacket>() {
                @Override
                public void call(Subscriber<? super GetReceivePacket> subscriber) {
                    Timber.d("Begin to fetch ReceivePacketList from server for openTime [%s] count [%s]", openTime, count);
                    getReceivedPackagesServer(openTime, count, ORDER).subscribeOn(Schedulers.io()).doOnCompleted(() -> {
                        Timber.d("Finished fetching ReceivePacketList for openTime [%s] count [%s]", openTime, count);
                        Observable<List<ReceivePackage>> obReceivePacket = mLocalStorage.getReceiveBundle(openTime, count);
                        Observable<GetReceivePacket> obGetReceivePacket = mLocalStorage.getReceivePacketSummary();
                        Observable.zip(obReceivePacket, obGetReceivePacket, (receivePackages, getReceivePacket) -> {
                            long totalOfRevAmount = 0;
                            int totalOfRevPackage = 0;
                            int numOfLuckiestDraw = 0;
                            if (getReceivePacket != null) {
                                totalOfRevAmount = getReceivePacket.totalofrevamount;
                                totalOfRevPackage = getReceivePacket.totalofrevpackage;
                                numOfLuckiestDraw = getReceivePacket.numofluckiestdraw;
                            }
                            return new GetReceivePacket(totalOfRevAmount, totalOfRevPackage, numOfLuckiestDraw, receivePackages);

                        }).subscribe(new Observer<GetReceivePacket>() {
                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(GetReceivePacket getReceivePacket) {
                                subscriber.onNext(getReceivePacket);
                            }
                        });
                    }).subscribe(new DefaultSubscriber<>());
                }
            });
        } else {
            Timber.d("getReceivePacketList -> getReceivedPackagesDB");
            return Observable.create(new Observable.OnSubscribe<GetReceivePacket>() {
                @Override
                public void call(Subscriber<? super GetReceivePacket> subscriber) {
                    Timber.d("Finished fetching ReceivePacketList for openTime [%s] count [%s]", openTime, count);
                    Observable<List<ReceivePackage>> obReceivePacket = mLocalStorage.getReceiveBundle(openTime, count);
                    Observable<GetReceivePacket> obGetReceivePacket = mLocalStorage.getReceivePacketSummary();
                    Observable.zip(obReceivePacket, obGetReceivePacket, (receivePackages, getReceivePacket) ->
                            new GetReceivePacket(getReceivePacket.totalofrevamount, getReceivePacket.totalofrevpackage,
                                    getReceivePacket.numofluckiestdraw, receivePackages))
                            .subscribe(new Observer<GetReceivePacket>() {
                                @Override
                                public void onCompleted() {
                                    subscriber.onCompleted();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    subscriber.onError(e);
                                }

                                @Override
                                public void onNext(GetReceivePacket getReceivePacket) {
                                    subscriber.onNext(getReceivePacket);
                                }
                            });
                }
            });
        }
    }

    private boolean shouldGetReceivePacketFromServer(long createTime, int count) {
        return !mLocalStorage.isHaveReceivePacketInDb(createTime, count);
    }

    private void getReceivePacketServer(long openedTime, int count, int sortOrder, Subscriber<? super Boolean> subscriber) {
        Timber.d("getReceivePacketServer openedTime [%s] ", openedTime);
        mRequestService.getReceivedPackageList(openedTime, count, sortOrder, user.uid, user.accesstoken)
                .map(mDataMapper::transformToGetRevPacket)
                .doOnNext(this::insertRevPacketSummary)
                .doOnNext(this::insertReceivePackages)
                .doOnNext(getReceivePacket -> {
                    subscriber.onNext(true);
                    if (getReceivePacket == null ||
                            getReceivePacket.revpackageList == null ||
                            getReceivePacket.revpackageList.size() < count) {
                        subscriber.onCompleted();
                    } else {
                        List<ReceivePackage> receivePackages = getReceivePacket.revpackageList;
                        long newTimeStamp = receivePackages.get(receivePackages.size() - 1).createTime;
                        getReceivePacketServer(newTimeStamp, count, sortOrder, subscriber);
                    }
                })
                .doOnError(subscriber::onError)
                .subscribe(new DefaultSubscriber<>());
    }

    @Override
    public Observable<Boolean> getAllReceivePacketServer() {
        int timestamp = 0;
        int count = LIMIT_ITEMS_PER_REQ;
        int sortOrder = ORDER;
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                getReceivePacketServer(timestamp, count, sortOrder, subscriber);
            }
        });
    }

    private void insertReceivePackages(GetReceivePacket getReceivePacket) {
        if (getReceivePacket == null) {
            return;
        }
        List<ReceivePackage> receivePackages = getReceivePacket.revpackageList;
        if (receivePackages == null || receivePackages.size() <= 0) {
            return;
        }
        List<ReceivePackageGD> receivePackageGDs = mDataMapper.transformToRevPacketsDB(receivePackages);
        mLocalStorage.putReceivePackages(receivePackageGDs);
    }

    @Override
    public Observable<List<PackageInBundle>> getPackageInBundleList(long bundleID, long timestamp, int count, int order) {
        return mRequestService.getPackageInBundleList(bundleID, timestamp, count, order, user.uid, user.accesstoken)
                .map(mDataMapper::transformToPackageInBundle)
                .doOnNext(this::insertPackageInBundle);
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
        mRequestService.getPackageInBundleList(bundleId, timestamp, count, sortOrder, user.uid, user.accesstoken)
                .map(mDataMapper::transformToPackageInBundle)
                .doOnNext(this::insertPackageInBundle)
                .doOnNext(packageInBundles -> {
                    subscriber.onNext(true);

                    if (packageInBundles != null && packageInBundles.size() >= count) {
                        long newTimeStamp = packageInBundles.get(packageInBundles.size() - 1).openTime;
                        getPackageInBundlesServer(bundleId, newTimeStamp, count, sortOrder, subscriber);
                    } else {
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
                        mLocalStorage.getPackageInBundle(bundleId).subscribe(subscriber);
                    }).subscribe(new DefaultSubscriber<>());
                }
            });
        } else {
            return mLocalStorage.getPackageInBundle(bundleId);
        }
    }

    private boolean shouldUpdatePacketsForBundle(long bundleId) {
        Long lastOpenTime = mLocalStorage.getLastOpenTimeForPacketsInBundle(bundleId);

        if (lastOpenTime == null) {
            Timber.d("Last open time is NULL");
            return true;
        }

        Timber.d("Last open time: %s, %s, %s", lastOpenTime, System.currentTimeMillis(), System.currentTimeMillis() - lastOpenTime);
        return lastOpenTime + 1000 * 60 * 60 * 24 > System.currentTimeMillis();

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
    public Observable<GetSentBundle> getSendBundleSummary() {
        Timber.d("getSendBundleSummary");
        if (shouldGetSentBundleSummaryServer()) {
            Timber.d("getSendBundleSummary -> getSentBundleSummaryServer");
            return getSentBundleSummaryServer();
        } else {
            Timber.d("getSendBundleSummary -> getSentBundleSummary");
            return mLocalStorage.getSentBundleSummary();
        }
    }

    private Observable<GetSentBundle> getSentBundleSummaryServer() {
        return mRequestService.getSentBundleList(0, LIMIT_ITEMS_PER_REQ, ORDER, user.uid, user.accesstoken)
                .map(mDataMapper::transformToSentBundleSummary)
                .doOnNext(this::insertSentBundleSummary)
                .doOnNext(this::insertSentBundles);
    }

    private boolean shouldGetSentBundleSummaryServer() {
        return !mLocalStorage.isHaveSentBundleSunmmaryInDb();
    }

    @Override
    public Observable<GetReceivePacket> getReceiveBundleSummary() {
        Timber.d("getReceiveBundleSummary");
        if (shouldGetRevPacketSummaryServer()) {
            Timber.d("getReceiveBundleSummary -> getRevPacketSummaryServer");
            return getRevPacketSummaryServer();
        } else {
            Timber.d("getReceiveBundleSummary -> getReceivePacketSummary");
            return mLocalStorage.getReceivePacketSummary();
        }
    }

    private Observable<GetReceivePacket> getRevPacketSummaryServer() {
        return mRequestService.getReceivedPackageList(0, LIMIT_ITEMS_PER_REQ, ORDER, user.uid, user.accesstoken)
                .map(mDataMapper::transformToGetRevPacket)
                .doOnNext(this::insertRevPacketSummary)
                .doOnNext(this::insertReceivePackages);
    }

    private void insertRevPacketSummary(GetReceivePacket getReceivePacket) {
        if (getReceivePacket == null) {
            return;
        }
        mLocalStorage.putReceivePacketSummary(new ReceivePacketSummaryDB(null, getReceivePacket.totalofrevamount,
                getReceivePacket.totalofrevpackage, getReceivePacket.numofluckiestdraw,
                System.currentTimeMillis()));
    }

    private boolean shouldGetRevPacketSummaryServer() {
        return !mLocalStorage.isHaveRevPacketSunmmaryInDb();
    }

    @Override
    public Observable<ReceivePackage> getReceivedPacket(long packetId) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.getReceivedPacket(packetId));
    }

    @Override
    public Observable<Boolean> getAllSentBundlesServer() {
        Timber.d("getAllSentBundlesServer");
        int timestamp = 0;
        int count = LIMIT_ITEMS_PER_REQ;
        int sortOrder = ORDER;
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Timber.d("getAllSentBundlesServer call");
                getSentBundleServer(timestamp, count, sortOrder, subscriber);
            }
        });
    }

    @Override
    public Observable<GetSentBundle> getSentBundleListServer(long timestamp, int count, int order) {
        return mRequestService.getSentBundleList(timestamp, count, order, user.uid, user.accesstoken)
                .map(mDataMapper::transformToSentBundleSummary)
                .doOnNext(this::insertSentBundleSummary)
//                .map(mDataMapper::transformToSentBundles)
                .doOnNext(this::insertSentBundles);
    }

    private void insertSentBundleSummary(GetSentBundle getSentBundle) {
        if (getSentBundle == null) {
            return;
        }
        mLocalStorage.putSentBundleSummary(new SentBundleSummaryDB(null, getSentBundle.totalofsentamount,
                getSentBundle.totalofsentbundle,
                System.currentTimeMillis()));
    }

    @Override
    public Observable<GetSentBundle> getSentBundleList(final long timeCreate, final int count) {
        Timber.d("getSentBundleList timeCreate [%s] count [%s]", String.valueOf(timeCreate), String.valueOf(count));
        if (shouldGetSentBundleFromServer(timeCreate, count)) {
            Timber.d("getSentBundleList -> getSentBundleListServer");
            //return getSentBundleListServer(timeCreate, count, ORDER);
            return Observable.create(new Observable.OnSubscribe<GetSentBundle>() {
                @Override
                public void call(Subscriber<? super GetSentBundle> subscriber) {
                    Timber.d("Begin to fetch SentBundleList from server for timeCreate [%s] count [%s]", timeCreate, count);
                    getSentBundleListServer(timeCreate, count, ORDER).subscribeOn(Schedulers.io()).doOnCompleted(() -> {
                        Timber.d("Finished fetching SentBundleList for timeCreate [%s] count [%s]", timeCreate, count);
                        Observable<List<SentBundle>> obSentBundles = mLocalStorage.getSentBundle(timeCreate, count);
                        Observable<GetSentBundle> obGetSentBundle = mLocalStorage.getSentBundleSummary();
                        Observable.zip(obSentBundles, obGetSentBundle, new Func2<List, GetSentBundle, GetSentBundle>() {

                            @Override
                            public GetSentBundle call(List sentBundles, GetSentBundle getSentBundle) {
                                long totalOfSentAmount = 0;
                                int totalOfSentBundle = 0;
                                if (getSentBundle != null) {
                                    totalOfSentAmount = getSentBundle.totalofsentamount;
                                    totalOfSentBundle = getSentBundle.totalofsentbundle;
                                }
                                return new GetSentBundle(totalOfSentAmount, totalOfSentBundle, sentBundles);
                            }
                        }).subscribe(new Observer<GetSentBundle>() {
                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(GetSentBundle getSentBundle) {
                                subscriber.onNext(getSentBundle);
                            }
                        });
                    }).subscribe(new DefaultSubscriber<>());
                }
            });
        } else {
            Timber.d("getSentBundleList -> getSentBundleListDB");
            return Observable.create(new Observable.OnSubscribe<GetSentBundle>() {
                @Override
                public void call(Subscriber<? super GetSentBundle> subscriber) {
                    Timber.d("Finished fetching SentBundleList for timeCreate [%s] count [%s]", timeCreate, count);
                    Observable<List<SentBundle>> obSentBundles = mLocalStorage.getSentBundle(timeCreate, count);
                    Observable<GetSentBundle> obGetSentBundle = mLocalStorage.getSentBundleSummary();
                    Observable.zip(obSentBundles, obGetSentBundle, (sentBundles, getSentBundle) ->
                            new GetSentBundle(getSentBundle.totalofsentamount, getSentBundle.totalofsentbundle, sentBundles))
                            .subscribe(new Observer<GetSentBundle>() {
                                @Override
                                public void onCompleted() {
                                    subscriber.onCompleted();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    subscriber.onError(e);
                                }

                                @Override
                                public void onNext(GetSentBundle getSentBundle) {
                                    subscriber.onNext(getSentBundle);
                                }
                            });
                }
            });
        }
    }

    private boolean shouldGetSentBundleFromServer(long createTime, int count) {
        return !mLocalStorage.isHaveSentBundleInDb(createTime, count);
    }

    private void getSentBundleServer(long timestamp, int count, int sortOrder, Subscriber<? super Boolean> subscriber) {
        Timber.d("getSentBundleServer %s ", timestamp);
        mRequestService.getSentBundleList(timestamp, count, sortOrder, user.uid, user.accesstoken)
                .map(mDataMapper::transformToSentBundleSummary)
                .doOnNext(this::insertSentBundleSummary)
//                .map(mDataMapper::transformToSentBundles)
                .doOnNext(this::insertSentBundles)
                .doOnNext(getSentBundle -> {
                    subscriber.onNext(true);
                    if (getSentBundle == null ||
                            getSentBundle.sentbundlelist == null ||
                            getSentBundle.sentbundlelist.size() < count) {
                        subscriber.onCompleted();
                    } else {
                        List<SentBundle> sentBundles = getSentBundle.sentbundlelist;
                        long newTimeStamp = sentBundles.get(sentBundles.size() - 1).createTime;
                        getSentBundleServer(newTimeStamp, count, sortOrder, subscriber);
                    }
                })
                .doOnError(subscriber::onError)
                .subscribe(new DefaultSubscriber<>());
    }

    private void insertPackageInBundle(List<PackageInBundle> packageInBundles) {
        mLocalStorage.putPackageInBundle(mDataMapper.transformToPackageInBundleGD(packageInBundles));
    }
}
