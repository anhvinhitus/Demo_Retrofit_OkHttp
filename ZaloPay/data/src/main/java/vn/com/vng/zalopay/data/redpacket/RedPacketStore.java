package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;

/**
 * Created by longlv on 13/07/2016.
 * Declaration for redPackage local storage, redPackage request service, redPackage repository
 */
public interface RedPacketStore {

    interface LocalStorage {
        void putSentBundle(SentBundleGD sentBundle);
        void putSentBundle(List<SentBundleGD> sentBundle);
        Observable<List<SentBundle>> getAllSentBundle();
        Observable<List<SentBundle>> getSentBundle(long timeCreate, int limit);
        Observable<SentBundle> getSentBundle(long bundleID);
        Boolean isHaveSentBundleInDb(long createTime);

        void putReceivePackages(List<ReceivePackageGD> receiveBundleGDs);
        Observable<List<ReceivePackage>> getAllReceiveBundle();
        Observable<List<ReceivePackage>> getReceiveBundle(long timeCreate, int limit);
        Observable<ReceivePackage> getReceiveBundle(long bundleID);
        Boolean isHaveReceivePacketInDb(long createTime);

        void putPackageInBundle(List<PackageInBundleGD> packageInBundleGDs);
        Observable<List<PackageInBundle>> getPackageInBundle(long bundleID, int pageIndex, int limit);
        Observable<List<PackageInBundle>> getPackageInBundle(long bundleID);
        Boolean isHavePackagesInDb(long bundleID);

        Boolean isPacketOpen(long packetId);

        Void setPacketIsOpen(long packetId, long amount);

        Void addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message);

        ReceivePackage getReceivedPacket(long packetId);

        Long getLastOpenTimeForPacketsInBundle(long bundleId);
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

        @GET("redpackage/getpackagestatus")
        Observable<PackageStatusResponse> getPackageStatus(@Query("packageid") long packageID, @Query("zptransid") long zpTransID, @Query("userid") String userid, @Query("accesstoken") String accessToken, @Query("deviceid") String deviceid);

        @GET("redpackage/getsentbundlelist")
        Observable<SentBundleListResponse> getSentBundleList(@Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("zalopayid") String zalopayid, @Query("accesstoken") String accesstoken);

        @GET("redpackage/getrevpackagelist")
        Observable<GetReceivePackageResponse> getReceivedPackageList(@Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("zalopayid") String zalopayid, @Query("accesstoken") String accesstoken);

        @GET("redpackage/getpackagesinbundle")
        Observable<SentPackageInBundleResponse> getPackageInBundleList(@Query("bundleid") long bundleid, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("zalopayid") String zalopayid, @Query("accesstoken") String accesstoken);
    }

    /**
     * Interface for providing requests which relate with redPackage
     */
    interface Repository {
        Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage);

        Observable<Boolean> sendBundle(long bundleID, List<Long> friendList);

        Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID);

        Observable<PackageStatus> getpackagestatus(long packageID, long zpTransID, String deviceId);

        Observable<List<SentBundle>> getSentBundleListServer(long timestamp, int count, int order);

        Observable<List<SentBundle>> getSentBundleList(long timeStamp, int count);

        Observable<Boolean> getAllSentBundlesServer();

        Observable<List<ReceivePackage>> getReceivedPackagesServer(long timestamp, int count, int order);

        Observable<List<ReceivePackage>> getReceivePacketList(long timeStamp, int count);

        Observable<ReceivePackage> getReceivedPacket(long packetId);

        Observable<Boolean> getAllReceivePacketServer();

        Observable<List<PackageInBundle>> getPackageInBundleList(long bundleID, long timestamp, int count, int order);

        Observable<Boolean> getAllPacketInBundleServer(long bundleId);

        Observable<List<PackageInBundle>> getPacketsInBundle(long bundleId);

        Observable<Boolean> isPacketOpen(String packetId);

        Observable<Void> setPacketIsOpen(long packageId, long amount);

        Observable<Void> addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message);
    }
}
