package vn.com.vng.zalopay.network;

/**
 * Created by AnhHieu on 8/10/16.
 * Socket connection events
 */
public interface ConnectorListener {
    void onConnected();

    void onMessage(byte[] data);

    void onDisconnected(ConnectionErrorCode code, String reason);

    void onError(Throwable error);
}