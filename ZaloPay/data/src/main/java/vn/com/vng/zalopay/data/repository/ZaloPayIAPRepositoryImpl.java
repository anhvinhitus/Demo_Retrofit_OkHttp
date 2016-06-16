package vn.com.vng.zalopay.data.repository;

import rx.Observable;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.data.repository.datasource.ZaloPayIAPFactory;
import vn.com.vng.zalopay.domain.model.MerChantUserInfo;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class ZaloPayIAPRepositoryImpl implements ZaloPayIAPRepository {

    final ZaloPayIAPFactory zaloPayIAPFactory;

    public ZaloPayIAPRepositoryImpl(ZaloPayIAPFactory factory) {
        this.zaloPayIAPFactory = factory;
    }

    @Override
    public Observable<MerChantUserInfo> getMerchantUserInfo(long appId) {
        return zaloPayIAPFactory.getMerchantUserInfo(appId).map(this::transform);
    }

    @Override
    public Observable<Boolean> verifyMerchantAccessToken(String mUid, String token) {
        return zaloPayIAPFactory.verifyMerchantAccessToken(mUid, token).map(baseResponse -> Boolean.TRUE);
    }

    private MerChantUserInfo transform(GetMerchantUserInfoResponse response) {
        MerChantUserInfo ret = new MerChantUserInfo();
        ret.birthdate = response.birthdate;
        ret.displayname = response.displayname;
        ret.muid = response.muid;
        ret.usergender = response.usergender;
        ret.maccesstoken = response.maccesstoken;
        return ret;
    }
}
