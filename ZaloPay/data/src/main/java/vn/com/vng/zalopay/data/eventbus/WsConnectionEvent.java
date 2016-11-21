package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by longlv 10/17/2016
 * *
 */
public class WsConnectionEvent {
    public final boolean isConnect;

    public WsConnectionEvent(boolean isConnect) {
        this.isConnect = isConnect;
    }
}
