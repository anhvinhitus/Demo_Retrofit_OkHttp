package vn.com.vng.zalopay.withdraw.models;

import vn.com.zalopay.wallet.business.entity.atm.BankConfig;

/**
 * Created by longlv on 12/2/16.
 * *
 */

public class BankSupportWithdraw {
    public BankConfig mBankConfig;
    public boolean mIsMapped;

    public BankSupportWithdraw(BankConfig bankConfig, boolean isMapped) {
        mBankConfig = bankConfig;
        mIsMapped = isMapped;
    }
}
