package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;

public interface ZPWGetGatewayInfoListener {
    void onProcessing();

    void onSuccess();

    void onError(PlatformInfoResponse pMessage);

    void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);
}
