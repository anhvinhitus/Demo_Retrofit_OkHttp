package vn.com.vng.zalopay.data.repository.datasource;

import android.util.LruCache;

import rx.Observable;
import vn.com.vng.zalopay.data.api.ZaloPayIAPService;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class ZaloPayIAPFactory {

    final ZaloPayIAPService service;
    final User user;
    private LruCache<Long, GetMerchantUserInfoResponse> mCacheMerchantUser = new LruCache<>(10);

    public ZaloPayIAPFactory(ZaloPayIAPService service, User user) {
        this.service = service;
        this.user = user;
    }

    public Observable<GetMerchantUserInfoResponse> getMerchantUserInfo(long mAppJSId) {
        GetMerchantUserInfoResponse responseCache = mCacheMerchantUser.get(mAppJSId);
        if (responseCache != null) {
            return Observable.just(responseCache);
        } else {
            return service.getmerchantuserinfo(mAppJSId, user.uid, user.accesstoken)
                    .doOnNext(response -> mCacheMerchantUser.put(mAppJSId, response))
                    ;
        }
    }
}
