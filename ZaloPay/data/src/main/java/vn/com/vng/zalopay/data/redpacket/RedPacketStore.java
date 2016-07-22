package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;

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

        void putReceivePackages(List<ReceivePackageGD> receiveBundleGDs);
        Observable<List<ReceivePackage>> getReceiveBundle();
        Observable<List<ReceivePackage>> getReceiveBundle(int pageIndex, int limit);
        Observable<ReceivePackage> getReceiveBundle(long bundleID);

        void putPackageInBundle(List<PackageInBundleGD> packageInBundleGDs);
        Observable<List<PackageInBundle>> getPackageInBundle();
        Observable<List<PackageInBundle>> getPackageInBundle(int pageIndex, int limit);
        Observable<PackageInBundle> getPackageInBundle(long bundleID);

        Boolean isPacketOpen(long packetId);

        Void setPacketIsOpen(long packetId);

        Void addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message);
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
        @POST("redpackage/getsentbundlelist")
        Observable<SentBundleListResponse> getSentBundleList(@Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zalopayid") String zalopayid, @Field("accesstoken") String accesstoken);

        @FormUrlEncoded
        @POST("redpackage/getrevpackagelist")
        Observable<GetReceivePackageResponse> getReceivedPackageList(@Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zalopayid") String zalopayid, @Field("accesstoken") String accesstoken);

        @FormUrlEncoded
        @POST("redpackage/getpackageinbundlelist")
        Observable<SentPackageInBundleResponse> getPackageInBundleList(@Field("bundleid") long bundleid, @Field("timestamp") long timestamp, @Field("count") int count, @Field("order") int order, @Field("zalopayid") String zalopayid, @Field("accesstoken") String accesstoken);
    }

    /**
     * Interface for providing requests which relate with redPackage
     */
    interface Repository {
        Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage);

        Observable<Boolean> sendBundle(long bundleID, List<Long> friendList);

        Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID);

        Observable<PackageStatus> getpackagestatus(long packageID, long zpTransID, String deviceId);

        Observable<GetSentBundle> getSentBundleList(long timestamp, int count, int order);

        Observable<GetReceivePacket> getReceivedPackageList(long timestamp, int count, int order);

        Observable<List<PackageInBundle>> getPackageInBundleList(long bundleID, long timestamp, int count, int order);

        Observable<Boolean> isPacketOpen(String packetId);

        Observable<Void> setPacketIsOpen(long packageId);

        Observable<Void> addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message);
    }
}
