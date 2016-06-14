package vn.com.vng.zalopay.data.ws.connection;

public interface ConnectionListener {

    void onConnected();

    void onReceived(byte[] data);

    void onError(int code, String message);

    void onDisconnected(int code, String message);

}
