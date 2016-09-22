package vn.com.vng.zalopay.data.merchant;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;
import vn.com.vng.zalopay.data.api.response.ListMerchantUserInfoResponse;
import vn.com.vng.zalopay.data.cache.model.MerchantUser;
import vn.com.vng.zalopay.data.util.ListStringUtil;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.domain.model.User;

import static vn.com.vng.zalopay.data.util.ListStringUtil.toLongArr;
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
                .flatMap(merchantUser -> {
                    Timber.d("getMerchantUserInfoLocal %s", merchantUser);
                    return merchantUser == null ? Observable.empty() : Observable.just(transform(merchantUser));
                });
    }

    @Override
    public Observable<Boolean> getListMerchantUserInfo(String appIdList) {
        Timber.d("getListMerchantUserInfo:  [%s] ", appIdList);

        return makeObservable(() -> {
            Timber.d("getListMerchantUserInfo begin");
            List<Long> appIds = ListStringUtil.toListLong(appIdList);
            boolean existIn = localStorage.existIn(appIds);
            Timber.d("getListMerchantUserInfo: appIds [%s] existIn [%s] ", appIds, existIn);
            return existIn;
        }).flatMap(existIn -> {
            return existIn ? Observable.just(Boolean.TRUE) : fetchListMerchant(appIdList);
        });
    }

    private Observable<Boolean> fetchListMerchant(String appIdList) {
        return requestService.getlistmerchantuserinfo(appIdList, user.zaloPayId, user.accesstoken)
                .doOnNext(response -> {
                    List<MerchantUser> entities = transform(response);
                    if (!Lists.isEmptyOrNull(entities)) {
                        localStorage.put(transform(response));
                    } else {
                        //clear
                       // localStorage.removeAll();
                    }
                })
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

    private List<MerchantUser> transform(ListMerchantUserInfoResponse response) {
        if (Lists.isEmptyOrNull(response.listmerchantuserinfo)) {
            return null;
        }
        List<MerchantUser> entities = new ArrayList<>();
        for (ListMerchantUserInfoResponse.ListMerchantUserInfo info : response.listmerchantuserinfo) {
            MerchantUser merchantUser = new MerchantUser(info.appid);
            merchantUser.setDisplayName(response.displayname);
            merchantUser.setBirthday(response.birthdate);
            merchantUser.setGender(response.usergender);
            merchantUser.setMUid(info.muid);
            merchantUser.setMAccessToken(info.maccesstoken);
            entities.add(merchantUser);
        }
        return entities;
    }

    private MerchantUser transform(GetMerchantUserInfoResponse response, long appId) {
        MerchantUser ret = new MerchantUser(appId);
        ret.setBirthday(response.birthdate);
        ret.setMUid(response.muid);
        ret.setMAccessToken(response.maccesstoken);
        ret.setDisplayName(response.displayname);
        ret.setGender(response.usergender);
        return ret;
    }

    private MerchantUserInfo transform(MerchantUser entity) {
        if (entity == null) {
            return null;
        }

        MerchantUserInfo ret = new MerchantUserInfo(entity.getAppid());
        ret.birthdate = entity.getBirthday();
        ret.displayname = entity.getDisplayName();
        ret.muid = entity.getMUid();
        ret.usergender = entity.getGender();
        ret.maccesstoken = entity.getMAccessToken();
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
