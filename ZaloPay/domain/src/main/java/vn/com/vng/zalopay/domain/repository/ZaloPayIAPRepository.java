package vn.com.vng.zalopay.domain.repository;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.MerChantUserInfo;

/**
 * Created by AnhHieu on 5/24/16.
 */
public interface ZaloPayIAPRepository {
    Observable<MerChantUserInfo> getMerchantUserInfo(long appId);

    Observable<Boolean> verifyMerchantAccessToken(String mUid, String token);

    Observable<Boolean> updateTransaction();

}
