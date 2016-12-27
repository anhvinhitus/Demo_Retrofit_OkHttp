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

import retrofit2.http.Field;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.CustomObserver;
import vn.com.vng.zalopay.data.CustomRobolectricRunner;
import vn.com.vng.zalopay.data.DefaultObserver;
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
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.domain.model.redpacket.AppConfigEntity;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
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
    User user;

    //declare Variable
    BundleOrderResponse bundleOrderResponse;
    BaseResponse baseResponse;
    SubmitOpenPackageResponse submitOpenPackageResponse;
    PackageStatusResponse packageStatusResponse;
    SentBundleListResponse sentBundleListResponse;
    RedPacketAppInfo redPacketAppInfo;
    GetReceivePackageResponse getReceivePackageResponse;
    SentPackageInBundleResponse sentPackageInBundleResponse;
    RedPacketAppInfoResponse redPacketAppInfoResponse;

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
        baseResponse.err = 1;
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
            sentBundleResponse.bundleid = 123 + i;
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
            packageInBundleResponse.opentime = 23152432L;
            packageInBundleResponse.packageid = 100 + i;
            packageInBundleResponse.revzalopayid = "receiver";
            packageInBundleResponse.revavatarurl = "avaSender";
            packageInBundleResponse.revfullname = "nameSender";
            packageInBundleResponse.sendmessage = "sendmessage";
            sentPackageInBundleResponse.packageResponses.add(packageInBundleResponse);
        }

        redPacketAppInfoResponse = new RedPacketAppInfoResponse();
        redPacketAppInfoResponse.isUpdateAppInfo = false;
        redPacketAppInfoResponse.expiredTime = 23152432L;
        redPacketAppInfoResponse.checksum = "checksum";
        redPacketAppInfoResponse.appConfigResponse = redPacketAppInfoResponse.new AppConfigResponse();
        redPacketAppInfoResponse.appConfigResponse.bundleExpiredTime = 12348613L;
        redPacketAppInfoResponse.appConfigResponse.maxAmountPerPackage = 10000L;
        redPacketAppInfoResponse.appConfigResponse.maxCountHist = 3;
        redPacketAppInfoResponse.appConfigResponse.maxMessageLength = 51513576L;
        redPacketAppInfoResponse.appConfigResponse.maxPackageQuantity = 10L;
        redPacketAppInfoResponse.appConfigResponse.maxTotalAmountPerBundle = 2000000L;
        redPacketAppInfoResponse.appConfigResponse.minAmountEach = 10000L;
        redPacketAppInfoResponse.appConfigResponse.minDivideAmount = 10000L;
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

        user = new User("zaloPayID");
        user.zaloId = 123;
        user.displayName = "name";
        user.avatar = "avatar";

        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, dataMapper, user, 1, new Gson());
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
            return Observable.just(redPacketAppInfoResponse);
        }

        @Override
        public Observable<BaseResponse> submittosendbundlebyzalopayinfo(@Field("bundleid") long bundleID, @Field("zalopayoffriendlist") String friends, @Field("zalopayofsender") String sender, @Field("accesstoken") String accessToken) {
            return Observable.just(baseResponse);
        }
    }

    @Test
    public void createBundleOrder() {
        final List<BundleOrder> bundleOrderSource = new ArrayList<>();
        final List<BundleOrder> bundleOrders = new ArrayList<>();

        int quantity = 1;
        int totalLuck = 12;
        long amountEach = 20000;
        int type = 1;
        String message = "Chuc may man lan sau";

        mRepository.createBundleOrder(quantity, totalLuck, amountEach, type, message)
                .subscribe(new CustomObserver<>(bundleOrders));

        BundleOrder bundleOrder = new BundleOrder(bundleOrderResponse.getAppid(), bundleOrderResponse.getZptranstoken(),
                bundleOrderResponse.apptransid, bundleOrderResponse.appuser, bundleOrderResponse.apptime,
                bundleOrderResponse.embeddata, bundleOrderResponse.item, bundleOrderResponse.amount,
                bundleOrderResponse.description, bundleOrderResponse.payoption, bundleOrderResponse.mac,
                bundleOrderResponse.bundleID);
        bundleOrderSource.add(bundleOrder);

        Assert.assertEquals("createBundleOrder", bundleOrderSource, bundleOrders);
    }

    @Test
    public void sendBundle() {
        final List<Boolean> result = new ArrayList<>();

        long bundleId = 123321L;
        List<UserRPEntity> entities = new ArrayList<>();
        UserRPEntity entity = new UserRPEntity();
        entity.avatar = "ava";
        entity.zaloID = "zaloId";
        entity.zaloName = "name";
        entity.zaloPayID = "id";
        for(int i = 0; i < 5; i++) {
            entities.add(entity);
        }

        mRepository.sendBundle(bundleId, entities).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("sendBundle", true, result.get(0));
    }

    @Test
    public void submitOpenPackage() {
        final List<SubmitOpenPackage> result = new ArrayList<>();

        mRepository.submitOpenPackage(1L, 2L).subscribe(new CustomObserver<>(result));

        Assert.assertEquals("submitOpenPackage", submitOpenPackageResponse.zptransid, result.get(0).zpTransID);
    }

    @Test
    public void getpackagestatus() {
        final List<PackageStatus> result = new ArrayList<>();

        mRepository.getpackagestatus(1L, 2L, "abcd").subscribe(new CustomObserver<>(result));

        assertEquals(packageStatusResponse, result.get(0));
    }

    @Test
    public void setBundleStatusWithUndefinedBundleId() {
        List<GetSentBundle> result = new ArrayList<>();

        List<SentBundleGD> sendBundleGDs = new ArrayList<>();
        SentBundleGD sentBundleGD = new SentBundleGD();
        sentBundleGD.id = 1L;
        sentBundleGD.senderZaloPayID = "sender";
        sentBundleGD.type = 5L;
        sentBundleGD.createTime = 100L;
        sentBundleGD.lastOpenTime = 150L;
        sentBundleGD.totalLuck = 20000L;
        sentBundleGD.numOfOpenedPakages = 2L;
        sentBundleGD.numOfPackages = 5L;
        sentBundleGD.sendMessage = "message";
        sentBundleGD.status = 2L;
        sendBundleGDs.add(sentBundleGD);
        mLocalStorage.putSentBundle(sendBundleGDs);

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 1482716479L;
        sentBundle.totalOfSentAmount = 3L;
        sentBundle.totalOfSentBundle = 2L;
        mLocalStorage.putSentBundleSummary(sentBundle);

        mRepository = new RedPacketRepository(null, mRequestTPEService, mLocalStorage, dataMapper, user, 1, new Gson());

        mRepository.setBundleStatus(2, 1).subscribe();
        mRepository.getSentBundleList(101L, 1).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("setBundleStatus with undefined bundle id", 2, result.get(0).sentbundlelist.get(0).status);
    }

    @Test
    public void setBundleStatus() {
        List<GetSentBundle> result = new ArrayList<>();
        List<SentBundleGD> sentBundleGDs = new ArrayList<>();

        SentBundleSummaryDB sentBundle = new SentBundleSummaryDB();
        sentBundle.id = 1L;
        sentBundle.timeCreate = 1482716479L;
        sentBundle.totalOfSentAmount = 3L;
        sentBundle.totalOfSentBundle = 2L;
        mLocalStorage.putSentBundleSummary(sentBundle);

        int inputSize = 20;
        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;
            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 100L + j;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 2L;
            sentBundleGDs.add(sentBundleGD);
        }
        mLocalStorage.putSentBundle(sentBundleGDs);

        mRepository = new RedPacketRepository(null, mRequestTPEService, mLocalStorage, dataMapper, user, 1, new Gson());

        mRepository.setBundleStatus(17, 1).subscribe();
        mRepository.getSentBundleList(121L, 5).subscribe(new CustomObserver<>(result));
        Assert.assertEquals("setBundleStatus", 1, result.get(0).sentbundlelist.get(4).status);
    }

    @Test
    public void getSentBundleListServer() {
        final List<GetSentBundle> getSentBundles = new ArrayList<>();

        mRepository.getSentBundleListServer(111, 3, -1).subscribe(new CustomObserver<>(getSentBundles));

        assertEquals(sentBundleListResponse, getSentBundles.get(0));
    }

    @Test
    public void getSentBundleListWithHavingLocal() {
        List<GetSentBundle> result = new ArrayList<>();
        List<SentBundleGD> sentBundleGDs = new ArrayList<>();

        SentBundleSummaryDB sentBundleSummaryDB = new SentBundleSummaryDB();
        sentBundleSummaryDB.id = 1L;
        sentBundleSummaryDB.timeCreate = 1482716479L;
        sentBundleSummaryDB.totalOfSentAmount = 3L;
        sentBundleSummaryDB.totalOfSentBundle = 2L;
        mLocalStorage.putSentBundleSummary(sentBundleSummaryDB);

        int inputSize = 20;
        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;
            SentBundleGD sentBundleGD = new SentBundleGD();
            sentBundleGD.id = 1L + j;
            sentBundleGD.senderZaloPayID = "sender";
            sentBundleGD.type = 5L;
            sentBundleGD.createTime = 100L + j;
            sentBundleGD.lastOpenTime = 150L;
            sentBundleGD.totalLuck = 20000L;
            sentBundleGD.numOfOpenedPakages = 2L;
            sentBundleGD.numOfPackages = 5L;
            sentBundleGD.sendMessage = "message";
            sentBundleGD.status = 2L;
            sentBundleGDs.add(sentBundleGD);
        }
        mLocalStorage.putSentBundle(sentBundleGDs);

        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, dataMapper, user, 1, new Gson());

        GetSentBundle tmp = new GetSentBundle();
        tmp.totalofsentamount = sentBundleSummaryDB.totalOfSentAmount;
        tmp.totalofsentbundle = sentBundleSummaryDB.totalOfSentBundle;
        tmp.sentbundlelist = new ArrayList<>();
        for(int i = 0; i < inputSize; i++) {
            int j = i + 1;
            SentBundle sentBundle = new SentBundle();
            sentBundle.bundleID = 1L + j;
            sentBundle.sendZaloPayID = "sender";
            sentBundle.type = 5L;
            sentBundle.createTime = 100L + j;
            sentBundle.lastOpenTime = 150L;
            sentBundle.totalLuck = 20000L;
            sentBundle.numOfOpenedPakages = 2L;
            sentBundle.numOfPackages = 5L;
            sentBundle.sendMessage = "message";
            sentBundle.status = 2L;
            tmp.sentbundlelist.add(sentBundle);
        }

        mRepository.getSentBundleList(111, 3).subscribe(new CustomObserver<>(result));

        assertEquals(tmp, result.get(0), 9);
    }

    @Test
    public void getSentBundleList() {
        final List<GetSentBundle> getSentBundles = new ArrayList<>();

        mRepository.getSentBundleList(110, 3).subscribe(new CustomObserver<>(getSentBundles));

        assertEquals(sentBundleListResponse, getSentBundles.get(0), 9);
    }

    @Test
    public void getReceivedPackagesServer() {
        final List<GetReceivePacket> getReceivePackets = new ArrayList<>();

        mRepository.getReceivedPackagesServer(110, 3, -1).subscribe(new CustomObserver<>(getReceivePackets));

        assertEquals(getReceivePackageResponse, getReceivePackets.get(0));
    }

    @Test
    public void getReceivePacketListWithHavingLocal() {
        List<GetReceivePacket> result = new ArrayList<>();
        List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        ReceivePacketSummaryDB receivePacket = new ReceivePacketSummaryDB();
        receivePacket.id = 1L;
        receivePacket.timeCreate = 1482716479L;
        receivePacket.totalOfLuckiestDraw = 3L;
        receivePacket.totalOfRevamount = 4L;
        receivePacket.totalOfRevPackage = 2L;
        mLocalStorage.putReceivePacketSummary(receivePacket);

        int inputSize = 20;
        for(int i = 0; i < inputSize; i++) {
            ReceivePackageGD receivePackageGD = new ReceivePackageGD();
            receivePackageGD.id = 1L + i;
            receivePackageGD.bundleID = 1L + i;
            receivePackageGD.receiverZaloPayID = "receiver";
            receivePackageGD.senderZaloPayID = "id";
            receivePackageGD.senderFullName = "name";
            receivePackageGD.senderAvatar = "ava";
            receivePackageGD.amount = 100000L;
            receivePackageGD.openedTime = 200L + i;
            receivePackageGD.status = 1L;
            receivePackageGD.messageStatus = "messStt";
            receivePackageGD.message = "mess";
            receivePackageGD.isLuckiest = 1L;
            receivePackageGD.createTime = 100L + i;
            receivePackageGDs.add(receivePackageGD);
        }
        mLocalStorage.putReceivePackages(receivePackageGDs);

        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, dataMapper, user, 1, new Gson());

        GetReceivePacket tmp = new GetReceivePacket();
        tmp.totalofrevamount = receivePacket.totalOfRevamount;
        tmp.totalofrevpackage = receivePacket.totalOfRevPackage;
        tmp.numofluckiestdraw = receivePacket.totalOfLuckiestDraw;
        tmp.revpackageList = new ArrayList<>();
        for(int i = 0; i < inputSize; i++) {
            ReceivePackage receivePackage = new ReceivePackage();
            receivePackage.packageID = 1 + i;
            receivePackage.bundleID = 1 + i;
            receivePackage.revZaloPayID = "receiver";
            receivePackage.senderZaloPayID = "id";
            receivePackage.senderFullName = "name";
            receivePackage.senderAvatar = "ava";
            receivePackage.amount = 100000;
            receivePackage.openedTime = 200L + i;
            receivePackage.status = 1L;
            receivePackage.message = "mess";
            receivePackage.isLuckiest = 1;
            receivePackage.createTime = 100L + Long.valueOf(i);
            tmp.revpackageList.add(receivePackage);
        }

        mRepository.getReceivePacketList(110, 3).subscribe(new CustomObserver<>(result));

        assertEquals(tmp, result.get(0), 9);
    }

    @Test
    public void getReceivePacketList() {
        final List<GetReceivePacket> getReceivePackets = new ArrayList<>();

        mRepository.getReceivePacketList(110, 3).subscribe(new CustomObserver<>(getReceivePackets));

        assertEquals(getReceivePackageResponse, getReceivePackets.get(0), 10);
    }

    @Test
    public void addReceivedRedPacket() {
        final List<ReceivePackage> receivePackages = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess").subscribe();
        mRepository.getReceivedPacket(1).subscribe(new CustomObserver<>(receivePackages));

        Assert.assertEquals("packageID", 1, receivePackages.get(0).packageID);
        Assert.assertEquals("bundleID", 1L, receivePackages.get(0).bundleID);
        Assert.assertEquals("senderFullName", "name", receivePackages.get(0).senderFullName);
        Assert.assertEquals("senderAvatar", "ava", receivePackages.get(0).senderAvatar);
        Assert.assertEquals("message", "mess", receivePackages.get(0).message);
    }

    @Test
    public void addReceivedRedPacketWhenAlreadyHave() {
        final List<ReceivePackage> receivePackages = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess").subscribe();
        mRepository.addReceivedRedPacket(1, 3, "name1", "ava1", "mess1").subscribe();
        mRepository.getReceivedPacket(1).subscribe(new CustomObserver<>(receivePackages));

        Assert.assertEquals("packageID", 1, receivePackages.get(0).packageID);
        Assert.assertEquals("bundleID", 3L, receivePackages.get(0).bundleID);
        Assert.assertEquals("senderFullName", "name1", receivePackages.get(0).senderFullName);
        Assert.assertEquals("senderAvatar", "ava1", receivePackages.get(0).senderAvatar);
        Assert.assertEquals("message", "mess1", receivePackages.get(0).message);
    }

    @Test
    public void getReceivedPacketWhenNotHavingDatas() {
        final List<ReceivePackage> receivePackages = new ArrayList<>();

        mRepository.getReceivedPacket(1).subscribe(new CustomObserver<>(receivePackages));

        Assert.assertEquals("getReceivedPacket when not having datas", null, receivePackages.get(0));
    }

    @Test
    public void getAllPacketInBundleServer() {
        final List<Boolean> results = new ArrayList<>();

        mRepository.getAllPacketInBundleServer(125).subscribe(new CustomObserver<>(results));

        Assert.assertEquals("getAllPacketInBundleServer", true, results.get(0));
    }

    @Test
    public void getPacketsInBundle() {
        final List<PackageInBundle> results = new ArrayList<>();
        List<PackageInBundleGD> packageInBundleList = new ArrayList<PackageInBundleGD>();

        for(int i = 0; i < 25; i++) {
            PackageInBundleGD packageInBundleGD = new PackageInBundleGD();
            packageInBundleGD.id = 1L + i;
            packageInBundleGD.bundleID = 1L;
            packageInBundleGD.amount = 2L;
            packageInBundleGD.isLuckiest = 1L;
            packageInBundleGD.openTime = 1241235L + i;
            packageInBundleGD.revAvatarURL = "Ava";
            packageInBundleGD.revFullName = "Full name";
            packageInBundleGD.revZaloID = 1L;
            packageInBundleGD.revZaloPayID = "ZaloPayID";
            packageInBundleGD.sendMessage = "Message";

            packageInBundleList.add(packageInBundleGD);
        }
        mLocalStorage.putPackageInBundle(packageInBundleList);
        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, dataMapper, new User("1"), 1, new Gson());

        mRepository.getPacketsInBundle(100).subscribe(new DefaultObserver<>(results));

        for(int i = 0; i < results.size(); i++) {
            assertEquals(packageInBundleList.get(i), results.get(i));
        }
    }

    @Test
    public void getPacketsInBundleWhenNotHavingDatas() {
        final List<PackageInBundle> results = new ArrayList<>();

        mRepository.getPacketsInBundle(100).subscribe(new DefaultObserver<>(results));

        Assert.assertEquals("getPacketsInBundle when not having datas", 0, results.size());
    }

    @Test
    public void setPacketStatusWhenNotHavingPacket() {
        final List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        mRepository.setPacketStatus(1, 3, 1, "message").subscribe();
        mRepository.getPacketStatus("1").subscribe(new CustomObserver<>(receivePackageGDs));

        Assert.assertEquals("setPacketStatus when not having packet", null, receivePackageGDs.get(0));
    }

    @Test
    public void setPacketStatus() {
        final List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess").subscribe();
        mRepository.setPacketStatus(1, 3, 1, "message").subscribe();
        mRepository.getPacketStatus("1").subscribe(new CustomObserver<>(receivePackageGDs));

        Assert.assertEquals("id", 1, receivePackageGDs.get(0).id);
        Assert.assertEquals("amount", (Long) 3L, receivePackageGDs.get(0).amount);
        Assert.assertEquals("status", (Long) 1L, receivePackageGDs.get(0).status);
        Assert.assertEquals("message", "message", receivePackageGDs.get(0).messageStatus);
    }

    @Test
    public void getPacketStatusWhenNotHavingPacket() {
        final List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        mRepository.getPacketStatus("1").subscribe(new CustomObserver<>(receivePackageGDs));

        Assert.assertEquals("getPacketStatus when not having packet", null, receivePackageGDs.get(0));
    }

    @Test
    public void getPacketStatusWhenNotSetting() {
        final List<ReceivePackageGD> receivePackageGDs = new ArrayList<>();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess").subscribe();
        mRepository.getPacketStatus("1").subscribe(new CustomObserver<>(receivePackageGDs));

        Assert.assertEquals("id", 1, receivePackageGDs.get(0).id);
        Assert.assertEquals("amount", null, receivePackageGDs.get(0).amount);
        Assert.assertEquals("status", (Long) 1L, receivePackageGDs.get(0).status);
        Assert.assertEquals("message", null, receivePackageGDs.get(0).messageStatus);
    }

    @Test
    public void getAppInfoServer() {
        final List<RedPacketAppInfo> redPacketAppInfos = new ArrayList<>();

        mRepository.getAppInfoServer("checksum").subscribe(new CustomObserver<>(redPacketAppInfos));

        assertEquals(redPacketAppInfoResponse, redPacketAppInfos.get(0));
    }

    @Test
    public void getRedPacketAppInfoWithNonUpdate() {
        final List<RedPacketAppInfo> redPacketAppInfos = new ArrayList<>();

        RedPacketAppInfo redPacketAppInfo = new RedPacketAppInfo();
        redPacketAppInfo.checksum = "checksum";
        redPacketAppInfo.expiredTime = System.currentTimeMillis() + 10000L;
        redPacketAppInfo.isUpdateAppInfo = true;
        redPacketAppInfo.appConfigEntity = new AppConfigEntity();
        mLocalStorage.putRedPacketAppInfo(redPacketAppInfo);
        mRepository = new RedPacketRepository(null, mRequestTPEService, mLocalStorage, dataMapper, new User("1"), 1, new Gson());

        mRepository.getRedPacketAppInfo().subscribe(new CustomObserver<>(redPacketAppInfos));

        assertEquals(redPacketAppInfo, redPacketAppInfos.get(0));
    }

    @Test
    public void getRedPacketAppInfoWithUpdateAndNotHavingChecksum() {
        final List<RedPacketAppInfo> redPacketAppInfos = new ArrayList<>();

        RedPacketAppInfo redPacketAppInfo = new RedPacketAppInfo();
        redPacketAppInfo.checksum = "abc";
        redPacketAppInfo.expiredTime = 1232156L;
        redPacketAppInfo.isUpdateAppInfo = true;
        redPacketAppInfo.appConfigEntity = new AppConfigEntity();
        mLocalStorage.putRedPacketAppInfo(redPacketAppInfo);
        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, dataMapper, new User("1"), 1, new Gson());

        mRepository.getRedPacketAppInfo().subscribe(new CustomObserver<>(redPacketAppInfos));

        Assert.assertEquals("getRedPacketAppInfo with update and not having checksum", 0, redPacketAppInfos.size());
    }

    @Test
    public void getRedPacketAppInfoWithUpdate() {
//        final List<RedPacketAppInfo> redPacketAppInfos = new ArrayList<>();
//
//        RedPacketAppInfo redPacketAppInfo = new RedPacketAppInfo();
//        redPacketAppInfo.checksum = "checksum";
//        redPacketAppInfo.expiredTime = 1232156L;
//        redPacketAppInfo.isUpdateAppInfo = true;
//        redPacketAppInfo.appConfigEntity = new AppConfigEntity();
//        mLocalStorage.putRedPacketAppInfo(redPacketAppInfo);
//        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, dataMapper, new User("1"), 1, new Gson());
//
//        mRepository.getRedPacketAppInfo().subscribe(new CustomObserver<>(redPacketAppInfos));
//
//        assertEquals(redPacketAppInfo, redPacketAppInfos.get(0));
    }

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
            indexOfp1--;
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

        Assert.assertEquals("numofluckiestdraw", p1.numOfLuckiestDraw, p2.numofluckiestdraw);
        Assert.assertEquals("totalofrevamount", p1.totalOfRevAmount, p2.totalofrevamount);
        Assert.assertEquals("totalofrevpackage", p1.totalOfRevPackage, p2.totalofrevpackage);
//        if (p1.receivePackageResponses.size() == 0 && p2.revpackageList.size() != 0) {
//            Assert.fail("Compare null and non-null object");
//            return;
//        }
//
//        if (p1.receivePackageResponses.size() != 0 && p2.revpackageList.size() == 0) {
//            Assert.fail("Compare null and non-null object");
//            return;
//        }
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
            indexOfp1--;
        }
    }

    private void assertEquals(PackageInBundleGD b1, PackageInBundle b2) {
        if (b1 == null && b2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("amount", (long) b1.amount, b2.amount);
        Assert.assertEquals("sendMessage", b1.sendMessage, b2.sendMessage);
        Assert.assertEquals("revZaloPayID", b1.revZaloPayID, b2.revZaloPayID);
        Assert.assertEquals("revZaloID", (long) b1.revZaloID, b2.revZaloID);
        if(b1.isLuckiest == 1) {
            Assert.assertEquals("isLuckiest", true, b2.isLuckiest);
        }
        else if(b1.isLuckiest == 0) {
            Assert.assertEquals("isLuckiest", false, b2.isLuckiest);
        }
        Assert.assertEquals("revFullName", b1.revFullName, b2.revFullName);
        Assert.assertEquals("revAvatarURL", b1.revAvatarURL, b2.revAvatarURL);
        Assert.assertEquals("bundleID", (long) b1.bundleID, b2.bundleID);
        Assert.assertEquals("openTime", (long) b1.openTime, b2.openTime);
        Assert.assertEquals("id", (long) b1.id, b2.packageID);
    }

    private void assertEquals(RedPacketAppInfoResponse b1, RedPacketAppInfo b2) {
        if (b1 == null && b2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("checksum", b1.checksum, b2.checksum);
        Assert.assertEquals("expiredTime", b1.expiredTime, b2.expiredTime);
        Assert.assertEquals("isUpdateAppInfo", b1.isUpdateAppInfo, b2.isUpdateAppInfo);
        Assert.assertEquals("bundleExpiredTime", b1.appConfigResponse.bundleExpiredTime, b2.appConfigEntity.bundleExpiredTime);
        Assert.assertEquals("maxAmountPerPackage", b1.appConfigResponse.maxAmountPerPackage, b2.appConfigEntity.maxAmountPerPackage);
        Assert.assertEquals("maxCountHist", b1.appConfigResponse.maxCountHist, b2.appConfigEntity.maxCountHist);
        Assert.assertEquals("maxMessageLength", b1.appConfigResponse.maxMessageLength, b2.appConfigEntity.maxMessageLength);
        Assert.assertEquals("maxPackageQuantity", b1.appConfigResponse.maxPackageQuantity, b2.appConfigEntity.maxPackageQuantity);
        Assert.assertEquals("maxTotalAmountPerBundle", b1.appConfigResponse.maxTotalAmountPerBundle, b2.appConfigEntity.maxTotalAmountPerBundle);
        Assert.assertEquals("minAmountEach", b1.appConfigResponse.minAmountEach, b2.appConfigEntity.minAmountEach);
        Assert.assertEquals("minDivideAmount", b1.appConfigResponse.minDivideAmount, b2.appConfigEntity.minDivideAmount);
    }

    private void assertEquals(GetReceivePackageResponse b1, GetReceivePacket b2) {
        if (b1 == null && b2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("numofluckiestdraw", b1.numOfLuckiestDraw, b2.numofluckiestdraw);
        Assert.assertEquals("totalofrevamount", b1.totalOfRevAmount, b2.totalofrevamount);
        Assert.assertEquals("totalofrevpackage", b1.totalOfRevPackage, b2.totalofrevpackage);
        if (b1.receivePackageResponses.size() == 0 && b2.revpackageList.size() != 0) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1.receivePackageResponses.size() != 0 && b2.revpackageList.size() == 0) {
            Assert.fail("Compare null and non-null object");
            return;
        }
        for(int i = 0; i < b2.revpackageList.size(); i++) {
            Assert.assertEquals("amount", b1.receivePackageResponses.get(i).amount,
                    b2.revpackageList.get(i).amount);
            Assert.assertEquals("bundleID", b1.receivePackageResponses.get(i).bundleid,
                    b2.revpackageList.get(i).bundleID);
            Assert.assertEquals("createTime", b1.receivePackageResponses.get(i).createtime,
                    b2.revpackageList.get(i).createTime);
            Assert.assertEquals("isLuckiest", b1.receivePackageResponses.get(i).isluckiest,
                    b2.revpackageList.get(i).isLuckiest);
            Assert.assertEquals("openedTime", b1.receivePackageResponses.get(i).openedtime,
                    b2.revpackageList.get(i).openedTime);
            Assert.assertEquals("packageID", b1.receivePackageResponses.get(i).packageid,
                    b2.revpackageList.get(i).packageID);
//            Assert.assertEquals("message", b1.receivePackageResponses.get(i).message,
//                    b2.revpackageList.get(i).message);
            Assert.assertEquals("senderAvatar", b1.receivePackageResponses.get(i).avatarofsender,
                    b2.revpackageList.get(i).senderAvatar);
            Assert.assertEquals("revZaloPayID", b1.receivePackageResponses.get(i).revzalopayid,
                    b2.revpackageList.get(i).revZaloPayID);
            Assert.assertEquals("senderFullName", b1.receivePackageResponses.get(i).sendfullname,
                    b2.revpackageList.get(i).senderFullName);
            Assert.assertEquals("senderZaloPayID", b1.receivePackageResponses.get(i).sendzalopayid,
                    b2.revpackageList.get(i).senderZaloPayID);
        }
    }

    private void assertEquals(SentBundleListResponse p1, GetSentBundle p2) {
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
            Assert.assertEquals("bundleid", p1.bundleResponseList.get(i).bundleid,
                    p2.sentbundlelist.get(i).bundleID);
            Assert.assertEquals("createtime", p1.bundleResponseList.get(i).createtime,
                    p2.sentbundlelist.get(i).createTime);
            Assert.assertEquals("lastopentime", p1.bundleResponseList.get(i).lastopentime,
                    p2.sentbundlelist.get(i).lastOpenTime);
            Assert.assertEquals("numofopenedpakages", p1.bundleResponseList.get(i).numofopenedpakages,
                    p2.sentbundlelist.get(i).numOfOpenedPakages);
            Assert.assertEquals("numofpackages", p1.bundleResponseList.get(i).numofpackages,
                    p2.sentbundlelist.get(i).numOfPackages);
            Assert.assertEquals("sendmessage", p1.bundleResponseList.get(i).sendmessage,
                    p2.sentbundlelist.get(i).sendMessage);
            Assert.assertEquals("sendzalopayid", p1.bundleResponseList.get(i).sendzalopayid,
                    p2.sentbundlelist.get(i).sendZaloPayID);
            Assert.assertEquals("totalluck", p1.bundleResponseList.get(i).totalluck,
                    p2.sentbundlelist.get(i).totalLuck);
            Assert.assertEquals("type", p1.bundleResponseList.get(i).type,
                    p2.sentbundlelist.get(i).type);
        }
    }

    private void assertEquals(GetSentBundle p1, GetSentBundle p2, int indexOfp1) {
        if (p1 == null && p2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (p1 != null && p2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("totalOfSentAmount", p1.totalofsentamount, p2.totalofsentamount);
        Assert.assertEquals("totalOfSentBundle", p1.totalofsentbundle, p2.totalofsentbundle);
        if (p1.sentbundlelist.size() == 0 && p2.sentbundlelist.size()!= 0) {
            Assert.fail("Compare null and non-null bundleResponseList");
            return;
        }
        if (p1.sentbundlelist.size() != 0 && p2.sentbundlelist.size() == 0) {
            Assert.fail("Compare null and non-null bundleResponseList");
            return;
        }
        for(int i = 0; i < p2.sentbundlelist.size(); i++) {
            Assert.assertEquals("bundleid", p1.sentbundlelist.get(indexOfp1).bundleID,
                    p2.sentbundlelist.get(i).bundleID);
            Assert.assertEquals("createtime", p1.sentbundlelist.get(indexOfp1).createTime,
                    p2.sentbundlelist.get(i).createTime);
            Assert.assertEquals("lastopentime", p1.sentbundlelist.get(indexOfp1).lastOpenTime,
                    p2.sentbundlelist.get(i).lastOpenTime);
            Assert.assertEquals("numofopenedpakages", p1.sentbundlelist.get(indexOfp1).numOfOpenedPakages,
                    p2.sentbundlelist.get(i).numOfOpenedPakages);
            Assert.assertEquals("numofpackages", p1.sentbundlelist.get(indexOfp1).numOfPackages,
                    p2.sentbundlelist.get(i).numOfPackages);
            Assert.assertEquals("sendmessage", p1.sentbundlelist.get(indexOfp1).sendMessage,
                    p2.sentbundlelist.get(i).sendMessage);
            Assert.assertEquals("sendzalopayid", p1.sentbundlelist.get(indexOfp1).sendZaloPayID,
                    p2.sentbundlelist.get(i).sendZaloPayID);
            Assert.assertEquals("totalluck", p1.sentbundlelist.get(indexOfp1).totalLuck,
                    p2.sentbundlelist.get(i).totalLuck);
            Assert.assertEquals("type", p1.sentbundlelist.get(indexOfp1).type,
                    p2.sentbundlelist.get(i).type);
            indexOfp1--;
        }
    }

    private void assertEquals(GetReceivePacket p1, GetReceivePacket p2, int indexOfp1) {
        if (p1 == null && p2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (p1 != null && p2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("numofluckiestdraw", p1.numofluckiestdraw, p2.numofluckiestdraw);
        Assert.assertEquals("totalofrevamount", p1.totalofrevamount, p2.totalofrevamount);
        Assert.assertEquals("totalofrevpackage", p1.totalofrevpackage, p2.totalofrevpackage);
//        if (p1.revpackageList.size() == 0 && p2.revpackageList.size() != 0) {
//            Assert.fail("Compare null and non-null object");
//            return;
//        }
//
//        if (p1.revpackageList.size() != 0 && p2.revpackageList.size() == 0) {
//            Assert.fail("Compare null and non-null object");
//            return;
//        }
        for(int i = 0; i < p2.revpackageList.size(); i++) {
            Assert.assertEquals("amount", p1.revpackageList.get(indexOfp1).amount,
                    p2.revpackageList.get(i).amount);
            Assert.assertEquals("bundleid", p1.revpackageList.get(indexOfp1).bundleID,
                    p2.revpackageList.get(i).bundleID);
            Assert.assertEquals("createtime", p1.revpackageList.get(indexOfp1).createTime,
                    p2.revpackageList.get(i).createTime);
            Assert.assertEquals("isluckiest", p1.revpackageList.get(indexOfp1).isLuckiest,
                    p2.revpackageList.get(i).isLuckiest);
            Assert.assertEquals("message", p1.revpackageList.get(indexOfp1).message,
                    p2.revpackageList.get(i).message);
            Assert.assertEquals("openedtime", p1.revpackageList.get(indexOfp1).openedTime,
                    p2.revpackageList.get(i).openedTime);
            Assert.assertEquals("packageid", p1.revpackageList.get(indexOfp1).packageID,
                    p2.revpackageList.get(i).packageID);
            Assert.assertEquals("revzalopayid", p1.revpackageList.get(indexOfp1).revZaloPayID,
                    p2.revpackageList.get(i).revZaloPayID);
            Assert.assertEquals("avatarofsender", p1.revpackageList.get(indexOfp1).senderAvatar,
                    p2.revpackageList.get(i).senderAvatar);
            Assert.assertEquals("sendfullname", p1.revpackageList.get(indexOfp1).senderFullName,
                    p2.revpackageList.get(i).senderFullName);
            Assert.assertEquals("revzalopayid", p1.revpackageList.get(indexOfp1).senderZaloPayID,
                    p2.revpackageList.get(i).senderZaloPayID);
            indexOfp1--;
        }
    }

    private void assertEquals(RedPacketAppInfo b1, RedPacketAppInfo b2) {
        if (b1 == null && b2 != null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        if (b1 != null && b2 == null) {
            Assert.fail("Compare null and non-null object");
            return;
        }

        Assert.assertEquals("checksum", b1.checksum, b2.checksum);
        Assert.assertEquals("expiredTime", b1.expiredTime, b2.expiredTime);
        Assert.assertEquals("isUpdateAppInfo", false, b2.isUpdateAppInfo);
        Assert.assertEquals("bundleExpiredTime", b1.appConfigEntity.bundleExpiredTime, b2.appConfigEntity.bundleExpiredTime);
        Assert.assertEquals("maxAmountPerPackage", b1.appConfigEntity.maxAmountPerPackage, b2.appConfigEntity.maxAmountPerPackage);
        Assert.assertEquals("maxCountHist", b1.appConfigEntity.maxCountHist, b2.appConfigEntity.maxCountHist);
        Assert.assertEquals("maxMessageLength", b1.appConfigEntity.maxMessageLength, b2.appConfigEntity.maxMessageLength);
        Assert.assertEquals("maxPackageQuantity", b1.appConfigEntity.maxPackageQuantity, b2.appConfigEntity.maxPackageQuantity);
        Assert.assertEquals("maxTotalAmountPerBundle", b1.appConfigEntity.maxTotalAmountPerBundle, b2.appConfigEntity.maxTotalAmountPerBundle);
        Assert.assertEquals("minAmountEach", b1.appConfigEntity.minAmountEach, b2.appConfigEntity.minAmountEach);
        Assert.assertEquals("minDivideAmount", b1.appConfigEntity.minDivideAmount, b2.appConfigEntity.minDivideAmount);
    }
}
