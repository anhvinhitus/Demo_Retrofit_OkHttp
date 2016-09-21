package vn.com.vng.zalopay.data.merchant;

import rx.Observable;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */

public class MerchantRepository implements MerchantStore.Repository {

    private final MerchantStore.LocalStorage localStorage;
    private final MerchantStore.RequestService requestService;
    private final User user;

    public MerchantRepository(MerchantStore.LocalStorage localStorage, MerchantStore.RequestService requestService, User user) {
        this.localStorage = localStorage;
        this.requestService = requestService;
        this.user = user;
    }

    @Override
    public Observable<MerchantUserInfo> getMerchantUserInfo(long appId) {
        return requestService.getmerchantuserinfo(appId, user.zaloPayId, user.accesstoken)
                .map(this::transform);
    }

    @Override
    public Observable<Boolean> getListMerchantUserInfo(String appIdList) {
        return requestService.getlistmerchantuserinfo(appIdList, user.zaloPayId, user.accesstoken)
                .map(response -> Boolean.TRUE)
                ;
    }

    private MerchantUserInfo transform(GetMerchantUserInfoResponse response) {
        MerchantUserInfo ret = new MerchantUserInfo();
        ret.birthdate = response.birthdate;
        ret.displayname = response.displayname;
        ret.muid = response.muid;
        ret.usergender = response.usergender;
        ret.maccesstoken = response.maccesstoken;
        return ret;
    }
}
