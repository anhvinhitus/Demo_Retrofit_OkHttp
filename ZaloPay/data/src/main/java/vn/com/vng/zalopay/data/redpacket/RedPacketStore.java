package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.RevPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentPackageGD;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.SubmitOpenPackage;
import vn.com.vng.zalopay.domain.model.redpackage.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpackage.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpackage.SentBundle;
import vn.com.vng.zalopay.domain.model.redpackage.SentPackage;

/**
 * Created by longlv on 13/07/2016.
 * Declaration for redPackage local storage, redPackage request service, redPackage repository
 */
public interface RedPacketStore {

    interface LocalStorage {
        void putSentBundle(SentBundleGD sentBundle);
        void putSentBundle(List<SentBundleGD> sentBundle);
        Observable<List<SentBundle>> getAllSentBundle();
        Observable<List<SentBundle>> getSentBundle(int pageIndex, int limit);
        Observable<SentBundle> getSentBundle(long bundleID);

        void putSentPackage(List<SentPackageGD> sentPackages);
        Observable<List<SentPackage>> getAllSentPackage();
        Observable<List<SentPackage>> getSentPackage(int pageIndex, int limit);
        Observable<SentPackage> getSentPackage(long bundleID);

        void putReceivePackages(List<ReceivePackageGD> receivePackages);
        Observable<List<ReceivePackage>> getAllReceivePackage();
        Observable<List<ReceivePackage>> getReceivePackage(int pageIndex, int limit);
        Observable<ReceivePackage> getReceivePackage(long bundleID);
    }

    interface RequestService {
        @FormUrlEncoded
        @POST("redpackage/createbundleorder")
        Observable<BundleOrderResponse> createBundleOrder(@Field("quantity") int quantity, @Field("totalluck") long totalLuck, @Field("amounteach") long amountEach, @Field("type") int type, @Field("sendzalopayid") String sendZaloPayID, @Field("accesstoken") String accessToken, @Field("sendmessage") String sendMessage);

        @FormUrlEncoded
        @POST("redpackage/submittosendbundle")
        Observable<BaseResponse> sendBundle(@Field("bundleid") long bundleID, @Field("friendlist") String friendList, @Field("sendzalopayid") String sendZaloPayID, @Field("accesstoken") String accessToken);

        @FormUrlEncoded
        @POST("redpackage/submitopenpackage")
        Observable<SubmitOpenPackageResponse> submitOpenPackage(@Field("packageid") long packageID, @Field("bundleid") long bundleID, @Field("revzalopayid") String revZaloPayID, @Field("accesstoken") String accessToken);

        @FormUrlEncoded
        @POST("redpackage/getpackagestatus")
        Observable<PackageStatusResponse> getPackageStatus(@Field("packageid") long packageID, @Field("zptransid") long zpTransID, @Field("userid") String userid, @Field("accesstoken") String accessToken, @Field("deviceid") String deviceid);

        @FormUrlEncoded
        @POST("/rpe/getSentBundleList")
        Observable<SentBundleListResponse> getSentBundleList(@Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zalopayid") String zalopayid, @Field("accesstoken") String accesstoken);

        @FormUrlEncoded
        @POST("/rpe/getPackageInBundleList")
        Observable<SentPackageInBundleResponse> getPackageInBundleList(@Field("bundleid") long bundleid, @Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zalopayid") String zalopayid, @Field("accesstoken") String accesstoken);

        @FormUrlEncoded
        @POST("/rpe/getRevPackageList")
        Observable<RevPackageInBundleResponse> getRevPackageList(@Field("bundleid") long bundleid, @Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zalopayid") String zalopayid, @Field("accesstoken") String accesstoken);

    }

    /**
     * Interface for providing requests which relate with redPackage
     */
    interface Repository {
        Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage);

        Observable<Boolean> sendBundle(long bundleID, List<Long> friendList);

        Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID);

        Observable<PackageStatus> getpackagestatus(long packageID, long zpTransID);

        Observable<List<SentBundle>> getSentBundleList(long timestamp, int count, int order);

        Observable<List<SentPackage>> getPackageInBundleList(long bundleID, long timestamp, int count, int order);

        Observable<List<ReceivePackage>> getRevPackageList(long bundleID, long timestamp, int count, int order);
    }
}
