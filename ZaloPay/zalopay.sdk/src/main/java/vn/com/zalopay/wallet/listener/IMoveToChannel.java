package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;

public interface IMoveToChannel {
    void moveToChannel(PaymentChannel pChannel);
}
