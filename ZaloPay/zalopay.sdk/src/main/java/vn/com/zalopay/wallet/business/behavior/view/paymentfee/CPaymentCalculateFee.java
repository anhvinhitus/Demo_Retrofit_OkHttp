package vn.com.zalopay.wallet.business.behavior.view.paymentfee;

import vn.com.zalopay.wallet.business.data.GlobalData;
<<<<<<< HEAD
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.constants.FeeType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
=======
import vn.com.zalopay.wallet.business.entity.enumeration.EFeeCalType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.utils.Log;
>>>>>>> 9fd9a35... [SDK] Apply app info v1

/***
 * payment fee
 */
public class CPaymentCalculateFee implements ICalculateFee {
    private MiniPmcTransType mChannel;

    public CPaymentCalculateFee(MiniPmcTransType pChannel) {
        this.mChannel = pChannel;
    }

    @Override
    public double calculateFee() {
        if (mChannel == null) {
            return 0;
        }
        double orderFee = 0;
        if (mChannel.feerate > 0) {
            try {
                orderFee = mChannel.feerate * GlobalData.getPaymentInfo().amount;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (mChannel.minfee > 0) {
<<<<<<< HEAD
            switch (mChannel.feecaltype) {
                case FeeType.MAX:
                    orderFee = (orderFee > mChannel.minfee) ? orderFee : mChannel.minfee;
                    break;
                case FeeType.SUM:
                    orderFee += mChannel.minfee;
                    break;
=======
            if (mChannel.feecaltype == EFeeCalType.MAX) {
                orderFee = (orderFee > mChannel.minfee) ? orderFee : mChannel.minfee;
            } else if (mChannel.feecaltype == EFeeCalType.SUM) {
                orderFee += mChannel.minfee;
>>>>>>> 9fd9a35... [SDK] Apply app info v1
            }
        }
        return orderFee;
    }
}
