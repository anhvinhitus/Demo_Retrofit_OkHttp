package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.RedPacketAppInfoResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.BundleGD;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePacketSummaryDB;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleSummaryDB;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;

/**
 * Created by longlv on 13/07/2016.
 * Declaration for redPackage local storage, redPackage request service, redPackage repository
 */
public interface RedPacketStore {

    interface LocalStorage {
        void putBundle(List<BundleGD> bundleGDs);
        //update time that get PackageInBundle from server
        void updateLastTimeGetPackage(long bundleId);
        BundleGD getBundle(long bundleId);

        void putSentBundleSummary(SentBundleSummaryDB sentBundleSummaryDB);
        Observable<GetSentBundle> getSentBundleSummary();

        void putReceivePacketSummary(ReceivePacketSummaryDB receivePacketSummaryDB);
        Observable<GetReceivePacket> getReceivePacketSummary();

        void putSentBundle(List<SentBundleGD> sentBundle);
        Observable<List<SentBundle>> getSentBundle(long timeCreate, int limit);
        Boolean isHaveSentBundleInDb(long createTime, int count);
        Void setBundleStatus(long bundleId, int status);

        void putReceivePackages(List<ReceivePackageGD> receiveBundleGDs);
        Observable<List<ReceivePackage>> getReceiveBundle(long timeCreate, int limit);
        Boolean isHaveReceivePacketInDb(long createTime, int count);

        void putPackageInBundle(List<PackageInBundleGD> packageInBundleGDs);
        Observable<List<PackageInBundle>> getPackageInBundle(long bundleID);

        void putRedPacketAppInfo(RedPacketAppInfo redPacketAppInfo);
        RedPacketAppInfo getRedPacketAppInfo();

        ReceivePackageGD getPacketStatus(long packetId);
        Void setPacketStatus(long packetId, long amount, int status, String messageStatus);

        Void addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message);

        ReceivePackage getReceivedPacket(long packetId);
    }

    interface RequestTPEService {
        @GET(Constants.TPE_API.GETTRANSSTATUS)
        Observable<PackageStatusResponse> getPackageStatus(@Query("appid") int appId, @Query("packageid") long packageID, @Query("zptransid") long zpTransID, @Query("userid") String userid, @Query("accesstoken") String accessToken, @Query("deviceid") String deviceid);
    }

    interface RequestService {
        @FormUrlEncoded
        @POST(Constants.REDPACKET_API.CREATEBUNDLEORDER)
        Observable<BundleOrderResponse> createBundleOrder(@Field("quantity") int quantity, @Field("totalluck") long totalLuck, @Field("amounteach") long amountEach, @Field("type") int type, @Field("sendzalopayid") String sendZaloPayID, @Field("accesstoken") String accessToken, @Field("sendmessage") String sendMessage);

        @FormUrlEncoded
        @POST(Constants.REDPACKET_API.SUBMITTOSENDBUNDLE)
        Observable<BaseResponse> sendBundle(@Field("bundleid") long bundleID, @Field("friendlist") String friendList, @Field("sendzalopayid") String sendZaloPayID, @Field("accesstoken") String accessToken);

        @FormUrlEncoded
        @POST(Constants.REDPACKET_API.SUBMITOPENPACKAGE)
        Observable<SubmitOpenPackageResponse> submitOpenPackage(@Field("packageid") long packageID, @Field("bundleid") long bundleID, @Field("revzalopayid") String revZaloPayID, @Field("accesstoken") String accessToken);

        @GET(Constants.REDPACKET_API.GETSENTBUNDLELIST)
        Observable<SentBundleListResponse> getSentBundleList(@Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("zalopayid") String zalopayid, @Query("accesstoken") String accesstoken);

        @GET(Constants.REDPACKET_API.GETREVPACKAGELIST)
        Observable<GetReceivePackageResponse> getReceivedPackageList(@Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("zalopayid") String zalopayid, @Query("accesstoken") String accesstoken);

        @GET(Constants.REDPACKET_API.GETPACKAGESINBUNDLE)
        Observable<SentPackageInBundleResponse> getPackageInBundleList(@Query("bundleid") long bundleid, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("zalopayid") String zalopayid, @Query("accesstoken") String accesstoken);

        @GET(Constants.REDPACKET_API.GETAPPINFO)
        Observable<RedPacketAppInfoResponse> getAppInfo(@Query("checksum") String checksum, @Query("userid") String zalopayid, @Query("accesstoken") String accesstoken);
    }

    /**
     * Interface for providing requests which relate with redPackage
     */
    interface Repository {
        Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage);

        Observable<Boolean> sendBundle(long bundleID, List<Long> friendList);

        Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID);

        Observable<PackageStatus> getpackagestatus(long packageID, long zpTransID, String deviceId);

        Observable<Void> setBundleStatus(long bundleId, int status);

        Observable<GetSentBundle> getSentBundleListServer(long timestamp, int count, int order);

        Observable<GetSentBundle> getSentBundleList(long timeStamp, int count);

        Observable<GetReceivePacket> getReceivedPackagesServer(long timestamp, int count, int order);

        Observable<GetReceivePacket> getReceivePacketList(long timeStamp, int count);

        Observable<ReceivePackage> getReceivedPacket(long packetId);

        Observable<Boolean> getAllPacketInBundleServer(long bundleId);

        Observable<List<PackageInBundle>> getPacketsInBundle(long bundleId);

        Observable<ReceivePackageGD> getPacketStatus(String packetId);

        Observable<Void> setPacketStatus(long packageId, long amount, int status, String messageStatus);

        Observable<Void> addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message);

        Observable<RedPacketAppInfo> getAppInfoServer(String checksum);

        Observable<RedPacketAppInfo> getRedPacketAppInfo();
    }
}
