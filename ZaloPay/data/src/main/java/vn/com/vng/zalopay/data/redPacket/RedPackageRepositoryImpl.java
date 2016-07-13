package vn.com.vng.zalopay.data.redPacket;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.RedPackage;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by longlv on 13/07/2016.
 * Implementation for RedPackageStore.Repository
 */
public class RedPackageRepositoryImpl implements RedPackageStore.Repository {

    public RedPackageStore.RequestService requestService;
    public UserConfig userConfig;
    public User user;

    public RedPackageRepositoryImpl(RedPackageStore.RequestService requestService, UserConfig userConfig, User user) {
        this.requestService = requestService;
        this.user = user;
        this.userConfig = userConfig;
    }

    @Override
    public Observable<BundleOrder> createBundleOrder(int quantity, long totalLuck, long amountEach, int type, String sendMessage) {
        return requestService.createBundleOrder(quantity, totalLuck, amountEach, type, user.uid, user.accesstoken, sendMessage).map(bundleOrderResponse -> new BundleOrder(bundleOrderResponse.getAppid(), bundleOrderResponse.getZptranstoken(), bundleOrderResponse.apptransid, bundleOrderResponse.appuser, bundleOrderResponse.apptime, bundleOrderResponse.embeddata, bundleOrderResponse.item, bundleOrderResponse.amount, bundleOrderResponse.description, bundleOrderResponse.payoption, bundleOrderResponse.mac, bundleOrderResponse.bundleID));
    }

    @Override
    public Observable<Boolean> sendBundle(long bundleID, List<Long> friendList) {
        String friendListStr = convertListToString(friendList);
        return requestService.sendBundle(bundleID, friendListStr, user.uid, user.accesstoken).map(BaseResponse::isSuccessfulResponse);
    }

    @Override
    public Observable<RedPackage> submitOpenPackage(long packageID, long bundleID) {
        return requestService.submitOpenPackage(packageID, bundleID, user.uid, user.accesstoken).map(redPackageResponse -> new RedPackage(redPackageResponse.zpTransID));
    }

    private String convertListToString(List<Long> friendList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (friendList == null || friendList.size() <= 0) {
            return stringBuilder.toString();
        }
        String suffix = "|";
        for (long zaloId : friendList) {
            if (zaloId > 0) {
                stringBuilder.append(zaloId);
                stringBuilder.append(suffix);
            }
        }
        String result = stringBuilder.toString();
        if (result.endsWith(suffix)) {
            return stringBuilder.toString().substring(0, result.length() - 1);
        }
        return result;
    }
}
