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
import vn.com.vng.zalopay.data.DefaultObjectObserver;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.ListRedPacketStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.ReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;

/**
 * Created by longlv on 14/07/2016.
 * Unit test for RedPackageRepository
 */
@RunWith(CustomRobolectricRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class RedPackageRepositoryTest {
    private RedPacketStore.LocalStorage mLocalStorage;
    private RedPacketStore.Repository mRepository;
    private RedPacketStore.RequestService mRequestService;
    private RedPacketStore.RequestTPEService mRequestTPEService;
    private User user;

    //declare Variable
    private BundleOrderResponse bundleOrderResponse;
    private BaseResponse baseResponse;
    private SubmitOpenPackageResponse submitOpenPackageResponse;
    private PackageStatusResponse packageStatusResponse;
    private SentBundleListResponse sentBundleListResponse;
    private GetReceivePackageResponse getReceivePackageResponse;
    private SentPackageInBundleResponse sentPackageInBundleResponse;

    private DaoSession mDaoSession;

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
        baseResponse.message = "";

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
        for (int i = 0; i < 20; i++) {
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

        getReceivePackageResponse = new GetReceivePackageResponse();
        getReceivePackageResponse.totalOfRevAmount = 300000L;
        getReceivePackageResponse.totalOfRevPackage = 3L;
        getReceivePackageResponse.numOfLuckiestDraw = 2L;
        getReceivePackageResponse.receivePackageResponses = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
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
        for (int i = 0; i < 20; i++) {
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

    }

    @Before
    public void setup() throws Exception {
        setupRequestServiceVariable();

        mRequestService = new RequestServiceImpl();
        mRequestTPEService = new RequestTPEServiceImpl();

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        mDaoSession = new DaoMaster(db).newSession();
        mLocalStorage = new RedPacketLocalStorage(mDaoSession);

        user = new User("zaloPayID");
        user.zaloId = 123;
        user.displayName = "name";
        user.avatar = "avatar";

        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, user, 1, new Gson());
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
        public Observable<SubmitOpenPackageResponse> submitOpenPackage(@Field("packageID") long packageID, @Field("bundleID") long bundleID, @Field("revZaloPayID") String revZaloPayID, @Field("accessToken") String accessToken) {
            return Observable.just(submitOpenPackageResponse);
        }

        @Override
        public Observable<BaseResponse> submittosendbundlebyzalopayinfo(@Field("bundleid") long bundleID, @Field("zalopayoffriendlist") String friends, @Field("zalopayofsender") String sender, @Field("accesstoken") String accessToken) {
            return Observable.just(baseResponse);
        }

        @Override
        public Observable<ListRedPacketStatusResponse> getListPackageStatus(@Query("listpackageid") String listpackageid, @Query("userid") String zalopayId, @Query("accesstoken") String accessToken) {
            return Observable.just(new ListRedPacketStatusResponse());
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
        List<RedPacketUserEntity> entities = new ArrayList<>();
        RedPacketUserEntity entity = new RedPacketUserEntity();
        entity.avatar = "ava";
        entity.zaloID = "zaloId";
        entity.zaloName = "name";
        entity.zaloPayID = "id";
        for (int i = 0; i < 5; i++) {
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

        mRepository.getPackageStatus(1L, 2L, "abcd").subscribe(new CustomObserver<>(result));

        assertEquals(packageStatusResponse, result.get(0));
    }

    @Test
    public void addReceivedRedPacketWhenAlreadyHave() {
        ReceivePackageGD receivePackageGD = new ReceivePackageGD();

        mRepository.addReceivedRedPacket(1, 1, "name", "ava", "mess").subscribe();
        mRepository.addReceivedRedPacket(1, 3, "name1", "ava1", "mess1").subscribe();
        mRepository.getPacketStatus("1").subscribe(new DefaultObjectObserver<>(receivePackageGD));

        Assert.assertEquals("packageID", 1, receivePackageGD.id);
        Assert.assertEquals("bundleID", 3L, (long) receivePackageGD.bundleID);
        Assert.assertEquals("senderFullName", "name1", receivePackageGD.senderFullName);
        Assert.assertEquals("senderAvatar", "ava1", receivePackageGD.senderAvatar);
        Assert.assertEquals("message", "mess1", receivePackageGD.message);
    }

    @Test
    public void getReceivedPacketWhenNotHavingData() {
        final ReceivePackageGD receivePackageGD = null;

        mRepository.getPacketStatus("1000").subscribe(new DefaultObjectObserver<>(receivePackageGD));

        Assert.assertEquals("getReceivedPacket when not having data", null, receivePackageGD);
    }

    @Test
    public void setPacketStatusWhenNotHavingPacket() {
        ReceivePackageGD receivePackageGD = null;

        mRepository.setPacketStatus(1, 3, 1, "message").subscribe();
        mRepository.getPacketStatus("1").subscribe(new DefaultObjectObserver<>(receivePackageGD));

        Assert.assertEquals("setPacketStatus when not having packet", null, receivePackageGD);
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

}
