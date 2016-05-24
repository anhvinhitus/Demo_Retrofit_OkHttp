package vn.com.vng.zalopay.data.api.entity.mapper;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.domain.model.MerChantUserInfo;

/**
 * Created by AnhHieu on 5/24/16.
 */
@Singleton
public class ZaloPayIAPEntityDataMapper {

    @Inject
    public ZaloPayIAPEntityDataMapper() {
    }

    public MerChantUserInfo transform(GetMerchantUserInfoResponse response) {
        MerChantUserInfo ret = new MerChantUserInfo();
        ret.birthdate = response.birthdate;
        ret.displayname = response.displayname;
        ret.muid = response.muid;
        ret.usergender = response.usergender;
        ret.maccesstoken = response.maccesstoken;
        return ret;
    }
}
