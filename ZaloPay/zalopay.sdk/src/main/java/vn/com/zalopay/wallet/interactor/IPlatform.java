package vn.com.zalopay.wallet.interactor;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfo;

/**
 * Created by chucvv on 6/7/17.
 */

public interface IPlatform {
    Observable<DAppInfo> loadAppInfo(String appid, String userid, String accesstoken, long currentTime);

    Observable<BankConfigResponse> getBankList(String platform, String appversion, long currentTime);
}
