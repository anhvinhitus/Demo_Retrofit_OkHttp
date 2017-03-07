package vn.com.zalopay.wallet.business.behavior.view.paymentfee;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.enumeration.EFeeCalType;
import vn.com.zalopay.wallet.utils.Log;

/***
 * withdraw fee
 */
public class CWithDrawCalculateFee implements ICalculateFee {
    private BankConfig mBankConfig;

    public CWithDrawCalculateFee(BankConfig pBankConfig) {
        this.mBankConfig = pBankConfig;
    }

    @Override
    public double calculateFee() {
        if (mBankConfig == null)
            return 0;

        double orderFee = 0;

        if (mBankConfig.feerate > 0)
            try {
                orderFee = mBankConfig.feerate * GlobalData.getPaymentInfo().amount;
            } catch (Exception e) {
                Log.e(this, e);
            }

        if (mBankConfig.minfee > 0) {
            if (mBankConfig.feecaltype == EFeeCalType.MAX)
                orderFee = (orderFee > mBankConfig.minfee) ? orderFee : mBankConfig.minfee;
            else if (mBankConfig.feecaltype == EFeeCalType.SUM) {
                orderFee += mBankConfig.minfee;
            }
        }

        return orderFee;
    }
}
