package vn.com.vng.zalopay.data.eventbus;

/**
 * Created by longlv 10/17/2016
 * *
 */
public class WsConnectionEvent {
    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;

    public final boolean isConnect;

    public WsConnectionEvent(int connectionState) {
        this.isConnect = connectionState == CONNECTED;
    }
}
