package vn.com.vng.zalopay.data.ws.connection;

public interface ConnectionListener {

    void onConnected();

    void onReceived(byte[] data);

    void onDisconnected(int code, String message);

    void onError(Throwable error);

}
