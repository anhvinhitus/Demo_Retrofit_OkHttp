package vn.com.zalopay.wallet.interactor;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;

/**
 * Created by chucvv on 6/8/17.
 */

public interface IBankList {
    void clearCheckSum();
    void clearConfig();
    Observable<BankConfigResponse> getBankList(String appversion, long currentTime);
}
