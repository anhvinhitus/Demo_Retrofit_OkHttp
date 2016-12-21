package vn.com.vng.zalopay.data.redpacket;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit2.http.Field;
import retrofit2.http.Query;
import rx.Observable;
import rx.Observer;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.CustomRobolectricRunner;
import vn.com.vng.zalopay.data.api.entity.UserRPEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.ReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.RedPacketAppInfoResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.redpacket.AppConfigEntity;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;

/**
 * Created by longlv on 14/07/2016.
 * Unit test for RedPackageRepository
 */
@RunWith(CustomRobolectricRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class RedPackageRepositoryTest {
    RedPacketStore.LocalStorage mLocalStorage;
    RedPacketStore.Repository mRepository;
    RedPacketStore.RequestService mRequestService;
    RedPacketStore.RequestTPEService mRequestTPEService;
    RedPacketDataMapper dataMapper;

    //declare Variable
    BundleOrderResponse bundleOrderResponse;
    BaseResponse baseResponse;
    SubmitOpenPackageResponse submitOpenPackageResponse;
    PackageStatusResponse packageStatusResponse;
    SentBundleListResponse sentBundleListResponse;
    RedPacketAppInfo redPacketAppInfo;
    GetReceivePacket getReceivePacket;
    GetReceivePackageResponse getReceivePackageResponse;
    SentPackageInBundleResponse sentPackageInBundleResponse;

    DaoSession mDaoSession;

    private void setupRequestServiceVariable() {
        bundleOrderResponse = new BundleOrderResponse();
        bundleOrderResponse.bundleID = 123321L;
        bundleOrderResponse.appid = 123;
        bundleOrderResponse.zptranstoken = "abc";
        bundleOrderResponse.apptransid = "234";
        bundleOrderResponse.appuser = "1";
        bundleOrderResponse.apptime = 1468485738;
        bundleOrderResponse.embeddata = "embeddata";
        bundleOrderResponse.item = "item1";
        bundleOrderResponse.amount = 300000;
        bundleOrderResponse.description = "description";
        bundleOrderResponse.payoption = "payoption";
        bundleOrderResponse.mac = "mac";

        baseResponse = new BaseResponse();
        baseResponse.err = 0;
        baseResponse.message="";

        submitOpenPackageResponse = new SubmitOpenPackageResponse();
        submitOpenPackageResponse.zptransid = 123456;

        packageStatusResponse = new PackageStatusResponse();
        packageStatusResponse.zptransid = "abc";
        packageStatusResponse.amount = 300000;
        packageStatusResponse.balance = 1;
        packageStatusResponse.data = "data";
        packageStatusResponse.isprocessing = false;
        packageStatusResponse.reqdate = 1245364534;

        sentBundleListResponse = new SentBundleListResponse();
        sentBundleListResponse.totalOfSentAmount = 1000000L;
        sentBundleListResponse.totalOfSentBundle = 5L;
        sentBundleListResponse.bundleResponseList = new ArrayList<>();
        for(int i = 0; i < 20; i++) {
            SentBundleResponse sentBundleResponse = new SentBundleResponse();
            sentBundleResponse.bundleid = 123;
            sentBundleResponse.createtime = 100L + Long.valueOf(i);
            sentBundleResponse.lastopentime = 14351235L + Long.valueOf(i);
            sentBundleResponse.numofopenedpakages = 3;
            sentBundleResponse.numofpackages = 5;
            sentBundleResponse.sendmessage = "message";
            sentBundleResponse.sendzalopayid = "abc";
            sentBundleResponse.totalluck = 5L;
            sentBundleResponse.type = 1;
            sentBundleListResponse.bundleResponseList.add(sentBundleResponse);
        }

        redPacketAppInfo = new RedPacketAppInfo();
        redPacketAppInfo.isUpdateAppInfo = false;
        redPacketAppInfo.expiredTime = 12421352L;
        redPacketAppInfo.checksum = "abc";
        redPacketAppInfo.appConfigEntity = new AppConfigEntity();
        redPacketAppInfo.appConfigEntity.bundleExpiredTime = 12348613L;
        redPacketAppInfo.appConfigEntity.maxAmountPerPackage = 10000L;
        redPacketAppInfo.appConfigEntity.maxCountHist = 3;
        redPacketAppInfo.appConfigEntity.maxMessageLength = 51513576L;
        redPacketAppInfo.appConfigEntity.maxPackageQuantity = 10L;
        redPacketAppInfo.appConfigEntity.maxTotalAmountPerBundle = 2000000L;
        redPacketAppInfo.appConfigEntity.minAmountEach = 10000L;
        redPacketAppInfo.appConfigEntity.minDivideAmount = 10000L;

        getReceivePacket = new GetReceivePacket();
        getReceivePacket.numofluckiestdraw = 10L;
        getReceivePacket.totalofrevamount = 300000L;
        getReceivePacket.totalofrevpackage = 3L;
        getReceivePacket.revpackageList = new ArrayList<>();
        for(int i = 0; i < 20; i++) {
            ReceivePackage receivePackage = new ReceivePackage();
            receivePackage.amount = 100000;
            receivePackage.bundleID = 123 + i;
            receivePackage.createTime = 100L + Long.valueOf(i);
            receivePackage.isLuckiest = 0;
            receivePackage.message = "message";
            receivePackage.openedTime = 14351235L;
            receivePackage.packageID = 100 + i;
            receivePackage.revZaloPayID = "receiver";
            receivePackage.senderAvatar = "avaSender";
            receivePackage.senderFullName = "nameSender";
            receivePackage.senderZaloPayID = "sender";
            receivePackage.status = 1;
            getReceivePacket.revpackageList.add(receivePackage);
        }

        getReceivePackageResponse = new GetReceivePackageResponse();
        getReceivePackageResponse.totalOfRevAmount = 300000L;
        getReceivePackageResponse.totalOfRevPackage = 3L;
        getReceivePackageResponse.numOfLuckiestDraw = 2L;
        getReceivePackageResponse.receivePackageResponses = new ArrayList<>();
        for(int i = 0; i < 20; i++) {
            ReceivePackageResponse receivePackageResponse = new ReceivePackageResponse();
            receivePackageResponse.amount = 100000;
            receivePackageResponse.bundleid = 123 + i;
            receivePackageResponse.createtime = 100L + Long.valueOf(i);
            receivePackageResponse.isluckiest = 0;
            receivePackageResponse.message = "message";
            receivePackageResponse.openedtime = 14351235L;
            receivePackageResponse.packageid = 100 + i;
            receivePackageResponse.revzalopayid = "receiver";
            receivePackageResponse.avatarofsender = "avaSender";
            receivePackageResponse.sendfullname = "nameSender";
            receivePackageResponse.sendzalopayid = "sender";
            getReceivePackageResponse.receivePackageResponses.add(receivePackageResponse);
        }

        sentPackageInBundleResponse = new SentPackageInBundleResponse();
        sentPackageInBundleResponse.packageResponses = new ArrayList<>();
        for(int i = 0; i < 20; i++) {
            PackageInBundleResponse packageInBundleResponse = new PackageInBundleResponse();
            packageInBundleResponse.amount = 100000;
            packageInBundleResponse.bundleid = 123 + i;
            packageInBundleResponse.isluckiest = false;
            packageInBundleResponse.message = "message";
            packageInBundleResponse.opentime = 14351235L;
            packageInBundleResponse.packageid = 100 + i;
            packageInBundleResponse.revzalopayid = "receiver";
            packageInBundleResponse.revavatarurl = "avaSender";
            packageInBundleResponse.revfullname = "nameSender";
            packageInBundleResponse.sendmessage = "sendmessage";
            sentPackageInBundleResponse.packageResponses.add(packageInBundleResponse);
        }
    }

    @Before
    public void setup() throws Exception {
        setupRequestServiceVariable();

        mRequestService = new RequestServiceImpl();
        mRequestTPEService = new RequestTPEServiceImpl();

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        mDaoSession = new DaoMaster(db).newSession();
        dataMapper = new RedPacketDataMapper();
        mLocalStorage = new RedPacketLocalStorage(mDaoSession, dataMapper);

        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, dataMapper, new User("1"), 1, new Gson());
    }

    public class RequestTPEServiceImpl implements RedPacketStore.RequestTPEService {

        @Override
        public Observable<PackageStatusResponse> getPackageStatus(@Query("appid") int appId, @Query("packageid") long packageID, @Query("zptransid") long zpTransID, @Query("userid") String userid, @Query("accesstoken") String accessToken, @Query("deviceid") String deviceid) {
            return Observable.just(packageStatusResponse);
        }
    }

    public class RequestServiceImpl implements RedPacketStore.RequestService {

        @Override
        public Observable<BundleOrderResponse> createBundleOrder(@Field("quantity") int quantity, @Field("totalLuck") long totalLuck, @Field("amountEach") long amountEach, @Field("type") int type, @Field("sendZaloPayID") String sendZaloPayID, @Field("accessToken") String accessToken, @Field("sendMessage") String sendMessage) {
            return Observable.just(bundleOrderResponse);
        }

        @Override
        public Observable<BaseResponse> sendBundle(@Field("bundleID") long bundleID, @Field("friendList") String friendList, @Field("sendZaloPayID") String sendZaloPayID, @Field("accessToken") String accessToken) {
            return Observable.just(baseResponse);
        }

        @Override
        public Observable<SubmitOpenPackageResponse> submitOpenPackage(@Field("packageID") long packageID, @Field("bundleID") long bundleID, @Field("revZaloPayID") String revZaloPayID, @Field("accessToken") String accessToken) {
            return Observable.just(submitOpenPackageResponse);
        }

        @Override
        public Observable<SentBundleListResponse> getSentBundleList(@Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zaloPayID") String zaloPayID, @Field("accessToken") String accessToken) {
            return Observable.just(sentBundleListResponse);
        }

        @Override
        public Observable<GetReceivePackageResponse> getReceivedPackageList(@Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zalopayid") String zalopayid, @Field("accesstoken") String accesstoken) {
            return Observable.just(getReceivePackageResponse);
        }

        @Override
        public Observable<SentPackageInBundleResponse> getPackageInBundleList(@Field("bundleID") long bundleID, @Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zaloPayID") String zaloPayID, @Field("accessToken") String accessToken) {
            return Observable.just(sentPackageInBundleResponse);
        }

        @Override
        public Observable<RedPacketAppInfoResponse> getAppInfo(@Query("checksum") String checksum, @Query("userid") String zalopayid, @Query("accesstoken") String accesstoken) {
            return null;
        }

        @Override
        public Observable<BaseResponse> submittosendbundlebyzalopayinfo(@Field("bundleid") long bundleID, @Field("zalopayoffriendlist") String friends, @Field("zalopayofsender") String sender, @Field("accesstoken") String accessToken) {
            return Observable.just(baseResponse);
        }
    }

    @Test
    public void createBundleOrder() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<BundleOrder> bundleOrderSource = new ArrayList<>();
        final List<BundleOrder> bundleOrders = new ArrayList<>();

        int quantity = 1;
        int totalLuck = 12;
        long amountEach = 20000;
        int type = 1;
        String message = "Chuc may man lan sau";

        mRepository.createBundleOrder(quantity, totalLuck, amountEach, type, message).subscribe(new Observer<BundleOrder>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(BundleOrder bundleOrder) {
                System.out.println("Got onNext");
                bundleOrders.add(bundleOrder);
                countDownLatch.countDown();
            }
        });

        BundleOrder bundleOrder = new BundleOrder(bundleOrderResponse.getAppid(), bundleOrderResponse.getZptranstoken(),
                bundleOrderResponse.apptransid, bundleOrderResponse.appuser, bundleOrderResponse.apptime,
                bundleOrderResponse.embeddata, bundleOrderResponse.item, bundleOrderResponse.amount,
                bundleOrderResponse.description, bundleOrderResponse.payoption, bundleOrderResponse.mac,
                bundleOrderResponse.bundleID);
        bundleOrderSource.add(bundleOrder);

        Assert.assertTrue("createBundleOrder", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("createBundleOrder", bundleOrderSource, bundleOrders);
    }

    @Test
    public void sendBundle() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<Boolean> result = new ArrayList<>();
        List<UserRPEntity> entities = new ArrayList<UserRPEntity>();

        UserRPEntity entity = new UserRPEntity();
        entity.avatar = "ava";
        entity.zaloID = "zaloId";
        entity.zaloName = "name";
        entity.zaloPayID = "id";

        for(int i = 0; i < 5; i++) {
            entities.add(entity);
        }

        mRepository.sendBundle(123321L, entities).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                System.out.println("Got onNext");
                result.add(aBoolean);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("sendBundle", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("sendBundle", true, result.get(0));
    }

    @Test
    public void sendBundleWithUndefinedBundleId() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<Boolean> result = new ArrayList<>();
        List<UserRPEntity> entities = new ArrayList<UserRPEntity>();

        UserRPEntity entity = new UserRPEntity();
        entity.avatar = "ava";
        entity.zaloID = "zaloId";
        entity.zaloName = "name";
        entity.zaloPayID = "id";

        for(int i = 0; i < 5; i++) {
            entities.add(entity);
        }

        mRepository.sendBundle(1L, entities).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                System.out.println("Got onNext");
                result.add(aBoolean);
                countDownLatch.countDown();
            }
        });

        Assert.assertFalse("sendBundle with undefined bundleId", countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void sendBundleWithNullEntities() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<Boolean> result = new ArrayList<>();

        mRepository.sendBundle(123321L, null).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                System.out.println("Got onNext");
                result.add(aBoolean);
                countDownLatch.countDown();
            }
        });

        Assert.assertFalse("sendBundle with null entities", countDownLatch.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void submitOpenPackage() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<SubmitOpenPackage> result = new ArrayList<>();

        mRepository.submitOpenPackage(1L, 2L).subscribe(new Observer<SubmitOpenPackage>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(SubmitOpenPackage submitOpenPackage) {
                System.out.println("Got onNext");
                result.add(submitOpenPackage);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("submitOpenPackage", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("submitOpenPackage", submitOpenPackageResponse.zptransid, result.get(0).zpTransID);
    }

    @Test
    public void getpackagestatus() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<PackageStatus> result = new ArrayList<>();

        mRepository.getpackagestatus(1L, 2L, "abcd").subscribe(new Observer<PackageStatus>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(PackageStatus packageStatus) {
                System.out.println("Got onNext");
                result.add(packageStatus);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS));
        assertEquals(packageStatusResponse, result.get(0));
    }

    // setBundleStatus

    @Test
    public void getSentBundleListServer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<GetSentBundle> getSentBundles = new ArrayList<>();

        mRepository.getSentBundleListServer(110, 3, -1).subscribe(new Observer<GetSentBundle>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(GetSentBundle getSentBundle) {
                System.out.println("Got onNext");
                getSentBundles.add(getSentBundle);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getSentBundleListServer", countDownLatch.await(2, TimeUnit.SECONDS));
        assertEquals(sentBundleListResponse, getSentBundles.get(0), 10);
    }

    @Test
    public void getSentBundleList() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<GetSentBundle> getSentBundles = new ArrayList<>();

        mRepository.getSentBundleList(110, 3).subscribe(new Observer<GetSentBundle>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(GetSentBundle getSentBundle) {
                System.out.println("Got onNext");
                getSentBundles.add(getSentBundle);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getSentBundleListServer", countDownLatch.await(2, TimeUnit.SECONDS));
        assertEquals(sentBundleListResponse, getSentBundles.get(0), 10);
    }

    @Test
    public void getReceivedPackagesServer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<GetReceivePacket> getReceivePackets = new ArrayList<>();

        mRepository.getReceivedPackagesServer(110, 3, -1).subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                System.out.println("Got onNext");
                getReceivePackets.add(getReceivePacket);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getSentBundleListServer", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("getSentBundleListServer", getReceivePacket, getReceivePackets.get(0));
    }

    @Test
    public void getReceivePacketList() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<GetReceivePacket> getReceivePackets = new ArrayList<>();

        mRepository.getReceivePacketList(110, 3).subscribe(new Observer<GetReceivePacket>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(GetReceivePacket getReceivePacket) {
                System.out.println("Got onNext");
                getReceivePackets.add(getReceivePacket);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getSentBundleListServer", countDownLatch.await(2, TimeUnit.SECONDS));
        assertEquals(getReceivePackageResponse, getReceivePackets.get(0), 10);
    }

    @Test
    public void addReceivedRedPacket() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<ReceivePackage> receivePackages = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        mRepository.getReceivedPacket(1).subscribe(new Observer<ReceivePackage>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(ReceivePackage receivePackage) {
                System.out.println("Got onNext");
                receivePackages.add(receivePackage);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getReceivedPacket", countDownLatch.await(2, TimeUnit.SECONDS));

        Assert.assertEquals("packageID", 1, receivePackages.get(0).packageID);
        Assert.assertEquals("bundleID", 3L, receivePackages.get(0).bundleID);
        Assert.assertEquals("senderFullName", "name", receivePackages.get(0).senderFullName);
        Assert.assertEquals("senderAvatar", "ava", receivePackages.get(0).senderAvatar);
        Assert.assertEquals("message", "mess", receivePackages.get(0).message);
    }

    @Test
    public void addReceivedRedPacketWhenAlreadyHave() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<ReceivePackage> receivePackages = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        mRepository.addReceivedRedPacket(1, 3, "name1", "ava1", "mess1");
        mRepository.getReceivedPacket(1).subscribe(new Observer<ReceivePackage>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(ReceivePackage receivePackage) {
                System.out.println("Got onNext");
                receivePackages.add(receivePackage);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getReceivedPacket", countDownLatch.await(2, TimeUnit.SECONDS));

        Assert.assertEquals("packageID", 1, receivePackages.get(0).packageID);
        Assert.assertEquals("bundleID", 3L, receivePackages.get(0).bundleID);
        Assert.assertEquals("senderFullName", "name1", receivePackages.get(0).senderFullName);
        Assert.assertEquals("senderAvatar", "ava1", receivePackages.get(0).senderAvatar);
        Assert.assertEquals("message", "mess1", receivePackages.get(0).message);
    }

    @Test
    public void getReceivedPacketWhenNotHavingDatas() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<ReceivePackage> receivePackages = new ArrayList<>();

        mRepository.getReceivedPacket(1).subscribe(new Observer<ReceivePackage>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(ReceivePackage receivePackage) {
                System.out.println("Got onNext");
                receivePackages.add(receivePackage);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getReceivedPacket when not having datas", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("getReceivedPacket when not having datas", 0, receivePackages.size());
    }

    @Test
    public void getReceivedPacket() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<ReceivePackage> receivePackages = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        mRepository.getReceivedPacket(1).subscribe(new Observer<ReceivePackage>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(ReceivePackage receivePackage) {
                System.out.println("Got onNext");
                receivePackages.add(receivePackage);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getReceivedPacket", countDownLatch.await(2, TimeUnit.SECONDS));

        Assert.assertEquals("packageID", 1, receivePackages.get(0).packageID);
        Assert.assertEquals("bundleID", 1, receivePackages.get(0).bundleID);
        Assert.assertEquals("senderFullName", "name", receivePackages.get(0).senderFullName);
        Assert.assertEquals("senderAvatar", "ava", receivePackages.get(0).senderAvatar);
        Assert.assertEquals("message", "mess", receivePackages.get(0).message);
    }

    @Test
    public void getAllPacketInBundleServer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<Boolean> results = new ArrayList<>();

        mRepository.getAllPacketInBundleServer(125).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                System.out.println("Got onNext");
                results.add(aBoolean);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getAllPacketInBundleServer", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("getAllPacketInBundleServer", true, results.get(0));
    }

    @Test
    public void getAllPacketInBundleServer1() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<Boolean> results = new ArrayList<>();

        mRepository.getAllPacketInBundleServer(100).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                System.out.println("Got onNext");
                results.add(aBoolean);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getAllPacketInBundleServer", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("getAllPacketInBundleServer", false, results.get(0));
    }

    // getPacketsInBundle

    @Test
    public void setPacketStatusWhenNotHavingPacket() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        mRepository.setPacketStatus(1, 3, 1, "message");
        mRepository.getPacketStatus("1").subscribe(new Observer<ReceivePackageGD>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(ReceivePackageGD receivePackageGD) {
                System.out.println("Got onNext");
                receivePackageGDs.add(receivePackageGD);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("setPacketStatus when not having datas", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("setPacketStatus when not having packet", null, receivePackageGDs.get(0));
    }

    @Test
    public void setPacketStatus() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        mRepository.setPacketStatus(1, 3, 1, "message");
        mRepository.getPacketStatus("1").subscribe(new Observer<ReceivePackageGD>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(ReceivePackageGD receivePackageGD) {
                System.out.println("Got onNext");
                receivePackageGDs.add(receivePackageGD);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getReceivedPacket", countDownLatch.await(2, TimeUnit.SECONDS));

        Assert.assertEquals("id", 1, receivePackageGDs.get(0).id);
        Assert.assertEquals("amount", (Long) 3L, receivePackageGDs.get(0).amount);
        Assert.assertEquals("status", (Long) 1L, receivePackageGDs.get(0).status);
        Assert.assertEquals("message", "message", receivePackageGDs.get(0).messageStatus);
    }

    @Test
    public void getPacketStatusWhenNotHavingPacket() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        mRepository.getPacketStatus("1").subscribe(new Observer<ReceivePackageGD>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(ReceivePackageGD receivePackageGD) {
                System.out.println("Got onNext");
                receivePackageGDs.add(receivePackageGD);
                countDownLatch.countDown();
            }
        });
        Assert.assertEquals("getPacketStatus when not having packet", null, receivePackageGDs.get(0));
    }

    @Test
    public void getPacketStatusWhenNotSetting() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess");
        mRepository.getPacketStatus("1").subscribe(new Observer<ReceivePackageGD>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(ReceivePackageGD receivePackageGD) {
                System.out.println("Got onNext");
                receivePackageGDs.add(receivePackageGD);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getReceivedPacket", countDownLatch.await(2, TimeUnit.SECONDS));

        Assert.assertEquals("id", 1, receivePackageGDs.get(0).id);
        Assert.assertEquals("amount", null, receivePackageGDs.get(0).amount);
        Assert.assertEquals("status", (Long) 1L, receivePackageGDs.get(0).status);
        Assert.assertEquals("message", null, receivePackageGDs.get(0).messageStatus);
    }

    @Test
    public void getAppInfoServer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<RedPacketAppInfo> redPacketAppInfos = new ArrayList<>();

        mRepository.getAppInfoServer("abc").subscribe(new Observer<RedPacketAppInfo>() {
            @Override
            public void onCompleted() {
                System.out.println("Got completed");
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("Got error: " + e);
                countDownLatch.countDown();
            }

            @Override
            public void onNext(RedPacketAppInfo redPacketAppInfo) {
                System.out.println("Got onNext");
                redPacketAppInfos.add(redPacketAppInfo);
                countDownLatch.countDown();
            }
        });

        Assert.assertTrue("getSentBundleListServer", countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals("getSentBundleListServer", redPacketAppInfo, redPacketAppInfos.get(0));
    }

    // getRedPacketAppInfo

    @Test
    public void testRedPackage() throws Exception {
//        CountDownLatch countDownLatch = new CountDownLatch(6);
//        final List<RedPacket> redPackages = new ArrayList<>();
//        final List<BundleOrder> bundleOrders = new ArrayList<>();

//        mRequestService.sendBundle(bundleOrderResponse.bundleID, "u1|u2|u3", "323242", "adsfsafa")
//        .subscribe(new Observer<BaseResponse>() {
//            @Override
//            public void onCompleted() {
//                countDownLatch.countDown();
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.println("sendBundle Got error: " + e);
//                countDownLatch.countDown();
//            }
//
//            @Override
//            public void onNext(BaseResponse baseResponse) {
//                System.out.println("Got baseResponse: " + String.valueOf(baseResponse));
//                mLocalStorage.updateRedPackage(bundleOrderResponse.bundleID, RedPacket.RedPacketState.SENT.getValue());
//                countDownLatch.countDown();
//            }
//        });

//        mLocalStorage.getAllRedPackage().subscribe(new Observer<List<RedPacket>>() {
//            @Override
//            public void onCompleted() {
//                countDownLatch.countDown();
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.println("getAllRedPackage Got error: " + e);
//                countDownLatch.countDown();
//            }
//
//            @Override
//            public void onNext(List<RedPacket> redPackageList) {
//                for (int i = 0; i < redPackageList.size(); i++) {
//                    System.out.print("bundleId:" + redPackageList.get(i).bundleId);
//                    System.out.print("state:" + redPackageList.get(i).state);
//                    bundleIDs.add(redPackageList.get(i).bundleId);
//                    bundleStates.add(redPackageList.get(i).state);
//                    redPackages.add(redPackageList.get(i));
//                }
//                countDownLatch.countDown();
//            }
//        });

//        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS));
//        Assert.assertEquals(redPackages.get(0).bundleId, bundleOrderResponse.bundleID, 0);
//        Assert.assertEquals(redPackages.get(0).quantity, quantity, 0);
//        Assert.assertEquals(redPackages.get(0).totalLuck, totalLuck, 0);
//        Assert.assertEquals(redPackages.get(0).amountEach, amountEach, 0);
//        Assert.assertEquals(redPackages.get(0).type, type, 0);
//        Assert.assertEquals(redPackages.get(0).sendMessage, message, 0);
    }

    @Test
    public void testDataTransform() {
        RedPacketDataMapper mapper = new RedPacketDataMapper();
        SentBundleGD inputItem = new SentBundleGD();
        inputItem.numOfOpenedPakages = 10L;
        inputItem.numOfPackages = 20L;
        inputItem.totalLuck = 4L;
        inputItem.senderZaloPayID = "1611100000";
        inputItem.type = 1L;
        inputItem.createTime = 1L;
        inputItem.lastOpenTime = 2L;
        inputItem.sendMessage = "message";
        inputItem.status = 1L;
        inputItem.__setDaoSession(mDaoSession);

        SentBundle bundle = new SentBundle();
        bundle.numOfOpenedPakages = 10;
        bundle.numOfPackages = 20;
        bundle.totalLuck = 4;
        bundle.sendZaloPayID = "1611100000";
        bundle.type = 1;
        bundle.createTime = 1L;
        bundle.lastOpenTime = 2L;
        bundle.sendMessage = "message";
        bundle.status = 1;

        SentBundle outputBundle = mapper.transform(inputItem);
        assertEquals(bundle, outputBundle);
    }

    private void assertEquals(SentBundle b1, SentBundle b2) {
        if (b1 == null && b2 == null) {
            return;
        }

        if (b1 == null && b2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("numOfOpenedPakages", b1.numOfOpenedPakages, b2.numOfOpenedPakages);
        Assert.assertEquals("numOfPackages", b1.numOfPackages, b2.numOfPackages);
        Assert.assertEquals("totalLuck", b1.totalLuck, b2.totalLuck);
        Assert.assertEquals("sendZaloPayID", b1.sendZaloPayID, b2.sendZaloPayID);
        Assert.assertEquals("type", b1.type, b2.type);
        Assert.assertEquals("createTime", b1.createTime, b2.createTime);
        Assert.assertEquals("lastOpenTime", b1.lastOpenTime, b2.lastOpenTime);
        Assert.assertEquals("sendMessage", b1.sendMessage, b2.sendMessage);
        Assert.assertEquals("status", b1.status, b2.status);
    }

    @Test
    public void testGetSentBundleSummary() throws Exception {
//        CountDownLatch countDownLatch = new CountDownLatch(1);
        SentBundleGD inputItem = new SentBundleGD();
        inputItem.numOfOpenedPakages = 10L;
        inputItem.numOfPackages = 20L;
        inputItem.totalLuck = 4L;
        inputItem.senderZaloPayID = "1611100000";
        inputItem.type = 1L;
        inputItem.createTime = 1L;
        inputItem.lastOpenTime = 2L;
        inputItem.sendMessage = "message";
        inputItem.status = 1L;

        List<SentBundleGD> list = new ArrayList<>();
        list.add(inputItem);
        mLocalStorage.putSentBundle(list);
        List<SentBundle> inputList = new ArrayList<>();
        RedPacketDataMapper mapper = new RedPacketDataMapper();
        inputList.add(mapper.transform(inputItem));

        System.out.println("Begin to get sent bundle");
        mLocalStorage.getSentBundle(0L, 2).subscribe(new DefaultSubscriber<List<SentBundle>>() {
            @Override
            public void onNext(List<SentBundle> sentBundles) {
                super.onNext(sentBundles);
                System.out.println("Got onNext");
                System.out.println(String.format("Got %d item: ", sentBundles.size()));
//                Assert.assertArrayEquals(sentBundles.toArray(), inputList.toArray());
                SentBundle bundle = sentBundles.get(0);
                Assert.assertEquals(bundle.sendMessage, "message");
                assertEquals(mapper.transform(inputItem), bundle);
                System.out.println("Completed onNext");

//                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                System.out.println(e.getMessage());
                e.printStackTrace();

                Assert.fail(e.getMessage());
            }
        });

        System.out.println("Wait for completion");
//        Assert.assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));
    }

    private void assertEquals(PackageStatusResponse p2, PackageStatus p1) {
        if (p1 == null && p2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (p1 != null && p2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("zpTransID", p1.zpTransID, p2.zptransid);
        Assert.assertEquals("data", p1.data, p2.data);
        Assert.assertEquals("amount", p1.amount, p2.amount);
        Assert.assertEquals("balance", p1.balance, p2.balance);
        Assert.assertEquals("isProcessing", p1.isProcessing, p2.isprocessing);
        Assert.assertEquals("reqdate", p1.reqdate, p2.reqdate);
    }

    private void assertEquals(SentBundleListResponse p1, GetSentBundle p2, int indexOfp1) {
        if (p1 == null && p2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (p1 != null && p2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("totalOfSentAmount", p1.totalOfSentAmount, p2.totalofsentamount);
        Assert.assertEquals("totalOfSentBundle", p1.totalOfSentBundle, p2.totalofsentbundle);
        if (p1.bundleResponseList.size() == 0 && p2.sentbundlelist.size()!= 0) {
            Assert.fail("Compare null and non-null bundleResponseList");
            return;
        }

        if (p1.bundleResponseList.size() != 0 && p2.sentbundlelist.size() == 0) {
            Assert.fail("Compare null and non-null bundleResponseList");
            return;
        }
        for(int i = 0; i < p2.sentbundlelist.size(); i++) {
            System.out.print(p1.bundleResponseList.get(indexOfp1).createtime + " - " + p2.sentbundlelist.get(i).createTime + "\n");
            Assert.assertEquals("bundleid", p1.bundleResponseList.get(indexOfp1).bundleid,
                    p2.sentbundlelist.get(i).bundleID);
            Assert.assertEquals("createtime", p1.bundleResponseList.get(indexOfp1).createtime,
                    p2.sentbundlelist.get(i).createTime);
            Assert.assertEquals("lastopentime", p1.bundleResponseList.get(indexOfp1).lastopentime,
                    p2.sentbundlelist.get(i).lastOpenTime);
            Assert.assertEquals("numofopenedpakages", p1.bundleResponseList.get(indexOfp1).numofopenedpakages,
                    p2.sentbundlelist.get(i).numOfOpenedPakages);
            Assert.assertEquals("numofpackages", p1.bundleResponseList.get(indexOfp1).numofpackages,
                    p2.sentbundlelist.get(i).numOfPackages);
            Assert.assertEquals("sendmessage", p1.bundleResponseList.get(indexOfp1).sendmessage,
                    p2.sentbundlelist.get(i).sendMessage);
            Assert.assertEquals("sendzalopayid", p1.bundleResponseList.get(indexOfp1).sendzalopayid,
                    p2.sentbundlelist.get(i).sendZaloPayID);
            Assert.assertEquals("totalluck", p1.bundleResponseList.get(indexOfp1).totalluck,
                    p2.sentbundlelist.get(i).totalLuck);
            Assert.assertEquals("type", p1.bundleResponseList.get(indexOfp1).type,
                    p2.sentbundlelist.get(i).type);
            indexOfp1++;
        }
    }

    private void assertEquals(GetReceivePackageResponse p1, GetReceivePacket p2, int indexOfp1) {
        if (p1 == null && p2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (p1 != null && p2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("totalOfSentAmount", p1.numOfLuckiestDraw, p2.numofluckiestdraw);
        Assert.assertEquals("totalOfSentBundle", p1.totalOfRevAmount, p2.totalofrevamount);
        Assert.assertEquals("totalOfSentBundle", p1.totalOfRevPackage, p2.totalofrevpackage);
        if (p1.receivePackageResponses.size() == 0 && p2.revpackageList.size()!= 0) {
            Assert.fail("Compare null and non-null bundleResponseList");
            return;
        }

        if (p1.receivePackageResponses.size() != 0 && p2.revpackageList.size() == 0) {
            Assert.fail("Compare null and non-null bundleResponseList");
            return;
        }
        for(int i = 0; i < p2.revpackageList.size(); i++) {
            System.out.print(p1.receivePackageResponses.get(indexOfp1).createtime + " - " + p2.revpackageList.get(i).createTime + "\n");
            Assert.assertEquals("amount", p1.receivePackageResponses.get(indexOfp1).amount,
                    p2.revpackageList.get(i).amount);
            Assert.assertEquals("bundleid", p1.receivePackageResponses.get(indexOfp1).bundleid,
                    p2.revpackageList.get(i).bundleID);
            Assert.assertEquals("createtime", p1.receivePackageResponses.get(indexOfp1).createtime,
                    p2.revpackageList.get(i).createTime);
            Assert.assertEquals("isluckiest", p1.receivePackageResponses.get(indexOfp1).isluckiest,
                    p2.revpackageList.get(i).isLuckiest);
            Assert.assertEquals("message", p1.receivePackageResponses.get(indexOfp1).message,
                    p2.revpackageList.get(i).message);
            Assert.assertEquals("openedtime", p1.receivePackageResponses.get(indexOfp1).openedtime,
                    p2.revpackageList.get(i).openedTime);
            Assert.assertEquals("packageid", p1.receivePackageResponses.get(indexOfp1).packageid,
                    p2.revpackageList.get(i).packageID);
            Assert.assertEquals("revzalopayid", p1.receivePackageResponses.get(indexOfp1).revzalopayid,
                    p2.revpackageList.get(i).revZaloPayID);
            Assert.assertEquals("avatarofsender", p1.receivePackageResponses.get(indexOfp1).avatarofsender,
                    p2.revpackageList.get(i).senderAvatar);
            Assert.assertEquals("sendfullname", p1.receivePackageResponses.get(indexOfp1).sendfullname,
                    p2.revpackageList.get(i).senderFullName);
            Assert.assertEquals("revzalopayid", p1.receivePackageResponses.get(indexOfp1).revzalopayid,
                    p2.revpackageList.get(i).senderZaloPayID);
            indexOfp1++;
        }
    }
}
