package vn.com.vng.zalopay.data.RedPacket;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.BundleOrder;
import vn.com.vng.zalopay.domain.model.RedPackage;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by longlv on 13/07/2016.
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
    public Observable<BundleOrder> createbundleorder(int quantity, long totalLuck, long amountEach, int type, String sendMessage) {
        return requestService.createbundleorder(quantity, totalLuck, amountEach, type, user.uid, user.accesstoken, sendMessage).map(bundleOrderResponse -> {
            BundleOrder bundleOrder = new BundleOrder(bundleOrderResponse.getAppid(), bundleOrderResponse.getZptranstoken(), bundleOrderResponse.apptransid, bundleOrderResponse.appuser, bundleOrderResponse.apptime, bundleOrderResponse.embeddata, bundleOrderResponse.item, bundleOrderResponse.amount, bundleOrderResponse.description, bundleOrderResponse.payoption, bundleOrderResponse.mac, bundleOrderResponse.bundleID);
            return bundleOrder;
        });
    }

    @Override
    public Observable<Boolean> sendbundle(long bundleID, List<Long> friendList) {
        String friendListStr = convertListToString(friendList);
        return requestService.sendbundle(bundleID, friendListStr, user.uid, user.accesstoken).map(baseResponse -> {
            if (baseResponse.isSuccessfulResponse()) {
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    public Observable<RedPackage> submitopenpackage(long packageID, long bundleID) {
        return requestService.submitopenpackage(packageID, bundleID, user.uid, user.accesstoken).map(redPackageResponse -> {
            RedPackage redPackage = new RedPackage(redPackageResponse.zpTransID);
            return redPackage;
        });
    }

    private String suffix = "|";
    private String convertListToString(List<Long> friendList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (friendList == null || friendList.size() <= 0) {
            return stringBuilder.toString();
        }
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
