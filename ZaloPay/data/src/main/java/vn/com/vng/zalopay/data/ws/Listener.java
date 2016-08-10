package vn.com.vng.zalopay.data.ws;

/**
 * Created by AnhHieu on 8/10/16.
 */
public interface Listener {
    void onConnected();

    void onMessage(byte[] data);

    void onDisconnected(int code, String reason);

    void onError(Throwable error);
}