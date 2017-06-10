package vn.com.zalopay.wallet.business.channel.injector;

import java.util.Iterator;

import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.PaymentChannelStatus;
import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

public class WithDrawChannelInjector extends BaseChannelInjector {
    protected ZPWOnGetChannelListener mGetChannelListener;

    public WithDrawChannelInjector(PaymentInfoHelper paymentInfoHelper) {
        super(paymentInfoHelper);
    }

    @Override
    protected void detectChannel(ZPWOnGetChannelListener pListener) throws Exception {
        mGetChannelListener = pListener;
        getMapBankAccount();
        getMapCard();
        crossCheckWithDraw();
    }

    /***
     * check which map card type is allow from banklist
     * @throws Exception
     */
    protected void crossCheckWithDraw() throws Exception {
        for (Iterator<PaymentChannel> iterator = mChannelList.iterator(); iterator.hasNext(); ) {
            PaymentChannel channelView = iterator.next();
            BankConfig bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(channelView.bankcode), BankConfig.class);
            if (bankConfig == null) {
                continue;
            }
            if (!bankConfig.isWithDrawAllow()) {
                channelView.setStatus(PaymentChannelStatus.DISABLE);
            } else if (bankConfig.isBankMaintenence(BankFunctionCode.WITHDRAW)) {
                channelView.setStatus(PaymentChannelStatus.MAINTENANCE);
            }

            //check fee + amount <= balance
            long balance = mPaymentInfoHelper.getBalance();
            double amount_total = mPaymentInfoHelper.getAmount() + channelView.totalfee;
            if (balance < amount_total) {
                channelView.setAllowByAmountAndFee(false);
            }
        }
        sortChannels();
        if (mGetChannelListener != null) {
            mGetChannelListener.onGetChannelComplete();
        }
    }
}
