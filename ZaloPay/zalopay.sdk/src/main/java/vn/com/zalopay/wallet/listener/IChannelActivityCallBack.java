package vn.com.zalopay.wallet.listener;


public interface IChannelActivityCallBack {
    void onBackAction();

    void onExitAction();

    void onCallBackAction(boolean pIsShowDialog, String pMessage);
}
