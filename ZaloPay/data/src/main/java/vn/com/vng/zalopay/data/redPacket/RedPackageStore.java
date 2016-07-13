package vn.com.vng.zalopay.data.redPacket;

import java.util.List;

import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpackage.RedPackageResponse;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.RedPackage;

/**
 * Created by longlv on 13/07/2016.
 * Declaration for redPackage local storage, redPackage request service, redPackage repository
 */
public interface RedPackageStore {

    interface LocalStorage {
    }

    interface RequestService {
        @FormUrlEncoded
        @POST("redpackage/createBundleOrder")
        Observable<BundleOrderResponse> createBundleOrder(@Query("quantity") int quantity, @Query("totalLuck") long totalLuck, @Query("amountEach") long amountEach, @Query("type") int type, @Query("sendZaloPayID") String sendZaloPayID, @Query("accessToken") String accessToken, @Query("sendMessage") String sendMessage);

        @FormUrlEncoded
        @POST("redpackage/sendBundle")
        Observable<BaseResponse> sendBundle(@Query("bundleID") long bundleID, @Query("friendList") String friendList, @Query("sendZaloPayID") String sendZaloPayID, @Query("accessToken") String accessToken);

        @FormUrlEncoded
        @POST("redpackage/submitOpenPackage")
        Observable<RedPackageResponse> submitOpenPackage(@Query("packageID") long packageID, @Query("bundleID") long bundleID, @Query("revZaloPayID") String revZaloPayID, @Query("accessToken") String accessToken);
    }

    /**
     * Interface for providing requests which relate with redPackage
     */
    interface Repository {
        Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage);

        Observable<Boolean> sendBundle(long bundleID, List<Long> friendList);

        Observable<RedPackage> submitOpenPackage(long packageID, long bundleID);
    }
}
