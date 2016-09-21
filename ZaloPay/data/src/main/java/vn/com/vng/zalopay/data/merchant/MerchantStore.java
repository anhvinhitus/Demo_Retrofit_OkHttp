package vn.com.vng.zalopay.data.merchant;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.data.api.response.ListMerchantUserInfoResponse;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */
public interface MerchantStore {
    interface LocalStorage {

    }

    interface RequestService {
        @GET("ummerchant/getmerchantuserinfo")
        Observable<GetMerchantUserInfoResponse> getmerchantuserinfo(@Query("appid") long appid,
                                                                    @Query("userid") String userid,
                                                                    @Query("accesstoken") String accesstoken);

        @GET("ummerchant/getlistmerchantuserinfo")
        Observable<ListMerchantUserInfoResponse> getlistmerchantuserinfo(@Query("appidlist") String appidlist,
                                                                         @Query("userid") String userid,
                                                                         @Query("accesstoken") String accesstoken);
    }


    interface Repository {
        Observable<MerchantUserInfo> getMerchantUserInfo(long appId);

        Observable<Boolean> getListMerchantUserInfo(String appIdList);
    }
}
