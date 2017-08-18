package vn.com.zalopay.wallet.workflow.channelloader;

import vn.com.zalopay.wallet.helper.PaymentPermission;
import vn.com.zalopay.wallet.constants.TransactionType;

public class PaymentChannelLoader extends AbstractChannelLoader {
    public PaymentChannelLoader(long pAppId, String pUserId, long pAmount, long pBalance, @TransactionType int pTranstype) {
        super(pAppId, pUserId, pAmount, pBalance, pTranstype);
    }

    @Override
    protected void detectChannel() throws Exception {
        getChannelFromConfig();
        getMapBankAccount();
        getMapCard();
        if (PaymentPermission.allowLinkChannel()) {
            createLinkChannel();
        }
        source.onCompleted();
    }
}
