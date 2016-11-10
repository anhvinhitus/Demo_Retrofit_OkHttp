package vn.com.vng.zalopay.data.ws.connection;

/**
 * Created by huuhoa on 11/10/16.
 * Data struct to hold message to be sent to server
 */

public class NotificationApiMessage {
    public final int messageCode;
    public final byte[] messageContent;

    public NotificationApiMessage(int messageCode, byte[] messageContent) {
        this.messageCode = messageCode;
        this.messageContent = messageContent;
    }
}
