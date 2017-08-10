package vn.com.zalopay.wallet.business.behavior.view.paymentfee;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.FeeType;

/***
 * calculate fee for pay transtype
 */
public class PayFeeImpl implements ICalculateFee {
    private MiniPmcTransType mChannel;

    public PayFeeImpl(MiniPmcTransType pChannel) {
        this.mChannel = pChannel;
    }

    @Override
    public double calculateFee(long amount) {
        if (mChannel == null) {
            return 0;
        }
        double orderFee = 0;
        if (mChannel.feerate > 0) {
            orderFee = mChannel.feerate * amount;
        }
        if (mChannel.minfee > 0) {
            switch (mChannel.feecaltype) {
                case FeeType.MAX:
                    orderFee = (orderFee > mChannel.minfee) ? orderFee : mChannel.minfee;
                    break;
                case FeeType.SUM:
                    orderFee += mChannel.minfee;
                    break;
            }
        }
        return orderFee;
    }
}
