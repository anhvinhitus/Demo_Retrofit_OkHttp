package vn.com.zalopay.wallet.listener;

public interface ZPWGatewayInfoCallback {
    void onProcessing();

    void onFinish();

    void onError(String pMessage);

    void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);
}
