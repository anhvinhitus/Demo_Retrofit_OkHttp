package vn.com.zalopay.wallet.workflow.channelloader;

import vn.com.zalopay.wallet.constants.TransactionType;

public class PaymentChannelLoader extends AbstractChannelLoader {
    public PaymentChannelLoader(long pAppId, String pUserId, long pAmount, long pBalance, @TransactionType int pTranstype) {
        super(pAppId, pUserId, pAmount, pBalance, pTranstype);
    }

    @Override
    protected void detectChannel() throws Exception {
        getMapBankAccount();
        getMapCard();
        getChannelFromConfig();
        source.onCompleted();
    }
}
