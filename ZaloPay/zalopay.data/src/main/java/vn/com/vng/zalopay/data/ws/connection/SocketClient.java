package vn.com.vng.zalopay.data.ws.connection;

/**
 * Created by AnhHieu on 8/10/16.
 * Interface for socket connect
 */
interface SocketClient {
    void connect();
    void disconnect();
    void send(byte[] data);

    boolean isConnected();
    boolean isConnecting();
}
