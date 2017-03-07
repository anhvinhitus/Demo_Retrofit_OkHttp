package vn.com.zalopay.wallet.merchant.listener;

public interface IMerchantListener {
    void onProcess();

    void onError(String pErrorMess);

    void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);
}
