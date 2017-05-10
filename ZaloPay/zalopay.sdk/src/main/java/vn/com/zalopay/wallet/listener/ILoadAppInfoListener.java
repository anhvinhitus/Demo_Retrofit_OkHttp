package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;


public interface ILoadAppInfoListener {
    void onProcessing();

    void onSuccess();

    void onError(AppInfoResponse message);
}
