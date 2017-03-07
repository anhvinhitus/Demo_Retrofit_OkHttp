package vn.com.zalopay.wallet.business.behavior.view.paymentfee;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.enumeration.EFeeCalType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.utils.Log;

/***
 * payment fee
 */
public class CPaymentCalculateFee implements ICalculateFee {
    private DPaymentChannel mChannel;

    public CPaymentCalculateFee(DPaymentChannel pChannel) {
        this.mChannel = pChannel;
    }

    @Override
    public double calculateFee() {
        if (mChannel == null)
            return 0;

        double orderFee = 0;

        if (mChannel.feerate > 0)
            try {
                orderFee = mChannel.feerate * GlobalData.getPaymentInfo().amount;
            } catch (Exception e) {
                Log.e(this, e);
            }

        if (mChannel.minfee > 0) {
            if (mChannel.feecaltype == EFeeCalType.MAX)
                orderFee = (orderFee > mChannel.minfee) ? orderFee : mChannel.minfee;
            else if (mChannel.feecaltype == EFeeCalType.SUM) {
                orderFee += mChannel.minfee;
            }
        }

        return orderFee;
    }
}
