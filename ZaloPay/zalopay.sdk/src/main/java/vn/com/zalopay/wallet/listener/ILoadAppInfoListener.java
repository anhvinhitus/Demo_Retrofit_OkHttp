package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfoResponse;


public interface ILoadAppInfoListener {
    public void onProcessing();

    public void onSuccess();

    public void onError(DAppInfoResponse message);
}
