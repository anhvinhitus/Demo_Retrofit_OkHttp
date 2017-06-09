package vn.com.zalopay.wallet.interactor;

import java.util.List;

import rx.Observable;
import rx.Observer;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by chucvv on 6/8/17.
 */

public interface IBankList {
    void clearCheckSum();

    void clearConfig();

    String getBankPrefix();

    Observable<List<ZPCard>> getSupportCards(String appVersion, long currentTime);

    Observable<BankConfigResponse> getBankList(String appversion, long currentTime);
}
