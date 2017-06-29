package vn.com.zalopay.wallet.event;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;

/**
 * Created by huuhoa on 6/29/17.
 * Payment information is ready to used
 */

public class SdkPaymentInfoReadyMessage {
    public AppInfo mAppInfo;
}
