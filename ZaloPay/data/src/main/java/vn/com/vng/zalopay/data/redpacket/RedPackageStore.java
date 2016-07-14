package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.SubmitOpenPackage;
import vn.com.vng.zalopay.domain.model.RedPackage;

/**
 * Created by longlv on 13/07/2016.
 * Declaration for redPackage local storage, redPackage request service, redPackage repository
 */
public interface RedPackageStore {

    interface LocalStorage {

        void putRedPackage(long bundleId, int quantity, long totalLuck, long amountEach, int type, String sendMessage);
        void updateRedPackage(long bundleId, int state);

        void putRedPackageItem(long packageId, long bundleId, String zpTransID, int state);

        Observable<List<RedPackage>> getAllRedPackage();
    }

    interface RequestService {
        @FormUrlEncoded
        @POST("redpackage/createBundleOrder")
        Observable<BundleOrderResponse> createBundleOrder(@Field("quantity") int quantity, @Field("totalLuck") long totalLuck, @Field("amountEach") long amountEach, @Field("type") int type, @Field("sendZaloPayID") String sendZaloPayID, @Field("accessToken") String accessToken, @Field("sendMessage") String sendMessage);

        @FormUrlEncoded
        @POST("redpackage/sendBundle")
        Observable<BaseResponse> sendBundle(@Field("bundleID") long bundleID, @Field("friendList") String friendList, @Field("sendZaloPayID") String sendZaloPayID, @Field("accessToken") String accessToken);

        @FormUrlEncoded
        @POST("redpackage/submitOpenPackage")
        Observable<SubmitOpenPackageResponse> submitOpenPackage(@Field("packageID") long packageID, @Field("bundleID") long bundleID, @Field("revZaloPayID") String revZaloPayID, @Field("accessToken") String accessToken);
    }

    /**
     * Interface for providing requests which relate with redPackage
     */
    interface Repository {
        Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage);

        Observable<Boolean> sendBundle(long bundleID, List<Long> friendList);

        Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID);
    }
}
