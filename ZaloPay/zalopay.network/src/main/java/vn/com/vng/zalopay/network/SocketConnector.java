package vn.com.vng.zalopay.network;

/**
 * Created by AnhHieu on 8/10/16.
 * Interface for socket connect
 */
public interface SocketConnector {
    void connect();
    void disconnect();
    void send(byte[] data);

    boolean isConnected();
    boolean isConnecting();
}
