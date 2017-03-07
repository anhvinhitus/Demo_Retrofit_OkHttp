package vn.com.vng.zalopay.data.ws.connection;

/**
 * Created by AnhHieu on 8/10/16.
 * Socket connection events
 */
interface Listener {
    void onConnected();

    void onMessage(byte[] data);

    void onDisconnected(ConnectionErrorCode code, String reason);

    void onError(Throwable error);
}