package vn.com.zalopay.wallet.business.behavior.view.paymentfee;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.constants.FeeType;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;

/***
 * withdraw fee
 */
public class CWithDrawCalculateFee implements ICalculateFee {
    private BankConfig mBankConfig;

    public CWithDrawCalculateFee(BankConfig pBankConfig) {
        this.mBankConfig = pBankConfig;
    }

    @Override
    public double calculateFee(long amount) {
        if (mBankConfig == null)
            return 0;

        double orderFee = 0;

        if (mBankConfig.feerate > 0)
            try {
                orderFee = mBankConfig.feerate * amount;
            } catch (Exception e) {
                Log.e(this, e);
            }

        if (mBankConfig.minfee > 0) {
            switch (mBankConfig.feecaltype) {
                case FeeType.MAX:
                    orderFee = (orderFee > mBankConfig.minfee) ? orderFee : mBankConfig.minfee;
                    break;
                case FeeType.SUM:
                    orderFee += mBankConfig.minfee;
                    break;
            }
        }

        return orderFee;
    }
}
