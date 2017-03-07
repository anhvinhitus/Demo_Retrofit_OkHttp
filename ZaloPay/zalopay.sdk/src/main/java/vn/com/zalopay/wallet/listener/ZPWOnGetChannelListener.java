package vn.com.zalopay.wallet.listener;

public interface ZPWOnGetChannelListener {
    void onGetChannelComplete();

    void onGetChannelError(String pError);
}
