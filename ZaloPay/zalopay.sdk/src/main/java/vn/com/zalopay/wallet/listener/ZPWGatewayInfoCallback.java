package vn.com.zalopay.wallet.listener;

/***
 * CLIENT USE THIS CALLBACK TO GET RESULT FROM GETWAY INFO DataRepository.
 */
public interface ZPWGatewayInfoCallback {
    void onProcessing();

    void onFinish();

    void onError(String pMessage);

    void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);
}
