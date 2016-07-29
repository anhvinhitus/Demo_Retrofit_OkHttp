package vn.com.vng.zalopay.domain.repository;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;

/**
 * Created by AnhHieu on 5/24/16.
 */
public interface ZaloPayIAPRepository {
    Observable<MerchantUserInfo> getMerchantUserInfo(long appId);
}
