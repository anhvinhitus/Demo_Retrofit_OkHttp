package vn.com.zalopay.wallet.business.channel.injector;

import vn.com.zalopay.wallet.constants.TransactionType;

public class WithDrawChannelLoader extends AbstractChannelLoader {
    public WithDrawChannelLoader(long pAppId, String pUserId, long pAmount, long pBalance, @TransactionType int pTranstype) {
        super(pAppId, pUserId, pAmount, pBalance, pTranstype);
    }

    @Override
    protected void detectChannel() throws Exception {
        getMapBankAccount();
        getMapCard();
        source.onCompleted();
    }
}
