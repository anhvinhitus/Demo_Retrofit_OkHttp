package vn.com.zalopay.wallet.interactor;

import java.util.List;
import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;

/**
 * Created by chucvv on 6/8/17.
 */

public interface IBank {
    void clearCheckSum();

    void clearConfig();

    void resetExpireTime();

    void setPaymentBank(String userId, String cardKey);

    String getPaymentBank(String userId);

    Map<String, String> getBankPrefix();

    BankConfig getBankConfig(String bankCode);

    Observable<List<BankConfig>> getWithdrawBanks(String appVersion, long currentTime);

    Observable<List<ZPBank>> getSupportBanks(String appVersion, long currentTime);

    Observable<BankConfigResponse> getBankList(String appversion, long currentTime);

    String getBankCodeList();
}
