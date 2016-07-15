package vn.com.vng.zalopay.data.redpacket;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.SubmitOpenPackage;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by longlv on 13/07/2016.
 * Implementation for RedPackageStore.Repository
 */
public class RedPackageRepositoryImpl implements RedPackageStore.Repository {

    public RedPackageStore.RequestService mRequestService;
    private RedPackageStore.LocalStorage mLocalStorage;
    public UserConfig userConfig;
    public User user;

    public RedPackageRepositoryImpl(RedPackageStore.RequestService requestService, RedPackageStore.LocalStorage localStorage, UserConfig userConfig, User user) {
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
        this.user = user;
        this.userConfig = userConfig;
    }

    @Override
    public Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage) {
        return mRequestService.createBundleOrder(quantity, totalLuck, amountEach, type, user.uid, user.accesstoken, sendMessage)
                .map(bundleOrderResponse -> new BundleOrder(bundleOrderResponse.getAppid(), bundleOrderResponse.getZptranstoken(), bundleOrderResponse.apptransid, bundleOrderResponse.appuser, bundleOrderResponse.apptime, bundleOrderResponse.embeddata, bundleOrderResponse.item, bundleOrderResponse.amount, bundleOrderResponse.description, bundleOrderResponse.payoption, bundleOrderResponse.mac, bundleOrderResponse.bundleID));
    }

    @Override
    public Observable<Boolean> sendBundle(long bundleID, List<Long> friendList) {
        String friendListStr = Strings.joinWithDelimiter("|", friendList);
        return mRequestService.sendBundle(bundleID, friendListStr, user.uid, user.accesstoken)
                .map(BaseResponse::isSuccessfulResponse);
                //.doOnNext(redPackageResponse -> mLocalStorage.updateRedPackage(bundleID, RedPackage.RedPackageState.SENT.getValue()));
    }

    @Override
    public Observable<SubmitOpenPackage> submitOpenPackage(long packageID, long bundleID) {
        return mRequestService.submitOpenPackage(packageID, bundleID, user.uid, user.accesstoken)
                .map(redPackageResponse -> new SubmitOpenPackage(bundleID, packageID, redPackageResponse.zpTransID));
    }
}
