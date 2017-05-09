package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;


public interface ILoadAppInfoListener {
    void onProcessing();

    void onSuccess();

<<<<<<< HEAD
    void onError(DAppInfoResponse message);
=======
    public void onError(AppInfoResponse message);
>>>>>>> 9fd9a35... [SDK] Apply app info v1
}
