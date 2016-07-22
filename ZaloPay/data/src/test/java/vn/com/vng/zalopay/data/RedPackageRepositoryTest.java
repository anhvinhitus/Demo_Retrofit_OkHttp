package vn.com.vng.zalopay.data;

import android.database.sqlite.SQLiteDatabase;

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
import rx.Observable;
import rx.Observer;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.redpacket.RedPacketLocalStorage;
import vn.com.vng.zalopay.data.redpacket.RedPacketRepository;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by longlv on 14/07/2016.
 * Unit test for RedPackageRepository
 */
@RunWith(RobolectricGradleTestRunner.class)
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

    //declare Variable
    BundleOrderResponse bundleOrderResponse;
    BaseResponse baseResponse;
    SubmitOpenPackageResponse submitOpenPackageResponse;

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

        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        RedPacketDataMapper dataMapper = new RedPacketDataMapper();
        mLocalStorage = new RedPacketLocalStorage(daoSession, dataMapper);

        mRepository = new RedPacketRepository(mRequestService, mLocalStorage, dataMapper, new User("1"));
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
        public Observable<PackageStatusResponse> getPackageStatus(@Field("packageid") long packageID, @Field("zptransid") long zpTransID, @Field("userid") String userid, @Field("accesstoken") String accessToken, @Field("deviceid") String deviceid) {
            return null;
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
    public void testRedPackage() throws Exception{
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


}
