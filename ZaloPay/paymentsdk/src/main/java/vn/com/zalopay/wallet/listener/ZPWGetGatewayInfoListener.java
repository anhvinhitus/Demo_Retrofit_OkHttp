package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;

public interface ZPWGetGatewayInfoListener {
    void onProcessing();

    void onSuccess();

    void onError(DPlatformInfo pMessage);

    void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);
}
