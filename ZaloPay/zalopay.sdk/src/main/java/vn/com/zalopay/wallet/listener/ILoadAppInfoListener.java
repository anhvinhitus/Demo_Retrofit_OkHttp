package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;


public interface ILoadAppInfoListener {
    public void onProcessing();

    public void onSuccess();

    public void onError(AppInfoResponse message);
}
