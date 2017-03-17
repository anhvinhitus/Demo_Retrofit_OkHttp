package vn.com.vng.zalopay.data.merchant;

import java.util.Collection;
import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.data.api.response.ListMUIResponse;
import vn.com.vng.zalopay.data.cache.model.MerchantUser;
import vn.com.vng.zalopay.data.net.adapter.API_NAME;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */
public interface MerchantStore {
    interface LocalStorage {
        void put(MerchantUser entity);

        void put(List<MerchantUser> entities);

        MerchantUser get(long appId);

        void removeAll();

        boolean existIn(Collection<Long> appIds);

        List<Long> notExistInDb(List<Long> appIds);
    }

    interface RequestService {
        @API_NAME(ZPEvents.CONNECTOR_UMMERCHANT_GETMERCHANTUSERINFO)
        @GET(Constants.UM_API.GETMERCHANTUSERINFO)
        @Headers({Constants.HEADER_EVENT + ZPEvents.CONNECTOR_UMMERCHANT_GETMERCHANTUSERINFO})
        Observable<GetMerchantUserInfoResponse> getmerchantuserinfo(@Query("appid") long appid,
                                                                    @Query("userid") String userid,
                                                                    @Query("accesstoken") String accesstoken);

        @API_NAME(ZPEvents.CONNECTOR_UMMERCHANT_GETLISTMERCHANTUSERINFO)
        @GET(Constants.UM_API.GETLISTMERCHANTUSERINFO)
        @Headers({Constants.HEADER_EVENT + ZPEvents.CONNECTOR_UMMERCHANT_GETLISTMERCHANTUSERINFO})
        Observable<ListMUIResponse> getlistmerchantuserinfo(@Query("appidlist") String appidlist,
                                                            @Query("userid") String userid,
                                                            @Query("accesstoken") String accesstoken);
    }


    interface Repository {
        Observable<MerchantUserInfo> getMerchantUserInfo(long appId);

        Observable<Boolean> getListMerchantUserInfo(List<Long> appIds);

        Observable<Boolean> removeAll();
    }
}