package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;

public interface IMoveToChannel {
    void moveToChannel(DPaymentChannelView pChannel);
}
