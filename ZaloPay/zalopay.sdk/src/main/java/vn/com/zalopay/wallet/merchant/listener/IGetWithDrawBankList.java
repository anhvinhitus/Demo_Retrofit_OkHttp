package vn.com.zalopay.wallet.merchant.listener;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.atm.BankConfig;

public interface IGetWithDrawBankList {
    void onComplete(List<BankConfig> pBankConfigList);

    void onError(String pErrorMess);
}
