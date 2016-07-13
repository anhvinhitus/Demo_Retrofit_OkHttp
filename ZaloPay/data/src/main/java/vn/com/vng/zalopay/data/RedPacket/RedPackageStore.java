package vn.com.vng.zalopay.data.RedPacket;

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
 */
public interface RedPackageStore {

    interface RequestService {
        @FormUrlEncoded
        @POST("um/createbundleorder")
        Observable<BundleOrderResponse> createbundleorder(@Query("quantity") int quantity, @Query("totalLuck") long totalLuck, @Query("amountEach") long amountEach, @Query("type") int type, @Query("sendZaloPayID") String sendZaloPayID, @Query("accessToken") String accessToken, @Query("sendMessage") String sendMessage);

        @FormUrlEncoded
        @POST("um/sendbundle")
        Observable<BaseResponse> sendbundle(@Query("bundleID") long bundleID, @Query("friendList") String friendList, @Query("sendZaloPayID") String sendZaloPayID, @Query("accessToken") String accessToken);

        @FormUrlEncoded
        @POST("um/submitopenpackage")
        Observable<RedPackageResponse> submitopenpackage(@Query("packageID") long packageID, @Query("bundleID") long bundleID, @Query("revZaloPayID") String revZaloPayID, @Query("accessToken") String accessToken);
    }

    /**
     * Interface for providing up-to-date balance information to outer layers
     */
    interface Repository {
        Observable<BundleOrder> createbundleorder(int quantity, long totalLuck, long amountEach, int type, String sendMessage);

        Observable<Boolean> sendbundle(long bundleID, List<Long> friendList);

        Observable<RedPackage> submitopenpackage(long packageID, long bundleID);
    }
}
