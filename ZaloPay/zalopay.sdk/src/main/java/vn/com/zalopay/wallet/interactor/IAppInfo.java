package vn.com.zalopay.wallet.interactor;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.TransactionType;

/**
 * Created by chucvv on 6/8/17.
 */

public interface IAppInfo {
    void setExpireTime(long appId, long expireTime);

    MiniPmcTransType getPmcTranstype(long pAppId, @TransactionType int transtype, boolean isBankAcount, String bankCode);

    Observable<AppInfo> loadAppInfo(long appid, @TransactionType int[] transtypes, String userid, String accesstoken, String appversion, long currentTime);
}
