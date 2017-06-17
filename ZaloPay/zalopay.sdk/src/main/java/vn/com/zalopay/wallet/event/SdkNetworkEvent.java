package vn.com.zalopay.wallet.event;

public class SdkNetworkEvent {
    public int origin;
    public boolean online;

    public SdkNetworkEvent(int origin, boolean online) {
        this.online = online;
        this.origin = origin;
    }
}
