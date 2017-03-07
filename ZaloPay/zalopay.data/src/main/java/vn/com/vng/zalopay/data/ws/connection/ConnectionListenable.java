package vn.com.vng.zalopay.data.ws.connection;

/**
 * Created by hieuvm on 12/20/16.
 */

public interface ConnectionListenable {
    void onConnected();

    void onReceived(byte[] data);

    void onDisconnected(ConnectionErrorCode reason);
}
