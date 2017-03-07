package vn.com.zalopay.wallet.business.channel.injector;

import vn.com.zalopay.wallet.listener.ZPWOnGetChannelListener;

public class TranferChannelInjector extends BaseChannelInjector {
    @Override
    protected void detectChannel(ZPWOnGetChannelListener pListener) throws Exception {

        try {
            getMapBankAccount();
            getMapCard();
            getChannelFromConfig();
            sortChannels();
            if (pListener != null) {
                pListener.onGetChannelComplete();
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
}
