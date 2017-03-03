package vn.com.zalopay.wallet.listener;

public interface ILoadBankListListener {
    void onProcessing();

    void onComplete();

    void onError(String pMessage);
}
