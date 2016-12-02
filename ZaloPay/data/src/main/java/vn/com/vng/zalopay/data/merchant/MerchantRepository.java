package vn.com.vng.zalopay.data.merchant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.data.api.response.ListMUIResponse;
import vn.com.vng.zalopay.data.cache.model.MerchantUser;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.domain.model.User;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

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
        return Observable.concat(getMerchantUserInfoLocal(appId), getMerchantUserInfoCloud(appId))
                .first();
    }

    private Observable<MerchantUserInfo> getMerchantUserInfoCloud(long appId) {
        return requestService.getmerchantuserinfo(appId, user.zaloPayId, user.accesstoken)
                .doOnNext(response -> localStorage.put(transform(response, appId)))
                .map(this::transform);
    }

    private Observable<MerchantUserInfo> getMerchantUserInfoLocal(long appId) {
        return makeObservable(() -> localStorage.get(appId))
                .filter(merchantUser -> merchantUser != null)
                .map(this::transform);
    }

    private Observable<Boolean> fetchListMerchantUserInfo(String appIdList) {
        Timber.d("fetchListMerchantUserInfo: appIdList %s", appIdList);
        return requestService.getlistmerchantuserinfo(appIdList, user.zaloPayId, user.accesstoken)
                .doOnNext(response -> {
                    List<MerchantUser> entities = transform(response);
                    Timber.d("fetchListMerchantUserInfo  %s", entities.size());
                    if (!Lists.isEmptyOrNull(entities)) {
                        localStorage.put(entities);
                    }
                })
                .map(response -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> getListMerchantUserInfo(List<Long> appIds) {
        return makeObservable(() -> localStorage.notExistInDb(appIds))
                .filter(longs -> !Lists.isEmptyOrNull(longs))
                .flatMap(longs -> fetchListMerchantUserInfo(Strings.joinWithDelimiter(",", longs)));
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

    private List<MerchantUser> transform(ListMUIResponse response) {
        if (Lists.isEmptyOrNull(response.listmerchantuserinfo)) {
            return Collections.emptyList();
        }
        List<MerchantUser> entities = new ArrayList<>();
        for (ListMUIResponse.MerchantUserSubInfo info : response.listmerchantuserinfo) {
            MerchantUser merchantUser = new MerchantUser(info.appid);
            merchantUser.mUid = (info.muid);
            merchantUser.mAccessToken = (info.maccesstoken);
            merchantUser.displayName = (response.displayname);
            merchantUser.birthday = (response.birthdate);
            merchantUser.gender = (response.usergender);
            entities.add(merchantUser);
        }
        return entities;
    }

    private MerchantUser transform(GetMerchantUserInfoResponse response, long appId) {
        MerchantUser ret = new MerchantUser(appId);
        ret.birthday = (response.birthdate);
        ret.mUid = (response.muid);
        ret.mAccessToken = (response.maccesstoken);
        ret.displayName = (response.displayname);
        ret.gender = (response.usergender);
        return ret;
    }

    private MerchantUserInfo transform(MerchantUser entity) {
        if (entity == null) {
            return null;
        }

        MerchantUserInfo ret = new MerchantUserInfo(entity.appid);
        ret.birthdate = entity.birthday;
        ret.displayname = entity.displayName;
        ret.muid = entity.mUid;
        ret.usergender = entity.gender;
        ret.maccesstoken = entity.mAccessToken;
        return ret;
    }

    @Override
    public Observable<Boolean> removeAll() {
        return makeObservable(() -> {
            localStorage.removeAll();
            return Boolean.TRUE;
        });
    }
}