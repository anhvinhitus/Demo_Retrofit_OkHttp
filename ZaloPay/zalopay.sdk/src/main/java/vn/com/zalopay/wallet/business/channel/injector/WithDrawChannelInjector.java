package vn.com.zalopay.wallet.business.channel.injector;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
<<<<<<< HEAD
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.PaymentChannelStatus;
=======
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannelStatus;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
>>>>>>> 9fd9a35... [SDK] Apply app info v1
import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;
import vn.com.zalopay.wallet.utils.GsonUtils;

public class WithDrawChannelInjector extends BaseChannelInjector {
    protected ZPWOnGetChannelListener mGetChannelListener;

    @Override
    protected void detectChannel(ZPWOnGetChannelListener pListener) throws Exception {
        mGetChannelListener = pListener;
        //load mapped card from cached.
        loadMapCardFromCacheForWithDraw();
        //load bank account
        loadBankAccountFromCacheForWithDraw();
        //check where bank allow withdrawing.
        crossCheckWithDraw();
    }

    /***
     * check which map card type is allow from banklist
     * @throws Exception
     */
    protected void crossCheckWithDraw() throws Exception {
<<<<<<< HEAD
        for (DPaymentChannelView channelView : mChannelList) {
=======
        for (Iterator<PaymentChannel> iterator = mChannelList.iterator(); iterator.hasNext(); ) {
            PaymentChannel channelView = iterator.next();
>>>>>>> 9fd9a35... [SDK] Apply app info v1
            BankConfig bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(channelView.bankcode), BankConfig.class);
            if (bankConfig == null) {
                continue;
            }
            //withdraw fee count from bank config
            channelView.totalfee = bankConfig.calculateFee();
            if (!bankConfig.isAllowWithDraw()) {
                channelView.setStatus(PaymentChannelStatus.DISABLE);
            } else if (bankConfig.isBankMaintenence(BankFunctionCode.WITHDRAW)) {
                channelView.setStatus(PaymentChannelStatus.MAINTENANCE);
            }
            //check fee + amount <= balance
            else if (!GlobalData.isEnoughMoneyForTransaction((long) channelView.totalfee)) {
                channelView.setAllowByAmountAndFee(false);
            }
        }
        sortChannels();
        if (mGetChannelListener != null) {
            mGetChannelListener.onGetChannelComplete();
        }
    }
}
