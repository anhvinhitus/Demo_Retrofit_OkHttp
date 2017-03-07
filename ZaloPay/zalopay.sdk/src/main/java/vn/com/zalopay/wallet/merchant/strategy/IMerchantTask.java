package vn.com.zalopay.wallet.merchant.strategy;

import vn.com.zalopay.wallet.merchant.listener.IMerchantListener;

public interface IMerchantTask {
    void onTaskInProcess();

    void onPrepareTaskComplete();

    void onTaskError(String pErrorMess);

    void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);

    void setTaskListener(IMerchantListener pListener);
}
