package vn.com.vng.zalopay.data.ws;

/**
 * Created by AnhHieu on 8/10/16.
 */
public interface SocketClient {
    void connect();

    void disconnect();

    void send(byte[] data);

    boolean isConnected();

    boolean isConnecting();
}
