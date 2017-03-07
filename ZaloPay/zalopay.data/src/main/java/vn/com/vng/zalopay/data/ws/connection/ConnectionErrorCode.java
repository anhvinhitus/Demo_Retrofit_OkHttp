package vn.com.vng.zalopay.data.ws.connection;

/**
 * Created by huuhoa on 9/4/16.
 * Define common error codes for socket connection
 */
enum ConnectionErrorCode {
    UNKNOWN_ERROR(0),
    SUCCESS(1),
    DISCONNECT_FINALIZE(2),
    TRIGGER_DISCONNECT(3),
    READ_ERROR(4),
    WRITE_ERROR(5),
    CONNECTION_ERROR(5);

    private final int value;
    ConnectionErrorCode(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
