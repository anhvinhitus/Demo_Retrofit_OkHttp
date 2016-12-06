package vn.com.vng.zalopay.data;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
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
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.RedPacketAppInfoResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.redpacket.RedPacketLocalStorage;
import vn.com.vng.zalopay.data.redpacket.RedPacketRepository;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

/**
 * Created by longlv on 14/07/2016.
 * Unit test for RedPackageRepository
 */
@RunWith(CustomRobolectricRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class RedPackageRepositoryTest {

    int quantity = 1;
    int totalLuck = 12;
    long amountEach = 20000;
    int type = 1;
    String message = "Chuc may man lan sau";

    RedPacketStore.LocalStorage mLocalStorage;
    RedPacketStore.Repository mRepository;
    RedPacketStore.RequestService mRequestService;
    RedPacketStore.RequestTPEService mRequestTPEService;

    //declare Variable
    BundleOrderResponse bundleOrderResponse;
    BaseResponse baseResponse;
    SubmitOpenPackageResponse submitOpenPackageResponse;

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
    }

    @Before
    public void setup() throws Exception {
        setupRequestServiceVariable();

        mRequestService = new RequestServiceImpl();
        mRequestTPEService = new RequestTPEServiceImpl();

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        mDaoSession = new DaoMaster(db).newSession();
        RedPacketDataMapper dataMapper = new RedPacketDataMapper();
        mLocalStorage = new RedPacketLocalStorage(mDaoSession, dataMapper);

        mRepository = new RedPacketRepository(mRequestService, mRequestTPEService, mLocalStorage, dataMapper, new User("1"), 1, new Gson());
    }

    public class RequestTPEServiceImpl implements RedPacketStore.RequestTPEService {

        @Override
        public Observable<PackageStatusResponse> getPackageStatus(@Query("appid") int appId, @Query("packageid") long packageID, @Query("zptransid") long zpTransID, @Query("userid") String userid, @Query("accesstoken") String accessToken, @Query("deviceid") String deviceid) {
            return null;
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
            return null;
        }

        @Override
        public Observable<GetReceivePackageResponse> getReceivedPackageList(@Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zalopayid") String zalopayid, @Field("accesstoken") String accesstoken) {
            return null;
        }

        @Override
        public Observable<SentPackageInBundleResponse> getPackageInBundleList(@Field("bundleID") long bundleID, @Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zaloPayID") String zaloPayID, @Field("accessToken") String accessToken) {
            return null;
        }

        @Override
        public Observable<RedPacketAppInfoResponse> getAppInfo(@Query("checksum") String checksum, @Query("userid") String zalopayid, @Query("accesstoken") String accesstoken) {
            return null;
        }

        @Override
        public Observable<BaseResponse> submittosendbundlebyzalopayinfo(@Field("bundleid") long bundleID, @Field("zalopayoffriendlist") String friends, @Field("zalopayofsender") String sender, @Field("accesstoken") String accessToken) {
            return null;
        }

    }

    @Test
    public void testCreateBundle() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final List<BundleOrder> bundleOrderSource = new ArrayList<>();
        final List<BundleOrder> bundleOrders = new ArrayList<>();

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

        BundleOrder bundleOrder = new BundleOrder(bundleOrderResponse.getAppid(), bundleOrderResponse.getZptranstoken(), bundleOrderResponse.apptransid, bundleOrderResponse.appuser, bundleOrderResponse.apptime, bundleOrderResponse.embeddata, bundleOrderResponse.item, bundleOrderResponse.amount, bundleOrderResponse.description, bundleOrderResponse.payoption, bundleOrderResponse.mac, bundleOrderResponse.bundleID);
        bundleOrderSource.add(bundleOrder);

        Assert.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(bundleOrderSource, bundleOrders);
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
        inputItem.numOfOpenedPakages = 10;
        inputItem.numOfPackages = 20;
        inputItem.totalLuck = 4;
        inputItem.senderZaloPayID = "1611100000";
        inputItem.type = 1;
        inputItem.createTime = 1L;
        inputItem.lastOpenTime = 2L;
        inputItem.sendMessage = "message";
        inputItem.status = 1;
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
        inputItem.numOfOpenedPakages = 10;
        inputItem.numOfPackages = 20;
        inputItem.totalLuck = 4;
        inputItem.senderZaloPayID = "1611100000";
        inputItem.type = 1;
        inputItem.createTime = 1L;
        inputItem.lastOpenTime = 2L;
        inputItem.sendMessage = "message";
        inputItem.status = 1;

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
}
