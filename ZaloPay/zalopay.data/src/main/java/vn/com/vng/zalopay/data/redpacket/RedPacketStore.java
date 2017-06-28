package vn.com.vng.zalopay.data.redpacket;

import android.support.annotation.Nullable;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.RedPacketStatusEntity;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.ListRedPacketStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by longlv on 13/07/2016.
 * Declaration for redPackage local storage, redPackage request service, redPackage repository
 */
public interface RedPacketStore {

    interface LocalStorage {

        //Update status when open red packet
        Void setPacketStatus(long packetId, long amount, int status, String messageStatus);

        //Update status when receive notification
        Void addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message);

        //Reload list package after receive recovery notification
        void updateListPackageStatus(@Nullable List<RedPacketStatusEntity> listPackageStatus);

        ReceivePackageGD getPacketStatus(long packetId);
    }

    interface RequestTPEService {
        @API_NAME(https = ZPEvents.API_V001_TPE_GETTRANSSTATUS, connector = ZPEvents.CONNECTOR_V001_TPE_GETTRANSSTATUS)
        @GET(Constants.TPE_API.GETTRANSSTATUS)
        Observable<PackageStatusResponse> getPackageStatus(@Query("appid") int appId, @Query("packageid") long packageID, @Query("zptransid") long zpTransID, @Query("userid") String userid, @Query("accesstoken") String accessToken, @Query("deviceid") String deviceid);
    }

    interface RequestService {
        @API_NAME(https = ZPEvents.API_REDPACKAGE_CREATEBUNDLEORDER, connector = ZPEvents.CONNECTOR_REDPACKAGE_CREATEBUNDLEORDER)
        @FormUrlEncoded
        @POST(Constants.REDPACKET_API.CREATEBUNDLEORDER)
        Observable<BundleOrderResponse> createBundleOrder(@Field("quantity") int quantity, @Field("totalluck") long totalLuck, @Field("amounteach") long amountEach, @Field("type") int type, @Field("sendzalopayid") String sendZaloPayID, @Field("accesstoken") String accessToken, @Field("sendmessage") String sendMessage);

        @API_NAME(https = ZPEvents.API_REDPACKAGE_SUBMITOPENPACKAGE, connector = ZPEvents.CONNECTOR_REDPACKAGE_SUBMITOPENPACKAGE)
        @FormUrlEncoded
        @POST(Constants.REDPACKET_API.SUBMITOPENPACKAGE)
        Observable<SubmitOpenPackageResponse> submitOpenPackage(@Field("packageid") long packageID, @Field("bundleid") long bundleID, @Field("revzalopayid") String revZaloPayID, @Field("accesstoken") String accessToken);

        @API_NAME(https = ZPEvents.API_REDPACKAGE_SUBMITTOSENDBUNDLEBYZALOPAYINFO, connector = ZPEvents.CONNECTOR_REDPACKAGE_SUBMITTOSENDBUNDLEBYZALOPAYINFO)
        @FormUrlEncoded
        @POST(Constants.REDPACKET_API.SUBMITTOSENDBUNDLEBYZALOPAYINFO)
        Observable<BaseResponse> submittosendbundlebyzalopayinfo(@Field("bundleid") long bundleID, @Field("zalopayoffriendlist") String friends, @Field("zalopayofsender") String sender, @Field("accesstoken") String accessToken);

        @API_NAME(https = ZPEvents.API_REDPACKAGE_GETLISTPACKAGESTATUS, connector = ZPEvents.CONNECTOR_REDPACKAGE_GETLISTPACKAGESTATUS)
        @GET(Constants.REDPACKET_API.GET_LIST_PACKAGE_STATUS)
        Observable<ListRedPacketStatusResponse> getListPackageStatus(@Query("listpackageid") String listpackageid, @Query("userid") String zalopayId, @Query("accesstoken") String accessToken);
    }

    /**
     * Interface for providing requests which relate with redPackage
     */
    interface Repository {
        Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage);

        Observable<Boolean> sendBundle(long bundleID, List<RedPacketUserEntity> entities);

        Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID);

        Observable<PackageStatus> getPackageStatus(long packageID, long zpTransID, String deviceId);

        Observable<ReceivePackageGD> getPacketStatus(String packetId);

        Observable<Void> setPacketStatus(long packageId, long amount, int status, String messageStatus);

        Observable<Void> addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message);

        Observable<Boolean> getListPackageStatus(List<Long> listPackageId);
    }
}
